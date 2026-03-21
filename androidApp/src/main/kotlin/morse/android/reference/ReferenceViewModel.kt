package morse.android.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import morse.android.audio.IAudioPlayer
import morse.android.haptics.IHapticsController
import morse.android.persistence.ISettingsRepository
import morse.core.MorseAlphabet
import morse.core.TimingEngine
import javax.inject.Inject

@HiltViewModel
class ReferenceViewModel @Inject constructor(
    private val audioPlayer: IAudioPlayer,
    private val hapticsController: IHapticsController,
    private val settingsRepository: ISettingsRepository,
    private val timingEngine: TimingEngine,
) : ViewModel() {

    data class ReferenceEntry(val character: Char, val morse: String)

    data class UiState(
        val query: String = "",
        val entries: List<ReferenceEntry> = emptyList(),
    )

    private val allEntries: List<ReferenceEntry> = MorseAlphabet.characters.entries
        .sortedBy { it.key }
        .map { ReferenceEntry(it.key, it.value) }

    private val _uiState = MutableStateFlow(UiState(entries = allEntries))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        val filtered = if (query.isBlank()) allEntries
        else allEntries.filter {
            it.character.toString().contains(query, ignoreCase = true) ||
                it.morse.contains(query)
        }
        _uiState.update { it.copy(query = query, entries = filtered) }
    }

    fun playCharacter(char: Char) {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            timingEngine.setSpeeds(settings.wpm, settings.wpm)
            val signals = timingEngine.textToSignals(char.toString())
            audioPlayer.playSignals(signals, settings.toneFrequencyHz)
            if (settings.hapticsEnabled) {
                hapticsController.vibrateSignals(signals)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
