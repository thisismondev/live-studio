package id.co.mondo.livestudio.domain.usecase

import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.domain.repository.LiveRepository
import javax.inject.Inject

class SaveBroadcastSettingsUseCase @Inject constructor(
    private val liveRepository: LiveRepository
) {
    suspend operator fun invoke(title: String, privacy: PrivacyOption) {
        liveRepository.saveLastSettings(title, privacy)
    }
}