package id.co.mondo.livestudio.domain.usecase

import android.content.Intent
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.model.VideoQualityProfile
import id.co.mondo.livestudio.domain.repository.StreamRepository
import javax.inject.Inject

class StartStreamUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    operator fun invoke(
        resultCode: Int,
        data: Intent,
        rtmpsUrl: String,
        quality: VideoQualityProfile,
        shareMode: ShareModeType,
        targetApp: TargetApp?
    ) {
        streamRepository.startStream(resultCode, data, rtmpsUrl, quality, shareMode, targetApp)
    }
}
