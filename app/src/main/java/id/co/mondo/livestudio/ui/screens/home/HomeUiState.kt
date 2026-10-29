package id.co.mondo.livestudio.ui.screens.home

import id.co.mondo.livestudio.domain.model.BroadcastConfig
import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.model.VideoQualityProfile

data class HomeUiState(
    val title: String = "",
    val privacy: PrivacyOption = PrivacyOption.PUBLIC,
    val quality: VideoQualityProfile = VideoQualityProfile.AUTO_720P,
    val shareMode: ShareModeType = ShareModeType.SPECIFIC_APP,
    val selectedApp: TargetApp? = null,
    val installedApps: List<TargetApp> = emptyList(),
    val showAppPicker: Boolean = false,
    val targetAppError: Boolean = false,
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val isLoading: Boolean = false,
    val titleError: Boolean = false,
    val errorMessage: String? = null,
    val showRequirementSheet: Boolean = false,
    val pendingBroadcast: BroadcastConfig? = null
)
