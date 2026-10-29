package id.co.mondo.livestudio.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.co.mondo.livestudio.core.util.AppHelper
import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.domain.model.AccountRequirementException
import id.co.mondo.livestudio.domain.model.BroadcastConfig
import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.domain.model.SessionExpiredException
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.model.VideoQualityProfile
import id.co.mondo.livestudio.domain.repository.StreamRepository
import id.co.mondo.livestudio.domain.usecase.CreateLiveStreamUseCase
import id.co.mondo.livestudio.domain.usecase.GetBroadcastSettingsUseCase
import id.co.mondo.livestudio.domain.usecase.GetLoginStateUseCase
import id.co.mondo.livestudio.domain.usecase.LogoutUseCase
import id.co.mondo.livestudio.domain.usecase.SaveBroadcastSettingsUseCase
import id.co.mondo.livestudio.domain.usecase.StartStreamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    getLoginStateUseCase: GetLoginStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val createLiveStreamUseCase: CreateLiveStreamUseCase,
    private val getBroadcastSettingsUseCase: GetBroadcastSettingsUseCase,
    private val saveBroadcastSettingsUseCase: SaveBroadcastSettingsUseCase,
    private val startStreamUseCase: StartStreamUseCase,
    private val streamRepository: StreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (title, privacy) = getBroadcastSettingsUseCase().first()
            val quality = streamRepository.getQualityProfile().first()
            val (savedShareMode, savedTargetApp) = streamRepository.getShareSettings().first()
            val installedApps = withContext(Dispatchers.IO) {
                AppHelper.getInstalledLauncherApps(appContext)
            }
            // Pastikan aplikasi yang tersimpan masih terpasang di perangkat.
            val validSavedApp = savedTargetApp?.let { saved ->
                installedApps.firstOrNull { it.packageName == saved.packageName }
            } ?: installedApps.firstOrNull()

            _uiState.update { current ->
                current.copy(
                    title = if (current.title.isEmpty()) title else current.title,
                    privacy = privacy,
                    quality = quality,
                    shareMode = savedShareMode,
                    selectedApp = validSavedApp,
                    installedApps = installedApps
                )
            }
        }
        viewModelScope.launch {
            getLoginStateUseCase().collect { session ->
                _uiState.update {
                    it.copy(
                        userName = session?.userName.orEmpty(),
                        userPhotoUrl = session?.userPhotoUrl
                    )
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title, titleError = false) }
    }

    fun onPrivacySelected(privacy: PrivacyOption) {
        _uiState.update { it.copy(privacy = privacy) }
    }

    fun onQualitySelected(quality: VideoQualityProfile) {
        _uiState.update { it.copy(quality = quality) }
        viewModelScope.launch {
            streamRepository.saveQualityProfile(quality)
        }
    }

    fun onShareModeSelected(mode: ShareModeType) {
        _uiState.update { it.copy(shareMode = mode, targetAppError = false) }
        viewModelScope.launch {
            streamRepository.saveShareSettings(mode, _uiState.value.selectedApp)
        }
    }

    fun onOpenAppPicker() {
        viewModelScope.launch {
            val freshApps = withContext(Dispatchers.IO) {
                AppHelper.getInstalledLauncherApps(appContext)
            }
            _uiState.update {
                it.copy(
                    installedApps = freshApps,
                    showAppPicker = true,
                    targetAppError = false
                )
            }
        }
    }

    fun onDismissAppPicker() {
        _uiState.update { it.copy(showAppPicker = false) }
    }

    fun onTargetAppSelected(app: TargetApp) {
        _uiState.update {
            it.copy(
                shareMode = ShareModeType.SPECIFIC_APP,
                selectedApp = app,
                showAppPicker = false,
                targetAppError = false
            )
        }
        viewModelScope.launch {
            streamRepository.saveShareSettings(ShareModeType.SPECIFIC_APP, app)
        }
    }

    fun onStartLiveClicked() {
        val current = _uiState.value
        if (current.isLoading) return
        if (current.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }
        if (current.shareMode == ShareModeType.SPECIFIC_APP && current.selectedApp == null) {
            _uiState.update { it.copy(targetAppError = true, showAppPicker = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = createLiveStreamUseCase(current.title, current.privacy)) {
                is Result.Success -> {
                    saveBroadcastSettingsUseCase(current.title.trim(), current.privacy)
                    streamRepository.saveShareSettings(current.shareMode, current.selectedApp)
                    _uiState.update { it.copy(isLoading = false, pendingBroadcast = result.data) }
                }
                is Result.Error -> {
                    when (result.exception) {
                        is AccountRequirementException -> _uiState.update {
                            it.copy(isLoading = false, showRequirementSheet = true)
                        }
                        is SessionExpiredException -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = result.message ?: "Sesi Facebook berakhir. Silakan login ulang."
                                )
                            }
                            logoutUseCase()
                        }
                        else -> _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "Gagal membuat siaran."
                            )
                        }
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onScreenCapturePermissionGranted(
        resultCode: Int,
        data: Intent,
        broadcastConfig: BroadcastConfig
    ) {
        val current = _uiState.value
        startStreamUseCase(
            resultCode = resultCode,
            data = data,
            rtmpsUrl = broadcastConfig.rtmpsUrl,
            quality = current.quality,
            shareMode = current.shareMode,
            targetApp = if (current.shareMode == ShareModeType.SPECIFIC_APP) current.selectedApp else null
        )
        _uiState.update { it.copy(pendingBroadcast = null) }
    }

    fun onScreenCaptureCancelled() {
        _uiState.update {
            it.copy(
                pendingBroadcast = null,
                isLoading = false,
                errorMessage = "Izin rekam layar diperlukan untuk memulai siaran."
            )
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch { logoutUseCase() }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onRequirementSheetDismissed() {
        _uiState.update { it.copy(showRequirementSheet = false) }
    }
}
