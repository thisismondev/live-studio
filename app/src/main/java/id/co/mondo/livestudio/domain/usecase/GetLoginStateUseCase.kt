package id.co.mondo.livestudio.domain.usecase

import id.co.mondo.livestudio.domain.model.UserSession
import id.co.mondo.livestudio.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoginStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<UserSession?> {
        return authRepository.getLoginState()
    }
}
