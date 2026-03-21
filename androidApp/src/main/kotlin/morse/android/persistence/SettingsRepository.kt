package morse.android.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ISettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun updateWpm(wpm: Int)
    suspend fun updateToneFrequency(hz: Float)
    suspend fun updateHapticsEnabled(enabled: Boolean)
    suspend fun updateThemeMode(mode: ThemeMode)
    suspend fun updateAudioProfile(profile: AudioProfile)
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ISettingsRepository {

    private object Keys {
        val WPM = intPreferencesKey("wpm")
        val TONE_HZ = floatPreferencesKey("tone_frequency_hz")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUDIO_PROFILE = stringPreferencesKey("audio_profile")
    }

    override val settings: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            wpm = prefs[Keys.WPM] ?: 20,
            toneFrequencyHz = prefs[Keys.TONE_HZ] ?: 700f,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            themeMode = ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name),
            audioProfile = AudioProfile.valueOf(prefs[Keys.AUDIO_PROFILE] ?: AudioProfile.PURE.name)
        )
    }

    override suspend fun updateWpm(wpm: Int) {
        dataStore.edit { it[Keys.WPM] = wpm.coerceIn(5, 40) }
    }

    override suspend fun updateToneFrequency(hz: Float) {
        dataStore.edit { it[Keys.TONE_HZ] = hz }
    }

    override suspend fun updateHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTICS] = enabled }
    }
    
    override suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun updateAudioProfile(profile: AudioProfile) {
        dataStore.edit { it[Keys.AUDIO_PROFILE] = profile.name }
    }
}
