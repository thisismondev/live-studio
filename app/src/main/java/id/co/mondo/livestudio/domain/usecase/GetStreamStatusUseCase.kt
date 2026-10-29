package id.co.mondo.livestudio.domain.usecase

import id.co.mondo.livestudio.domain.model.StreamStatus
import id.co.mondo.livestudio.domain.repository.StreamRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetStreamStatusUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    operator fun invoke(): StateFlow<StreamStatus> = streamRepository.streamStatus
}