package id.co.mondo.livestudio.domain.model

enum class PrivacyOption(val apiValue: String, val label: String) {
    PUBLIC("EVERYONE", "Publik"),
    FRIENDS("ALL_FRIENDS", "Teman");

    companion object {
        fun fromApiValue(value: String?): PrivacyOption =
            entries.firstOrNull { it.apiValue == value } ?: PUBLIC
    }
}