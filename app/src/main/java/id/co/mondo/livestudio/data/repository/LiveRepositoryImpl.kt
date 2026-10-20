package id.co.mondo.livestudio.data.repository

import id.co.mondo.livestudio.core.util.Result
import id.co.mondo.livestudio.data.local.PreferencesManager
import id.co.mondo.livestudio.data.remote.FacebookLiveApi
import id.co.mondo.livestudio.domain.model.BroadcastConfig
import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.domain.repository.LiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveRepositoryImpl @Inject constructor(
    private val facebookLiveApi: FacebookLiveApi,
    private val preferencesManager: PreferencesManager
) : LiveRepository {

    override suspend fun createLiveStream(
        title: String,
        privacy: PrivacyOption
    ): Result<BroadcastConfig> {
        return when (val result = facebookLiveApi.createLiveVideo(title, privacy)) {
            is Result.Success -> Result.Success(
                BroadcastConfig(
                    title = title,
                    privacy = privacy,
                    rtmpsUrl = result.data.first,
                    liveVideoId = result.data.second
                )
            )
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun saveLastSettings(title: String, privacy: PrivacyOption) {
        preferencesManager.saveLastBroadcastSettings(title, privacy.apiValue)
    }

    override fun getLastSettings(): Flow<Pair<String, PrivacyOption>> {
        return preferencesManager.lastBroadcastSettingsFlow.map { (title, privacy) ->
            title to PrivacyOption.fromApiValue(privacy)
        }
    }
}