package id.co.mondo.livestudio.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.mondo.livestudio.domain.model.StreamStatus
import id.co.mondo.livestudio.domain.usecase.GetLoginStateUseCase
import id.co.mondo.livestudio.domain.usecase.GetStreamStatusUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedIn : SessionState
    data object LoggedOut : SessionState
}

@HiltViewModel
class AppViewModel @Inject constructor(
    getLoginStateUseCase: GetLoginStateUseCase,
    getStreamStatusUseCase: GetStreamStatusUseCase
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = getLoginStateUseCase()
        .map { session -> if (session != null) SessionState.LoggedIn else SessionState.LoggedOut }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionState.Loading
        )

    /** Dipakai untuk kembali ke layar status bila aplikasi dibuka ulang saat siaran masih berjalan. */
    val streamStatus: StateFlow<StreamStatus> = getStreamStatusUseCase()
}