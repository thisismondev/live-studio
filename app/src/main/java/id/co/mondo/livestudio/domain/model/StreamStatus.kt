package id.co.mondo.livestudio.domain.model

sealed interface StreamStatus {
    data object Idle : StreamStatus
    data object Connecting : StreamStatus
    data class Streaming(
        val durationSeconds: Long = 0L,
        val bitrateKbps: Int = 0,
        val isMicMuted: Boolean = false,
        val isAppAudioMuted: Boolean = false,
        val shareMode: ShareModeType = ShareModeType.ENTIRE_SCREEN,
        val targetApp: TargetApp? = null
    ) : StreamStatus

    /** Siaran Facebook tetap hidup, tetapi berbagi layar dijeda (penonton melihat layar jeda). */
    data class ScreenPaused(
        val durationSeconds: Long = 0L,
        val bitrateKbps: Int = 0,
        val isMicMuted: Boolean = false,
        val isAppAudioMuted: Boolean = false,
        val shareMode: ShareModeType = ShareModeType.ENTIRE_SCREEN,
        val targetApp: TargetApp? = null
    ) : StreamStatus

    data class Reconnecting(val attempt: Int, val maxAttempts: Int = 5) : StreamStatus
    data class Stopped(val summaryDurationSeconds: Long) : StreamStatus
    data class Error(val message: String) : StreamStatus
}

/** Siaran sedang berjalan atau sedang berusaha berjalan (service masih aktif). */
fun StreamStatus.isActive(): Boolean = when (this) {
    is StreamStatus.Connecting,
    is StreamStatus.Streaming,
    is StreamStatus.ScreenPaused,
    is StreamStatus.Reconnecting -> true
    else -> false
}
