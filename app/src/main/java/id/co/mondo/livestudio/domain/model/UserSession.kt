package id.co.mondo.livestudio.domain.model

data class UserSession(
    val facebookUserId: String,
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val facebookAccessToken: String = "",
    val tokenExpiresAt: Long = 0L
)