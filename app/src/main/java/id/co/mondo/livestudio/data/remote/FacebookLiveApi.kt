package id.co.mondo.livestudio.data.remote

import android.os.Bundle
import com.facebook.AccessToken
import com.facebook.FacebookRequestError
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.domain.model.AccountRequirementException
import id.co.mondo.livestudio.domain.model.LiveApiException
import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.domain.model.SessionExpiredException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Pembungkus Facebook Live Video API (`POST /me/live_videos`) memakai [GraphRequest].
 * Hasil sukses: pasangan (secure_stream_url, live_video_id).
 */
@Singleton
class FacebookLiveApi @Inject constructor() {

    suspend fun createLiveVideo(
        title: String,
        privacy: PrivacyOption
    ): Result<Pair<String, String>> = suspendCancellableCoroutine { cont ->
        val token = AccessToken.getCurrentAccessToken()
        if (token == null || token.isExpired) {
            cont.resume(
                Result.Error(
                    SessionExpiredException("Token Facebook tidak tersedia atau kedaluwarsa."),
                    "Sesi Facebook berakhir. Silakan login ulang."
                )
            )
            return@suspendCancellableCoroutine
        }

        val params = Bundle().apply {
            putString("title", title)
            putString("description", title)
            putString("privacy", JSONObject().put("value", privacy.apiValue).toString())
            putString("status", "LIVE_NOW")
        }

        val request = GraphRequest(
            token,
            "me/live_videos",
            params,
            HttpMethod.POST,
            GraphRequest.Callback { response ->
                val error = response.error
                val json = response.jsonObject
                val result: Result<Pair<String, String>> = when {
                    error != null -> mapError(error)
                    json == null -> Result.Error(
                        LiveApiException("Respons kosong"),
                        "Facebook tidak mengembalikan data siaran."
                    )
                    else -> {
                        val url = json.optString("secure_stream_url")
                        val id = json.optString("id")
                        if (url.isBlank() || id.isBlank()) {
                            Result.Error(
                                LiveApiException("secure_stream_url/id kosong"),
                                "Facebook tidak mengembalikan URL siaran."
                            )
                        } else {
                            Result.Success(url to id)
                        }
                    }
                }
                if (cont.isActive) cont.resume(result)
            }
        )
        request.executeAsync()
    }

    private fun mapError(error: FacebookRequestError): Result.Error {
        val message = error.errorUserMessage
            ?: error.errorMessage
            ?: "Gagal membuat siaran di Facebook."
        val looksLikeRequirement = REQUIREMENT_KEYWORDS.any { message.contains(it, ignoreCase = true) }
        return when {
            error.errorCode == CODE_INVALID_TOKEN ->
                Result.Error(SessionExpiredException(message), "Sesi Facebook berakhir. Silakan login ulang.")

            error.errorCode in REQUIREMENT_CODES || looksLikeRequirement ->
                Result.Error(AccountRequirementException(message), message)

            else -> Result.Error(LiveApiException(message), message)
        }
    }

    private companion object {
        const val CODE_INVALID_TOKEN = 190
        val REQUIREMENT_CODES = setOf(3, 10, 200, 368)
        val REQUIREMENT_KEYWORDS = listOf("eligible", "professional", "followers", "not allowed to live")
    }
}