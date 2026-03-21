package morse.android.persistence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository(
    initial: UserSettings = UserSettings(),
) : ISettingsRepository {
    private val _settings = MutableStateFlow(initial)
    override val settings: Flow<UserSettings> = _settings.asStateFlow()
    override suspend fun updateWpm(wpm: Int) { _settings.value = _settings.value.copy(wpm = wpm) }
    override suspend fun updateToneFrequency(hz: Float) { _settings.value = _settings.value.copy(toneFrequencyHz = hz) }
    override suspend fun updateHapticsEnabled(enabled: Boolean) { _settings.value = _settings.value.copy(hapticsEnabled = enabled) }
    override suspend fun updateThemeMode(mode: ThemeMode) { _settings.value = _settings.value.copy(themeMode = mode) }
    override suspend fun updateAudioProfile(profile: AudioProfile) { _settings.value = _settings.value.copy(audioProfile = profile) }
}
