package id.co.mondo.livestudio.domain.model

data class BroadcastConfig(
    val title: String,
    val privacy: PrivacyOption,
    val rtmpsUrl: String,
    val liveVideoId: String
)