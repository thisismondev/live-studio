package id.co.mondo.livestudio.domain.usecase

import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.domain.repository.LiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBroadcastSettingsUseCase @Inject constructor(
    private val liveRepository: LiveRepository
) {
    operator fun invoke(): Flow<Pair<String, PrivacyOption>> = liveRepository.getLastSettings()
}