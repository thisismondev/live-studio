package id.co.mondo.livestudio.domain.usecase

import android.app.Activity
import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.domain.model.UserSession
import id.co.mondo.livestudio.domain.repository.AuthRepository
import javax.inject.Inject

class LoginFacebookUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(activity: Activity): Result<UserSession> {
        return authRepository.loginWithFacebook(activity)
    }
}
