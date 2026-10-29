package id.co.mondo.livestudio.domain.model

/**
 * Mode penyiaran layar yang dipilih oleh host sebelum memulai siaran.
 */
enum class ShareModeType {
    /** Menyiarkan seluruh tampilan layar perangkat secara terus-menerus. */
    ENTIRE_SCREEN,

    /** Menyiarkan hanya aplikasi tertentu dengan sensor jeda privasi otomatis. */
    SPECIFIC_APP;

    companion object {
        fun fromName(name: String?): ShareModeType =
            entries.firstOrNull { it.name == name } ?: ENTIRE_SCREEN
    }
}

/**
 * Informasi aplikasi target yang dipilih untuk dibagikan pada mode [ShareModeType.SPECIFIC_APP].
 */
data class TargetApp(
    val packageName: String,
    val appName: String
)
