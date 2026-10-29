package id.co.mondo.livestudio.domain.usecase

import id.co.mondo.livestudio.domain.repository.StreamRepository
import javax.inject.Inject

class StopStreamUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    operator fun invoke() {
        streamRepository.stopStream()
    }
}