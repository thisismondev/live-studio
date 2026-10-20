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
import id.co.mondo.livestudio.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
            preferences.clear()
        }
    }
}