package id.co.mondo.livestudio.domain.repository

import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.domain.model.BroadcastConfig
import id.co.mondo.livestudio.domain.model.PrivacyOption
import kotlinx.coroutines.flow.Flow

interface LiveRepository {
    suspend fun createLiveStream(title: String, privacy: PrivacyOption): Result<BroadcastConfig>
    suspend fun saveLastSettings(title: String, privacy: PrivacyOption)
    fun getLastSettings(): Flow<Pair<String, PrivacyOption>>
}