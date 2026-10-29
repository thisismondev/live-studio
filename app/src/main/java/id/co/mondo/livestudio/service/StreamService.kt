package id.co.mondo.livestudio.service

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.gl.render.filters.`object`.ImageObjectFilterRender
import com.pedro.encoder.input.sources.audio.MicrophoneSource
import com.pedro.encoder.input.sources.audio.MixAudioSource
import com.pedro.encoder.input.sources.video.BitmapSource
import com.pedro.encoder.input.sources.video.NoVideoSource
import com.pedro.encoder.input.sources.video.ScreenSource
import com.pedro.library.generic.GenericStream
import com.pedro.library.util.BitrateAdapter
import dagger.hilt.android.AndroidEntryPoint
import id.co.mondo.livestudio.core.service.NotificationHelper
import id.co.mondo.livestudio.core.util.AppHelper
import id.co.mondo.livestudio.core.util.Constants
import id.co.mondo.livestudio.data.streaming.StreamEngineController
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.StreamStatus
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.model.VideoQualityProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service penyiaran. Memakai [GenericStream] (RootEncoder 2.8.1) dengan [ScreenSource].
 * Mendukung dua mode:
 * - [ShareModeType.ENTIRE_SCREEN]: menyiarkan seluruh layar secara terus-menerus.
 * - [ShareModeType.SPECIFIC_APP]: meluncurkan aplikasi target dan menyensor layar otomatis
 *   ([ImageObjectFilterRender]) saat pengguna keluar dari aplikasi target demi privasi.
 */
@AndroidEntryPoint
class StreamService : Service(), ConnectChecker {

    @Inject
    lateinit var streamEngineController: StreamEngineController

    private var stream: GenericStream? = null
    private var projection: MediaProjection? = null
    private var bitrateAdapter: BitrateAdapter? = null
    private var privacyFilter: ImageObjectFilterRender? = null
    private var cachedPausedBitmap: Bitmap? = null
    private var cachedPausedAppName: String? = null

    private var durationSeconds: Long = 0L
    private var tickerJob: Job? = null
    private var foregroundMonitorJob: Job? = null
    private var reconnectAttempts = 0
    private var currentQuality: VideoQualityProfile = VideoQualityProfile.AUTO_720P
    private var currentShareMode: ShareModeType = ShareModeType.ENTIRE_SCREEN
    private var currentTargetApp: TargetApp? = null
    private var isPrivacyPaused: Boolean = false
    private var isIntentionallyStopping = false
    private var hasConnectedOnce = false

