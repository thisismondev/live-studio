package id.co.mondo.livestudio.domain.usecase

import id.co.mondo.livestudio.domain.repository.StreamRepository
import javax.inject.Inject

class ToggleAudioUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    fun toggleMic(mute: Boolean) {
        streamRepository.toggleMic(mute)
    }

    fun toggleAppAudio(mute: Boolean) {
        streamRepository.toggleAppAudio(mute)
    }
}