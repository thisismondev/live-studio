package id.co.mondo.livestudio.ui.screens.home

import id.co.mondo.livestudio.domain.model.BroadcastConfig
import id.co.mondo.livestudio.domain.model.PrivacyOption

data class HomeUiState(
    val title: String = "",
    val privacy: PrivacyOption = PrivacyOption.PUBLIC,
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val isLoading: Boolean = false,
    val titleError: Boolean = false,
    val errorMessage: String? = null,
    val showRequirementSheet: Boolean = false,
    val createdBroadcast: BroadcastConfig? = null
)