    private var micMuted = false
    private var appAudioMuted = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            handleProjectionStopped()
        }
    }

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NotificationHelper.ACTION_STOP_STREAM) {
                stopStreaming()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        ContextCompat.registerReceiver(
            this,
            stopReceiver,
            IntentFilter(NotificationHelper.ACTION_STOP_STREAM),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val dataIntent = readDataIntent(intent)
                val rtmpsUrl = intent.getStringExtra(EXTRA_RTMPS_URL) ?: ""
                val quality = VideoQualityProfile.fromName(intent.getStringExtra(EXTRA_QUALITY_NAME))
                val shareMode = ShareModeType.fromName(intent.getStringExtra(EXTRA_SHARE_MODE))
                val targetPkg = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
                val targetName = intent.getStringExtra(EXTRA_TARGET_APP_NAME)
                val targetApp = if (!targetPkg.isNullOrBlank() && !targetName.isNullOrBlank()) {
                    TargetApp(targetPkg, targetName)
                } else {
                    null
                }

                if (resultCode != 0 && dataIntent != null && rtmpsUrl.isNotBlank()) {
                    startStreaming(resultCode, dataIntent, rtmpsUrl, quality, shareMode, targetApp)
                } else {
                    streamEngineController.updateStatus(StreamStatus.Error("Parameter siaran tidak valid."))
                    stopSelf()
                }
            }
            ACTION_CHANGE_TARGET_APP -> {
                val shareMode = ShareModeType.fromName(intent.getStringExtra(EXTRA_SHARE_MODE))
                val targetPkg = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
                val targetName = intent.getStringExtra(EXTRA_TARGET_APP_NAME)
                val targetApp = if (!targetPkg.isNullOrBlank() && !targetName.isNullOrBlank()) {
                    TargetApp(targetPkg, targetName)
                } else {
                    null
                }
                changeTargetShareMode(shareMode, targetApp)
            }
            ACTION_UPDATE_PROJECTION -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val dataIntent = readDataIntent(intent)
                if (resultCode != 0 && dataIntent != null) {
                    restoreProjection(resultCode, dataIntent)
                }
            }
            ACTION_STOP -> stopStreaming()
            ACTION_TOGGLE_MIC -> {
                micMuted = intent.getBooleanExtra(EXTRA_MUTE_STATE, false)
                applyAudioMute()
                streamEngineController.updateAudioMute(isMicMuted = micMuted)
            }
            ACTION_TOGGLE_APP_AUDIO -> {
                appAudioMuted = intent.getBooleanExtra(EXTRA_MUTE_STATE, false)
                applyAudioMute()
                streamEngineController.updateAudioMute(isAppAudioMuted = appAudioMuted)
            }
        }
        return START_NOT_STICKY
    }

    private fun readDataIntent(intent: Intent): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DATA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_DATA_INTENT)
        }

    private fun startStreaming(
        resultCode: Int,
        dataIntent: Intent,
        rtmpsUrl: String,
        quality: VideoQualityProfile,
        shareMode: ShareModeType,
        targetApp: TargetApp?
    ) {
        currentQuality = quality
        currentShareMode = shareMode
        currentTargetApp = targetApp
        isPrivacyPaused = false
        durationSeconds = 0L
        reconnectAttempts = 0
        hasConnectedOnce = false
        isIntentionallyStopping = false
        micMuted = false
        appAudioMuted = false

        // 1. Foreground service harus aktif sebelum MediaProjection dibuat (Android 14+).
        val notification = NotificationHelper.buildNotification(this, "Mempersiapkan siaran...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            startForeground(Constants.NOTIFICATION_ID, notification, serviceType)
        } else {
            startForeground(Constants.NOTIFICATION_ID, notification)
        }

        // 2. MediaProjection dari hasil dialog sistem.
        val newProjection = createProjection(resultCode, dataIntent)
        if (newProjection == null) {
            failAndStop("Izin berbagi layar tidak valid. Coba mulai ulang siaran.")
            return
        }

        // 3. Engine: mulai dengan NoVideoSource, lalu ganti ke ScreenSource.
        val audioSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MixAudioSource(newProjection)
        } else {
            MicrophoneSource()
        }
        val genericStream = GenericStream(applicationContext, this, NoVideoSource(), audioSource).apply {
            // MediaProjection hanya mengirim frame saat layar berubah; paksa render agar FPS stabil.
            getGlInterface().setForceRender(true, 15)
        }
        stream = genericStream

        val videoPrepared = genericStream.prepareVideo(
            quality.width,
            quality.height,
            quality.bitrateBps
        )
        val audioPrepared = genericStream.prepareAudio(AUDIO_SAMPLE_RATE, true, AUDIO_BITRATE)
        if (!videoPrepared || !audioPrepared) {
            failAndStop("Gagal menginisialisasi encoder video/audio perangkat.")
            return
        }

        bitrateAdapter = BitrateAdapter { bitrate ->
            stream?.setVideoBitrateOnFly(bitrate)
        }.apply {
            setMaxBitrate(quality.bitrateBps + AUDIO_BITRATE)
        }

        try {
            genericStream.getGlInterface().setCameraOrientation(0)
            genericStream.changeVideoSource(ScreenSource(applicationContext, newProjection))
        } catch (e: Exception) {
            failAndStop("Gagal memulai penangkapan layar: ${e.localizedMessage}")
            return
        }

        genericStream.getStreamClient().setReTries(MAX_RECONNECT_ATTEMPTS)
        genericStream.startStream(rtmpsUrl)
    }

    /**
     * Mengganti mode berbagi layar (Seluruh Layar <-> Aplikasi Tertentu) atau mengganti
     * aplikasi target di tengah siaran tanpa memutus koneksi Facebook Live.
     */
    private fun changeTargetShareMode(shareMode: ShareModeType, targetApp: TargetApp?) {
        if (isIntentionallyStopping || stream == null) return
        if (currentTargetApp?.packageName != targetApp?.packageName) {
            cachedPausedBitmap?.recycle()
            cachedPausedBitmap = null
            cachedPausedAppName = null
        }
        currentShareMode = shareMode
        currentTargetApp = targetApp
        streamEngineController.updateShareTarget(shareMode, targetApp)

        if (shareMode == ShareModeType.SPECIFIC_APP && targetApp != null) {
            // Luncurkan aplikasi target yang baru dipilih ke layar depan.
            AppHelper.launchApp(this, targetApp.packageName)
            startForegroundAppMonitor()
        } else {
            // Mode Seluruh Layar: matikan sensor privasi dan pastikan layar langsung tampil.
            foregroundMonitorJob?.cancel()
            foregroundMonitorJob = null
            setPrivacyPauseOverlay(paused = false)
            updateNotification("Live Seluruh Layar (${formatDuration(durationSeconds)})")
        }
    }

    /** Memulihkan MediaProjection baru jika dicabut dari sistem. */
    private fun restoreProjection(resultCode: Int, dataIntent: Intent) {
        val genericStream = stream ?: return
        if (isIntentionallyStopping) return

        val newProjection = createProjection(resultCode, dataIntent) ?: return
        try {
            genericStream.getGlInterface().setCameraOrientation(0)
            genericStream.changeVideoSource(ScreenSource(applicationContext, newProjection))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                genericStream.changeAudioSource(MixAudioSource(newProjection))
            }
            applyAudioMute()
            setPrivacyPauseOverlay(paused = false)
            if (currentShareMode == ShareModeType.SPECIFIC_APP && currentTargetApp != null) {
                AppHelper.launchApp(this, currentTargetApp!!.packageName)
                startForegroundAppMonitor()
            }
        } catch (e: Exception) {
            streamEngineController.updateStatus(
                StreamStatus.Error("Gagal memulihkan penangkapan layar: ${e.localizedMessage}")
            )
        }
    }

    /**
     * Memantau aplikasi di layar depan setiap 500ms saat mode [ShareModeType.SPECIFIC_APP].
     * Jika host keluar dari aplikasi target (misal ke Home atau WhatsApp), sensor privasi aktif.
     */
    private fun startForegroundAppMonitor() {
        foregroundMonitorJob?.cancel()
        val target = currentTargetApp ?: return
        if (currentShareMode != ShareModeType.SPECIFIC_APP) return

        foregroundMonitorJob = serviceScope.launch {
            // Beri jeda 1.5 detik di awal agar aplikasi target sempat terbuka ke layar depan.
            setPrivacyPauseOverlay(paused = false)
            delay(1500L)

            var lastKnownPkg: String? = target.packageName
            while (isActive && currentShareMode == ShareModeType.SPECIFIC_APP) {
                val activeTarget = currentTargetApp ?: break
                val foregroundPkg = AppHelper.getForegroundPackageName(
                    context = this@StreamService,
                    lookbackMs = 5_000L,
                    fallbackPackage = lastKnownPkg
                )
                if (foregroundPkg != null) {
                    lastKnownPkg = foregroundPkg
                }

                val isTargetInFront = foregroundPkg == null || foregroundPkg == activeTarget.packageName
                setPrivacyPauseOverlay(paused = !isTargetInFront)

                delay(500L)
            }
        }
    }

    /**
     * Mengaktifkan atau menonaktifkan lapisan gambar jeda privasi di atas stream OpenGL
     * tanpa menghentikan [ScreenSource] atau membuat ulang VirtualDisplay (aman di Android 14+).
     */
    private fun setPrivacyPauseOverlay(paused: Boolean) {
        val genericStream = stream ?: return
        if (projection == null) return // Jika projection mati dari sistem, ditangani terpisah.
        if (isPrivacyPaused == paused) return
        isPrivacyPaused = paused

        try {
            if (paused) {
                val filter = ImageObjectFilterRender().apply {
                    setImage(
                        getOrCreatePausedBitmap(
                            width = currentQuality.width,
                            height = currentQuality.height,
                            appName = currentTargetApp?.appName
                        )
                    )
                    setScale(100f, 100f)
                    setPosition(0f, 0f)
                }
                privacyFilter = filter
                genericStream.getGlInterface().addFilter(filter)
                applyAudioMute()
                streamEngineController.markScreenPaused()
                val appLabel = currentTargetApp?.appName ?: "aplikasi"
                updateNotification("Dijeda demi privasi. Buka $appLabel untuk melanjutkan.")
            } else {
                privacyFilter?.let { filter ->
                    genericStream.getGlInterface().removeFilter(filter)
                }
                privacyFilter = null
                applyAudioMute()
                streamEngineController.markScreenResumed()
                updateNotification(buildStreamingNotificationText())
            }
        } catch (_: Exception) {
        }
    }

    /** Membuat [MediaProjection] baru, melepas yang lama tanpa memicu callback jeda. */
    private fun createProjection(resultCode: Int, dataIntent: Intent): MediaProjection? {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val newProjection = try {
            manager.getMediaProjection(resultCode, dataIntent)
        } catch (e: Exception) {
            null
        } ?: return null

        projection?.let { old ->
            try {
                old.unregisterCallback(projectionCallback)
                old.stop()
            } catch (_: Exception) {
            }
        }
        newProjection.registerCallback(projectionCallback, mainHandler)
        projection = newProjection
        return newProjection
    }

    /** Berbagi layar dihentikan paksa dari sistem: siaran tetap hidup dengan BitmapSource. */
    private fun handleProjectionStopped() {
        if (isIntentionallyStopping) return
        val genericStream = stream ?: return
        projection = null
        foregroundMonitorJob?.cancel()
        foregroundMonitorJob = null

        try {
            privacyFilter?.let { genericStream.getGlInterface().removeFilter(it) }
            privacyFilter = null
            genericStream.changeVideoSource(
                BitmapSource(
                    getOrCreatePausedBitmap(
                        width = currentQuality.width,
                        height = currentQuality.height,
                        appName = currentTargetApp?.appName
                    )
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                genericStream.changeAudioSource(MicrophoneSource())
            }
            applyAudioMute()
        } catch (_: Exception) {
        }

        isPrivacyPaused = true
        streamEngineController.markScreenPaused()
        updateNotification("Berbagi layar dijeda. Buka Live Studio untuk melanjutkan.")
    }

    /**
     * Mengatur volume mikrofon dan audio aplikasi.
     * Sesuai kesepakatan desain: saat layar dijeda demi privasi ([isPrivacyPaused] == true),
     * mikrofon tetap aktif agar host bisa menyapa penonton, sedangkan suara internal aplikasi
     * lain dibisukan agar suara notifikasi/chat tidak bocor.
     */
    private fun applyAudioMute() {
        val source = stream?.audioSource ?: return
        when (source) {
            is MixAudioSource -> {
                source.microphoneVolume = if (micMuted) 0f else MIC_VOLUME
                source.internalVolume = if (appAudioMuted || isPrivacyPaused) 0f else INTERNAL_VOLUME
            }
            is MicrophoneSource -> {
                if (micMuted) source.mute() else source.unMute()
            }
        }
    }

    private fun getOrCreatePausedBitmap(width: Int, height: Int, appName: String?): Bitmap {
        val existing = cachedPausedBitmap
        if (existing != null && !existing.isRecycled && cachedPausedAppName == appName &&
            existing.width == width && existing.height == height
        ) {
            return existing
        }
        existing?.recycle()
        val created = createPausedBitmap(width, height, appName)
        cachedPausedBitmap = created
        cachedPausedAppName = appName
        return created
    }

    private fun createPausedBitmap(width: Int, height: Int, appName: String?): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#1A1C2C"))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = width * 0.068f
            isFakeBoldText = true
        }
        canvas.drawText("Siaran Layar Dijeda", width / 2f, height / 2f - width * 0.04f, paint)
        paint.apply {
            textSize = width * 0.042f
            isFakeBoldText = false
            color = Color.parseColor("#B0B3C6")
        }
        val subtitle = if (!appName.isNullOrBlank()) {
            "Host sedang di luar $appName"
        } else {
            "Siaran akan dilanjutkan sebentar lagi"
        }
        canvas.drawText(subtitle, width / 2f, height / 2f + width * 0.06f, paint)
        paint.apply {
            textSize = width * 0.036f
            color = Color.parseColor("#8E92AB")
        }
        canvas.drawText("Suara mikrofon host tetap aktif", width / 2f, height / 2f + width * 0.14f, paint)
        return bitmap
    }

    private fun failAndStop(message: String) {
        streamEngineController.updateStatus(StreamStatus.Error(message))
        stopStreaming(keepStatus = true)
    }

    private fun stopStreaming(keepStatus: Boolean = false) {
        isIntentionallyStopping = true
        tickerJob?.cancel()
        foregroundMonitorJob?.cancel()

        try {
            stream?.let {
                privacyFilter?.let { filter -> it.getGlInterface().removeFilter(filter) }
                if (it.isStreaming) it.stopStream()
                it.release()
            }
        } catch (_: Exception) {
        }
        privacyFilter = null
        cachedPausedBitmap?.recycle()
        cachedPausedBitmap = null
        cachedPausedAppName = null
        stream = null
        bitrateAdapter = null

        projection?.let {
            try {
                it.unregisterCallback(projectionCallback)
                it.stop()
            } catch (_: Exception) {
            }
        }
        projection = null

        if (!keepStatus) {
            streamEngineController.updateStatus(StreamStatus.Stopped(durationSeconds))
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ---- ConnectChecker (dipanggil dari thread RootEncoder) ----

    override fun onConnectionStarted(url: String) {
        if (!hasConnectedOnce) streamEngineController.updateStatus(StreamStatus.Connecting)
    }

    override fun onConnectionSuccess() {
        val isFirstConnect = !hasConnectedOnce
        reconnectAttempts = 0
        hasConnectedOnce = true
        stream?.getStreamClient()?.setReTries(MAX_RECONNECT_ATTEMPTS)

        val kbps = currentQuality.bitrateBps / 1000
        val status = if (projection == null || isPrivacyPaused) {
            StreamStatus.ScreenPaused(
                durationSeconds = durationSeconds,
                bitrateKbps = kbps,
                isMicMuted = micMuted,
                isAppAudioMuted = appAudioMuted,
                shareMode = currentShareMode,
                targetApp = currentTargetApp
            )
        } else {
            StreamStatus.Streaming(
                durationSeconds = durationSeconds,
                bitrateKbps = kbps,
                isMicMuted = micMuted,
                isAppAudioMuted = appAudioMuted,
                shareMode = currentShareMode,
                targetApp = currentTargetApp
            )
        }
        streamEngineController.updateStatus(status)

        mainHandler.post {
            startDurationTicker()
            if (currentShareMode == ShareModeType.SPECIFIC_APP && currentTargetApp != null) {
                if (isFirstConnect) {
                    // Otomatis luncurkan aplikasi target ke layar depan saat live terhubung.
                    AppHelper.launchApp(this@StreamService, currentTargetApp!!.packageName)
                }
                startForegroundAppMonitor()
            }
        }
    }

    override fun onConnectionFailed(reason: String) {
        if (isIntentionallyStopping) return
        val genericStream = stream ?: return

        if (genericStream.getStreamClient().reTry(RECONNECT_DELAY_MS, reason)) {
            reconnectAttempts++
            streamEngineController.updateStatus(
                StreamStatus.Reconnecting(reconnectAttempts, MAX_RECONNECT_ATTEMPTS)
            )
            updateNotification(
                "Menghubungkan Ulang... ($reconnectAttempts/$MAX_RECONNECT_ATTEMPTS)",
                isReconnecting = true
            )
        } else {
            streamEngineController.updateStatus(
                StreamStatus.Error("Koneksi terputus: $reason. Percobaan sambung ulang habis.")
            )
            mainHandler.post { stopStreaming(keepStatus = true) }
        }
    }

    override fun onDisconnect() {
        // Pemutusan tidak disengaja ditangani lewat onConnectionFailed.
    }

    override fun onAuthError() {
        streamEngineController.updateStatus(
            StreamStatus.Error("Autentikasi Facebook Live gagal (stream key tidak valid atau kedaluwarsa).")
        )
        mainHandler.post { stopStreaming(keepStatus = true) }
    }

    override fun onAuthSuccess() {
        // Terautentikasi.
    }

    override fun onNewBitrate(bitrate: Long) {
        val congested = stream?.getStreamClient()?.hasCongestion() ?: false
        bitrateAdapter?.adaptBitrate(bitrate, congested)
        streamEngineController.updateBitrate((bitrate / 1000).toInt())
    }

    private fun startDurationTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                durationSeconds++
                streamEngineController.updateDuration(durationSeconds)
                if (projection != null && !isPrivacyPaused) {
                    updateNotification(buildStreamingNotificationText())
                }
            }
        }
    }

    private fun buildStreamingNotificationText(): String {
        val targetLabel = if (currentShareMode == ShareModeType.SPECIFIC_APP && currentTargetApp != null) {
            currentTargetApp!!.appName
        } else {
            "Seluruh Layar"
        }
        return "Live: $targetLabel (${formatDuration(durationSeconds)})"
    }

    private fun updateNotification(text: String, isReconnecting: Boolean = false) {
        val notification = NotificationHelper.buildNotification(this, text, isReconnecting)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.NOTIFICATION_ID, notification)
    }

    private fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(stopReceiver)
        } catch (_: Exception) {
        }
        tickerJob?.cancel()
        foregroundMonitorJob?.cancel()
        serviceScope.coroutineContext[Job]?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "id.co.mondo.livestudio.action.START"
        const val ACTION_CHANGE_TARGET_APP = "id.co.mondo.livestudio.action.CHANGE_TARGET_APP"
        const val ACTION_UPDATE_PROJECTION = "id.co.mondo.livestudio.action.UPDATE_PROJECTION"
        const val ACTION_STOP = "id.co.mondo.livestudio.action.STOP"
        const val ACTION_TOGGLE_MIC = "id.co.mondo.livestudio.action.TOGGLE_MIC"
        const val ACTION_TOGGLE_APP_AUDIO = "id.co.mondo.livestudio.action.TOGGLE_APP_AUDIO"

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_DATA_INTENT = "extra_data_intent"
        const val EXTRA_RTMPS_URL = "extra_rtmps_url"
        const val EXTRA_QUALITY_NAME = "extra_quality_name"
        const val EXTRA_SHARE_MODE = "extra_share_mode"
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
        const val EXTRA_TARGET_APP_NAME = "extra_target_app_name"
        const val EXTRA_MUTE_STATE = "extra_mute_state"

        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val RECONNECT_DELAY_MS = 3000L
        private const val AUDIO_SAMPLE_RATE = 44100
        private const val AUDIO_BITRATE = 128 * 1000
        private const val MIC_VOLUME = 1f
        private const val INTERNAL_VOLUME = 1f
    }
}
