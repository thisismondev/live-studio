package id.co.mondo.livestudio.domain.model

enum class VideoQualityProfile(
    val width: Int,
    val height: Int,
    val fps: Int,
    val bitrateBps: Int,
    val label: String
) {
    AUTO_720P(720, 1280, 30, 2500 * 1024, "Otomatis (720p)"),
    SAVER_480P(480, 854, 30, 1200 * 1024, "Hemat Data (480p)"),
    HIGH_1080P(1080, 1920, 30, 4000 * 1024, "Kualitas Tinggi (1080p)");

    companion object {
        fun fromName(name: String?): VideoQualityProfile =
            entries.firstOrNull { it.name == name } ?: AUTO_720P
    }
}