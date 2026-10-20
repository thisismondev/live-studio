package id.co.mondo.livestudio.domain.repository

import android.app.Activity
import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getLoginState(): Flow<UserSession?>
    suspend fun loginWithFacebook(activity: Activity): Result<UserSession>
    suspend fun refreshTokenIfNeeded(): Result<UserSession>
    suspend fun logout(): Result<Unit>
}