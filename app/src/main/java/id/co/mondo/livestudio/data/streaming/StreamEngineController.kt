package id.co.mondo.livestudio.data.streaming

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.StreamStatus
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.model.VideoQualityProfile
import id.co.mondo.livestudio.service.StreamService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamEngineController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _streamStatus = MutableStateFlow<StreamStatus>(StreamStatus.Idle)
    val streamStatus: StateFlow<StreamStatus> = _streamStatus.asStateFlow()

    /** Permintaan membuka pemilih aplikasi (misal dari tombol di notifikasi). */
    private val _pickAppRequested = MutableStateFlow(false)
    val pickAppRequested: StateFlow<Boolean> = _pickAppRequested.asStateFlow()

    fun requestPicker() {
        _pickAppRequested.value = true
    }

    fun consumePickerRequest() {
        _pickAppRequested.value = false
    }

    fun updateStatus(status: StreamStatus) {
        _streamStatus.value = status
    }

    fun resetStatus() {
        _streamStatus.value = StreamStatus.Idle
    }

    fun updateBitrate(bitrateKbps: Int) {
        _streamStatus.update { current ->
            when (current) {
                is StreamStatus.Streaming -> current.copy(bitrateKbps = bitrateKbps)
                is StreamStatus.ScreenPaused -> current.copy(bitrateKbps = bitrateKbps)
                else -> current
            }
        }
    }

    fun updateDuration(durationSeconds: Long) {
        _streamStatus.update { current ->
            when (current) {
                is StreamStatus.Streaming -> current.copy(durationSeconds = durationSeconds)
                is StreamStatus.ScreenPaused -> current.copy(durationSeconds = durationSeconds)
                else -> current
            }
        }
    }

    fun updateAudioMute(isMicMuted: Boolean? = null, isAppAudioMuted: Boolean? = null) {
        _streamStatus.update { current ->
            when (current) {
                is StreamStatus.Streaming -> current.copy(
                    isMicMuted = isMicMuted ?: current.isMicMuted,
                    isAppAudioMuted = isAppAudioMuted ?: current.isAppAudioMuted
                )
                is StreamStatus.ScreenPaused -> current.copy(
                    isMicMuted = isMicMuted ?: current.isMicMuted,
                    isAppAudioMuted = isAppAudioMuted ?: current.isAppAudioMuted
                )
                else -> current
            }
        }
    }

    fun updateShareTarget(shareMode: ShareModeType, targetApp: TargetApp?) {
        _streamStatus.update { current ->
            when (current) {
                is StreamStatus.Streaming -> current.copy(
                    shareMode = shareMode,
                    targetApp = targetApp
                )
                is StreamStatus.ScreenPaused -> current.copy(
                    shareMode = shareMode,
                    targetApp = targetApp
                )
                else -> current
            }
        }
    }

    fun markScreenPaused() {
        _streamStatus.update { current ->
            if (current is StreamStatus.Streaming) {
                StreamStatus.ScreenPaused(
                    durationSeconds = current.durationSeconds,
                    bitrateKbps = current.bitrateKbps,
                    isMicMuted = current.isMicMuted,
                    isAppAudioMuted = current.isAppAudioMuted,
                    shareMode = current.shareMode,
                    targetApp = current.targetApp
                )
            } else {
                current
            }
        }
    }

    fun markScreenResumed() {
        _streamStatus.update { current ->
            if (current is StreamStatus.ScreenPaused) {
                StreamStatus.Streaming(
                    durationSeconds = current.durationSeconds,
                    bitrateKbps = current.bitrateKbps,
                    isMicMuted = current.isMicMuted,
                    isAppAudioMuted = current.isAppAudioMuted,
                    shareMode = current.shareMode,
                    targetApp = current.targetApp
                )
            } else {
                current
            }
        }
    }

    fun startStream(
        resultCode: Int,
        data: Intent,
        rtmpsUrl: String,
        quality: VideoQualityProfile,
        shareMode: ShareModeType,
        targetApp: TargetApp?
    ) {
        _streamStatus.value = StreamStatus.Connecting

        val intent = Intent(context, StreamService::class.java).apply {
            action = StreamService.ACTION_START
            putExtra(StreamService.EXTRA_RESULT_CODE, resultCode)
            putExtra(StreamService.EXTRA_DATA_INTENT, data)
            putExtra(StreamService.EXTRA_RTMPS_URL, rtmpsUrl)
            putExtra(StreamService.EXTRA_QUALITY_NAME, quality.name)
            putExtra(StreamService.EXTRA_SHARE_MODE, shareMode.name)
            putExtra(StreamService.EXTRA_TARGET_PACKAGE, targetApp?.packageName)
            putExtra(StreamService.EXTRA_TARGET_APP_NAME, targetApp?.appName)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    /** Mengganti mode berbagi layar atau aplikasi target di tengah siaran tanpa memutus sesi live. */
    fun changeShareTarget(shareMode: ShareModeType, targetApp: TargetApp?) {
        val intent = Intent(context, StreamService::class.java).apply {
            action = StreamService.ACTION_CHANGE_TARGET_APP
            putExtra(StreamService.EXTRA_SHARE_MODE, shareMode.name)
            putExtra(StreamService.EXTRA_TARGET_PACKAGE, targetApp?.packageName)
            putExtra(StreamService.EXTRA_TARGET_APP_NAME, targetApp?.appName)
        }
        context.startService(intent)
    }

    /** Mengganti projection baru jika token sebelumnya dicabut dari sistem. */
    fun updateProjection(resultCode: Int, data: Intent) {
        val intent = Intent(context, StreamService::class.java).apply {
            action = StreamService.ACTION_UPDATE_PROJECTION
            putExtra(StreamService.EXTRA_RESULT_CODE, resultCode)
            putExtra(StreamService.EXTRA_DATA_INTENT, data)
        }
        context.startService(intent)
    }

    fun stopStream() {
        val intent = Intent(context, StreamService::class.java).apply {
            action = StreamService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun toggleMic(mute: Boolean) {
        val intent = Intent(context, StreamService::class.java).apply {
            action = StreamService.ACTION_TOGGLE_MIC
            putExtra(StreamService.EXTRA_MUTE_STATE, mute)
        }
        context.startService(intent)
    }

    fun toggleAppAudio(mute: Boolean) {
        val intent = Intent(context, StreamService::class.java).apply {
            action = StreamService.ACTION_TOGGLE_APP_AUDIO
            putExtra(StreamService.EXTRA_MUTE_STATE, mute)
        }
        context.startService(intent)
    }
}
