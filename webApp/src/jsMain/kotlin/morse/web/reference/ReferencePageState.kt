package morse.web.reference

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import morse.core.MorseAlphabet
import morse.web.audio.WebAudioPlayer
import morse.web.persistence.WebSettingsRepository

data class ReferenceTableEntry(
    val character: Char,
    val morse: String,
)

class ReferencePageState(
    private val audioPlayer: WebAudioPlayer,
    private val settingsRepository: WebSettingsRepository,
) {
    private val allEntries = MorseAlphabet.characters.entries
        .sortedBy { it.key }
        .map { ReferenceTableEntry(character = it.key, morse = it.value) }

    var query by mutableStateOf("")
        private set

    var entries by mutableStateOf(allEntries)
        private set

    fun updateQuery(value: String) {
        query = value
        entries = if (value.isBlank()) {
            allEntries
        } else {
            allEntries.filter { entry ->
                entry.character.toString().contains(value, ignoreCase = true) ||
                    entry.morse.contains(value)
            }
        }
    }

    fun playCharacter(character: Char) {
        audioPlayer.playText(character.toString(), settingsRepository.settings)
    }
}
