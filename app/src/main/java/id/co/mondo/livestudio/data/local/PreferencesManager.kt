package id.co.mondo.livestudio.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import id.co.mondo.livestudio.core.util.Constants
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFS_NAME)

/**
 * Menyimpan data non-sensitif saja (profil & expiry). Token disimpan di [SecureTokenStorage].
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureTokenStorage: SecureTokenStorage
) {

    private object PreferencesKeys {
        val FB_USER_ID = stringPreferencesKey("fb_user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
        val TOKEN_EXPIRES_AT = longPreferencesKey("token_expires_at")
        val LAST_BROADCAST_TITLE = stringPreferencesKey("last_broadcast_title")
        val LAST_PRIVACY_OPTION = stringPreferencesKey("last_privacy_option")
        val LAST_VIDEO_QUALITY = stringPreferencesKey("last_video_quality")
        val LAST_SHARE_MODE = stringPreferencesKey("last_share_mode")
        val LAST_TARGET_APP_PKG = stringPreferencesKey("last_target_app_pkg")
        val LAST_TARGET_APP_NAME = stringPreferencesKey("last_target_app_name")
    }

    val userSessionFlow: Flow<UserSession?> = context.dataStore.data.map { preferences ->
        val facebookUserId = preferences[PreferencesKeys.FB_USER_ID] ?: return@map null
        UserSession(
            facebookUserId = facebookUserId,
            userName = preferences[PreferencesKeys.USER_NAME] ?: "",
            userPhotoUrl = preferences[PreferencesKeys.USER_PHOTO_URL],
            facebookAccessToken = secureTokenStorage.readToken() ?: return@map null,
            tokenExpiresAt = preferences[PreferencesKeys.TOKEN_EXPIRES_AT] ?: 0L
        )
    }

    val lastBroadcastSettingsFlow: Flow<Pair<String, String>> = context.dataStore.data.map { preferences ->
        (preferences[PreferencesKeys.LAST_BROADCAST_TITLE] ?: "") to
            (preferences[PreferencesKeys.LAST_PRIVACY_OPTION] ?: "EVERYONE")
    }

    val lastVideoQualityFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_VIDEO_QUALITY] ?: "AUTO_720P"
    }

    val lastShareSettingsFlow: Flow<Pair<ShareModeType, TargetApp?>> = context.dataStore.data.map { preferences ->
        val mode = ShareModeType.fromName(preferences[PreferencesKeys.LAST_SHARE_MODE])
        val pkg = preferences[PreferencesKeys.LAST_TARGET_APP_PKG]
        val name = preferences[PreferencesKeys.LAST_TARGET_APP_NAME]
        val targetApp = if (!pkg.isNullOrBlank() && !name.isNullOrBlank()) {
            TargetApp(packageName = pkg, appName = name)
        } else {
            null
        }
        mode to targetApp
    }

    suspend fun saveLastVideoQuality(quality: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_VIDEO_QUALITY] = quality
        }
    }

    suspend fun saveLastShareSettings(mode: ShareModeType, targetApp: TargetApp?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SHARE_MODE] = mode.name
            if (targetApp != null) {
                preferences[PreferencesKeys.LAST_TARGET_APP_PKG] = targetApp.packageName
                preferences[PreferencesKeys.LAST_TARGET_APP_NAME] = targetApp.appName
            }
        }
    }

    suspend fun saveLastBroadcastSettings(title: String, privacy: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BROADCAST_TITLE] = title
            preferences[PreferencesKeys.LAST_PRIVACY_OPTION] = privacy
        }
    }

    suspend fun saveUserSession(userSession: UserSession) {
        secureTokenStorage.saveToken(userSession.facebookAccessToken)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FB_USER_ID] = userSession.facebookUserId
            preferences[PreferencesKeys.USER_NAME] = userSession.userName
            if (userSession.userPhotoUrl != null) {
                preferences[PreferencesKeys.USER_PHOTO_URL] = userSession.userPhotoUrl
            } else {
                preferences.remove(PreferencesKeys.USER_PHOTO_URL)
            }
            preferences[PreferencesKeys.TOKEN_EXPIRES_AT] = userSession.tokenExpiresAt
        }
    }

    suspend fun clearUserSession() {
        secureTokenStorage.clear()
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.FB_USER_ID)
            preferences.remove(PreferencesKeys.USER_NAME)
            preferences.remove(PreferencesKeys.USER_PHOTO_URL)
            preferences.remove(PreferencesKeys.TOKEN_EXPIRES_AT)
        }
    }
}
