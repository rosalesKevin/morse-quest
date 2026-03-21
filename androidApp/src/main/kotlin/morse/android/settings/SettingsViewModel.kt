package morse.android.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import morse.android.persistence.AudioProfile
import morse.android.persistence.IProgressRepository
import morse.android.persistence.ISettingsRepository
import morse.android.persistence.ThemeMode
import morse.android.persistence.UserSettings
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: ISettingsRepository,
    private val progressRepository: IProgressRepository,
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    fun updateWpm(wpm: Int) = viewModelScope.launch { settingsRepository.updateWpm(wpm) }
    fun updateToneFrequency(hz: Float) = viewModelScope.launch { settingsRepository.updateToneFrequency(hz) }
    fun updateHapticsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateHapticsEnabled(enabled) }
    fun updateThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.updateThemeMode(mode) }
    fun updateAudioProfile(profile: AudioProfile) = viewModelScope.launch { settingsRepository.updateAudioProfile(profile) }
    fun resetProgress() = viewModelScope.launch { progressRepository.clearAll() }
}
