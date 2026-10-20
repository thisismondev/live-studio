package id.co.mondo.livestudio.ui.screens.login

import id.co.mondo.livestudio.domain.model.UserSession

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val userSession: UserSession) : LoginUiState
    data class Error(val message: String) : LoginUiState
}
