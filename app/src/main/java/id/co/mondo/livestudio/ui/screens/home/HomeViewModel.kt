package id.co.mondo.livestudio.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.domain.model.AccountRequirementException
import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.domain.model.SessionExpiredException
import id.co.mondo.livestudio.domain.usecase.CreateLiveStreamUseCase
import id.co.mondo.livestudio.domain.usecase.GetBroadcastSettingsUseCase
import id.co.mondo.livestudio.domain.usecase.GetLoginStateUseCase
import id.co.mondo.livestudio.domain.usecase.LogoutUseCase
import id.co.mondo.livestudio.domain.usecase.SaveBroadcastSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getLoginStateUseCase: GetLoginStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val createLiveStreamUseCase: CreateLiveStreamUseCase,
    private val getBroadcastSettingsUseCase: GetBroadcastSettingsUseCase,
    private val saveBroadcastSettingsUseCase: SaveBroadcastSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (title, privacy) = getBroadcastSettingsUseCase().first()
            _uiState.update { current ->
                // Jangan timpa bila pengguna sudah sempat mengetik.
                if (current.title.isEmpty()) current.copy(title = title, privacy = privacy)
                else current.copy(privacy = privacy)
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

    fun onStartLiveClicked() {
        val current = _uiState.value
        if (current.isLoading) return
        if (current.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = createLiveStreamUseCase(current.title, current.privacy)) {
                is Result.Success -> {
                    saveBroadcastSettingsUseCase(current.title.trim(), current.privacy)
                    _uiState.update { it.copy(isLoading = false, createdBroadcast = result.data) }
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

    fun onLogoutClicked() {
        viewModelScope.launch { logoutUseCase() }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onRequirementSheetDismissed() {
        _uiState.update { it.copy(showRequirementSheet = false) }
    }

    fun onBroadcastDialogDismissed() {
        _uiState.update { it.copy(createdBroadcast = null) }
    }
}