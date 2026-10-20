package id.co.mondo.livestudio.domain.usecase

import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
