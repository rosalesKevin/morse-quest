package morse.android.quest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import morse.android.audio.IAudioPlayer
import morse.android.persistence.ISettingsRepository
import morse.android.practice.TouchpadState
import morse.core.MorseDecoder
import morse.core.TimingEngine
import morse.practice.Exercise
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyQuestViewModel @Inject constructor(
    private val repository: IDailyQuestRepository,
    private val generator: DailyQuestGenerator,
    private val settingsRepository: ISettingsRepository,
    private val audioPlayer: IAudioPlayer,
    private val timingEngine: TimingEngine,
) : ViewModel() {

    data class UiState(
        val quest: DailyQuest? = null,
        val isCompleted: Boolean = false,
        val selectedDifficulty: DailyQuestDifficulty = DailyQuestDifficulty.EASY,
        val progressIndex: Int = 0,
        val correctCount: Int = 0,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    var touchpadState: TouchpadState? by mutableStateOf(null)
        private set

    init {
        viewModelScope.launch {
            val savedDifficulty = repository.progressTodayDifficulty() ?: DailyQuestDifficulty.EASY
            val savedIndex      = repository.progressTodayIndex()
            val isCompleted     = repository.isCompletedToday()

            _uiState.value = UiState(
                quest = null,
                isCompleted = isCompleted,
                selectedDifficulty = savedDifficulty,
                progressIndex = savedIndex,
            )

            val quest = generator.generate(LocalDate.now(), savedDifficulty)

            _uiState.value = _uiState.value.copy(quest = quest)
        }
    }

    fun refreshTouchpadForQuestion(index: Int) {
        val exercise = _uiState.value.quest?.questions?.getOrNull(index)
        touchpadState = when (exercise) {
            is Exercise.ReadAndTap, is Exercise.EncodeWord -> TouchpadState(timingEngine)
            else -> null
        }
    }

    fun onDifficultySelected(difficulty: DailyQuestDifficulty) {
        viewModelScope.launch {
            val restoredIndex = if (repository.progressTodayDifficulty() == difficulty)
                repository.progressTodayIndex()
            else 0

            val quest = generator.generate(LocalDate.now(), difficulty)

            _uiState.value = _uiState.value.copy(
                quest = quest,
                selectedDifficulty = difficulty,
                progressIndex = restoredIndex,
            )
        }
    }

    fun onQuestionAnswered(index: Int, correct: Boolean) {
        viewModelScope.launch {
            repository.saveProgress(index, _uiState.value.selectedDifficulty)
            _uiState.value = _uiState.value.copy(
                progressIndex = index,
                correctCount = _uiState.value.correctCount + if (correct) 1 else 0,
            )
        }
    }

    fun onQuestCompleted() {
        viewModelScope.launch {
            repository.markCompletedToday()
            _uiState.value = _uiState.value.copy(isCompleted = true)
        }
    }

    fun playCurrentExercise(questIndex: Int) {
        val quest = _uiState.value.quest ?: return
        val exercise = quest.questions.getOrNull(questIndex) ?: return
        val text: String = when (exercise) {
            is Exercise.ListenAndIdentify -> MorseDecoder.decode(exercise.morse) ?: return
            is Exercise.DecodeWord        -> MorseDecoder.decode(exercise.morse) ?: return
            else                          -> return
        }
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            timingEngine.setSpeeds(settings.wpm, settings.wpm)
            val signals = timingEngine.textToSignals(text)
            audioPlayer.playSignals(signals, settings.toneFrequencyHz)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
