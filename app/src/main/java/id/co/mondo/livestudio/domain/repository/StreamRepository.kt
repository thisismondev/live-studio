package id.co.mondo.livestudio.domain.repository

import android.content.Intent
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.StreamStatus
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.model.VideoQualityProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface StreamRepository {
    val streamStatus: StateFlow<StreamStatus>
    val pickAppRequested: StateFlow<Boolean>
    fun startStream(
        resultCode: Int,
        data: Intent,
        rtmpsUrl: String,
        quality: VideoQualityProfile,
        shareMode: ShareModeType,
        targetApp: TargetApp?
    )
    fun changeShareTarget(shareMode: ShareModeType, targetApp: TargetApp?)
    fun updateProjection(resultCode: Int, data: Intent)
    fun stopStream()
    fun toggleMic(mute: Boolean)
    fun toggleAppAudio(mute: Boolean)
    fun consumePickerRequest()
    fun resetStatus()
    suspend fun saveQualityProfile(quality: VideoQualityProfile)
    fun getQualityProfile(): Flow<VideoQualityProfile>
    suspend fun saveShareSettings(mode: ShareModeType, targetApp: TargetApp?)
    fun getShareSettings(): Flow<Pair<ShareModeType, TargetApp?>>
}
