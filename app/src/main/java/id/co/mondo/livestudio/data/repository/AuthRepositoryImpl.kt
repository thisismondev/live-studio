package id.co.mondo.livestudio.data.repository

import android.app.Activity
import androidx.activity.result.ActivityResultRegistryOwner
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.data.local.PreferencesManager
import id.co.mondo.livestudio.domain.model.UserSession
import id.co.mondo.livestudio.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override fun getLoginState(): Flow<UserSession?> {
        return preferencesManager.userSessionFlow
    }

    override suspend fun loginWithFacebook(activity: Activity): Result<UserSession> {
        return try {
            val owner = activity as? ActivityResultRegistryOwner
                ?: return Result.Error(
                    IllegalArgumentException("Activity harus ActivityResultRegistryOwner."),
                    "Login gagal: Activity tidak didukung."
                )
            val accessToken = requestLogin(owner)
            val session = buildSession(accessToken)
            preferencesManager.saveUserSession(session)
            Result.Success(session)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Terjadi kesalahan saat login Facebook.")
        }
    }

    override suspend fun refreshTokenIfNeeded(): Result<UserSession> {
        return try {
            val current = preferencesManager.userSessionFlow.first()
                ?: return Result.Error(IllegalStateException("Belum login."), "Sesi tidak ditemukan, silakan login.")

            val token = AccessToken.getCurrentAccessToken()
            if (token == null || token.isExpired) {
                // Token tidak bisa diperbarui otomatis: wajib login ulang.
                val refreshed = refreshAccessToken() ?: return Result.Error(
                    IllegalStateException("Token kedaluwarsa."),
                    "Sesi Facebook berakhir, silakan login ulang."
                )
                val session = current.copy(
                    facebookAccessToken = refreshed.token,
                    tokenExpiresAt = refreshed.expires.time
                )
                preferencesManager.saveUserSession(session)
                return Result.Success(session)
            }

            if (token.expires.time == current.tokenExpiresAt) {
                Result.Success(current)
            } else {
                val session = current.copy(
                    facebookAccessToken = token.token,
                    tokenExpiresAt = token.expires.time
                )
                preferencesManager.saveUserSession(session)
                Result.Success(session)
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Gagal memperbarui token.")
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            LoginManager.getInstance().logOut()
            preferencesManager.clearUserSession()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Gagal melakukan logout.")
        }
    }

    private suspend fun requestLogin(owner: ActivityResultRegistryOwner): AccessToken =
        suspendCancellableCoroutine { cont ->
            val callbackManager = CallbackManager.Factory.create()
            val loginManager = LoginManager.getInstance()
            loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    if (cont.isActive) cont.resume(result.accessToken)
                }

                override fun onCancel() {
                    if (cont.isActive) {
                        cont.resumeWithException(IllegalStateException("Login dibatalkan."))
                    }
                }

                override fun onError(error: FacebookException) {
                    if (cont.isActive) cont.resumeWithException(error)
                }
            })
            cont.invokeOnCancellation { loginManager.unregisterCallback(callbackManager) }
            loginManager.logIn(owner, callbackManager, PERMISSIONS)
        }

    private suspend fun buildSession(token: AccessToken): UserSession =
        suspendCancellableCoroutine { cont ->
            val request = GraphRequest.newMeRequest(token) { json, response ->
                if (json == null) {
                    val msg = response?.error?.errorMessage ?: "Gagal mengambil profil Facebook."
                    if (cont.isActive) cont.resumeWithException(IllegalStateException(msg))
                    return@newMeRequest
                }
                val photoUrl = json.optJSONObject("picture")
                    ?.optJSONObject("data")
                    ?.optString("url")
                    ?.takeIf { it.isNotBlank() }
                val session = UserSession(
                    facebookUserId = json.optString("id", token.userId),
                    userName = json.optString("name").ifBlank { "Pengguna Live Studio" },
                    userPhotoUrl = photoUrl,
                    facebookAccessToken = token.token,
                    tokenExpiresAt = token.expires.time
                )
                if (cont.isActive) cont.resume(session)
            }
            request.parameters = android.os.Bundle().apply {
                putString("fields", "id,name,picture.type(large)")
            }
            request.executeAsync()
        }

    private suspend fun refreshAccessToken(): AccessToken? =
        suspendCancellableCoroutine { cont ->
            AccessToken.refreshCurrentAccessTokenAsync(object : AccessToken.AccessTokenRefreshCallback {
                override fun OnTokenRefreshed(accessToken: AccessToken?) {
                    if (cont.isActive) cont.resume(accessToken)
                }

                override fun OnTokenRefreshFailed(exception: FacebookException?) {
                    if (cont.isActive) cont.resume(null)
                }
            })
        }

    private companion object {
        val PERMISSIONS = listOf("public_profile", "publish_video")
    }
}