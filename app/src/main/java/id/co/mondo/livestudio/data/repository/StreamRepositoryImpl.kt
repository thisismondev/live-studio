package id.co.mondo.livestudio.data.repository

import android.content.Intent
import id.co.mondo.livestudio.data.local.PreferencesManager
import id.co.mondo.livestudio.data.streaming.StreamEngineController
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.StreamStatus
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.model.VideoQualityProfile
import id.co.mondo.livestudio.domain.repository.StreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepositoryImpl @Inject constructor(
    private val streamEngineController: StreamEngineController,
    private val preferencesManager: PreferencesManager
) : StreamRepository {

    override val streamStatus: StateFlow<StreamStatus> = streamEngineController.streamStatus
    override val pickAppRequested: StateFlow<Boolean> = streamEngineController.pickAppRequested

    override fun startStream(
        resultCode: Int,
        data: Intent,
        rtmpsUrl: String,
        quality: VideoQualityProfile,
        shareMode: ShareModeType,
        targetApp: TargetApp?
    ) {
        streamEngineController.startStream(resultCode, data, rtmpsUrl, quality, shareMode, targetApp)
    }

    override fun changeShareTarget(shareMode: ShareModeType, targetApp: TargetApp?) {
        streamEngineController.changeShareTarget(shareMode, targetApp)
    }

    override fun updateProjection(resultCode: Int, data: Intent) {
        streamEngineController.updateProjection(resultCode, data)
    }

    override fun stopStream() {
        streamEngineController.stopStream()
    }

    override fun toggleMic(mute: Boolean) {
        streamEngineController.toggleMic(mute)
    }

    override fun toggleAppAudio(mute: Boolean) {
        streamEngineController.toggleAppAudio(mute)
    }

    override fun consumePickerRequest() {
        streamEngineController.consumePickerRequest()
    }

    override fun resetStatus() {
        streamEngineController.resetStatus()
    }

    override suspend fun saveQualityProfile(quality: VideoQualityProfile) {
        preferencesManager.saveLastVideoQuality(quality.name)
    }

    override fun getQualityProfile(): Flow<VideoQualityProfile> {
        return preferencesManager.lastVideoQualityFlow.map { name ->
            VideoQualityProfile.fromName(name)
        }
    }

    override suspend fun saveShareSettings(mode: ShareModeType, targetApp: TargetApp?) {
        preferencesManager.saveLastShareSettings(mode, targetApp)
    }

    override fun getShareSettings(): Flow<Pair<ShareModeType, TargetApp?>> {
        return preferencesManager.lastShareSettingsFlow
    }
}
