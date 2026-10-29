package id.co.mondo.livestudio.ui.screens.status

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.co.mondo.livestudio.core.util.AppHelper
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.StreamStatus
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.repository.StreamRepository
import id.co.mondo.livestudio.domain.usecase.GetStreamStatusUseCase
import id.co.mondo.livestudio.domain.usecase.StopStreamUseCase
import id.co.mondo.livestudio.domain.usecase.ToggleAudioUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StreamStatusViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    getStreamStatusUseCase: GetStreamStatusUseCase,
    private val stopStreamUseCase: StopStreamUseCase,
    private val toggleAudioUseCase: ToggleAudioUseCase,
    private val streamRepository: StreamRepository
) : ViewModel() {

    val streamStatus: StateFlow<StreamStatus> = getStreamStatusUseCase()
    val pickAppRequested: StateFlow<Boolean> = streamRepository.pickAppRequested

    private val _showStopDialog = MutableStateFlow(false)
    val showStopDialog: StateFlow<Boolean> = _showStopDialog.asStateFlow()

    private val _showAppPicker = MutableStateFlow(false)
    val showAppPicker: StateFlow<Boolean> = _showAppPicker.asStateFlow()

    private val _installedApps = MutableStateFlow<List<TargetApp>>(emptyList())
    val installedApps: StateFlow<List<TargetApp>> = _installedApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                AppHelper.getInstalledLauncherApps(appContext)
            }
            _installedApps.value = apps
        }
    }

    fun onOpenAppPicker() {
        loadInstalledApps()
        _showAppPicker.value = true
    }

    fun onDismissAppPicker() {
        _showAppPicker.value = false
    }

    /** Mengganti ke aplikasi target baru di tengah siaran. */
    fun onSelectTargetApp(app: TargetApp) {
        _showAppPicker.value = false
        viewModelScope.launch {
            streamRepository.saveShareSettings(ShareModeType.SPECIFIC_APP, app)
        }
        streamRepository.changeShareTarget(ShareModeType.SPECIFIC_APP, app)
    }

    /** Mengganti mode ke Seluruh Layar di tengah siaran. */
    fun onSelectEntireScreen() {
        _showAppPicker.value = false
        viewModelScope.launch {
            streamRepository.saveShareSettings(ShareModeType.ENTIRE_SCREEN, null)
        }
        streamRepository.changeShareTarget(ShareModeType.ENTIRE_SCREEN, null)
    }

    /** Membuka aplikasi target yang sedang dipilih ke layar depan. */
    fun onOpenCurrentTargetApp(app: TargetApp) {
        AppHelper.launchApp(appContext, app.packageName)
    }

    fun onStopClicked() {
        _showStopDialog.value = true
    }

    fun onConfirmStop() {
        _showStopDialog.value = false
        stopStreamUseCase()
    }

    fun onDismissStopDialog() {
        _showStopDialog.value = false
    }

    fun onToggleMic(mute: Boolean) {
        toggleAudioUseCase.toggleMic(mute)
    }

    fun onToggleAppAudio(mute: Boolean) {
        toggleAudioUseCase.toggleAppAudio(mute)
    }

    fun onPickerRequestConsumed() {
        streamRepository.consumePickerRequest()
    }

    /** Dipanggil setelah layar status selesai ditutup (siaran berakhir atau gagal). */
    fun onFinishedAcknowledged() {
        streamRepository.resetStatus()
    }
}
