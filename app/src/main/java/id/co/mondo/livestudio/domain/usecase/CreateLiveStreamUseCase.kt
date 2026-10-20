package id.co.mondo.livestudio.domain.usecase

import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.domain.model.BroadcastConfig
import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.domain.repository.LiveRepository
import javax.inject.Inject

class CreateLiveStreamUseCase @Inject constructor(
    private val liveRepository: LiveRepository
) {
    suspend operator fun invoke(title: String, privacy: PrivacyOption): Result<BroadcastConfig> {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            return Result.Error(
                IllegalArgumentException("Judul siaran kosong."),
                "Judul siaran wajib diisi."
            )
        }
        return liveRepository.createLiveStream(cleanTitle, privacy)
    }
}