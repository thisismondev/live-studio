package id.co.mondo.livestudio.ui.screens.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.domain.usecase.GetLoginStateUseCase
import id.co.mondo.livestudio.domain.usecase.LoginFacebookUseCase
import id.co.mondo.livestudio.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginFacebookUseCase: LoginFacebookUseCase,
    getLoginStateUseCase: GetLoginStateUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val userSessionState = getLoginStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onLoginClicked(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = loginFacebookUseCase(activity)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.Error(result.message ?: result.exception.localizedMessage ?: "Gagal login")
                }
                is Result.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = logoutUseCase()) {
                is Result.Success -> {
                    _uiState.value = LoginUiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.Error(result.message ?: "Gagal logout")
                }
                is Result.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }

    fun resetUiState() {
        _uiState.value = LoginUiState.Idle
    }
}
