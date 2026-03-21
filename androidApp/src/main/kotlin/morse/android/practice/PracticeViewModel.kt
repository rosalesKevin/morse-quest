package morse.android.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import morse.android.audio.IAudioPlayer
import morse.android.haptics.IHapticsController
import morse.android.persistence.IProgressRepository
import morse.android.persistence.ISettingsRepository
import morse.core.TimingEngine
import morse.practice.Exercise
import morse.practice.ExerciseResult
import morse.practice.Lesson
import morse.practice.MistakeRecord
import morse.practice.PracticeSession
import morse.practice.SessionScore
import morse.practice.TimeProvider
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progressRepository: IProgressRepository,
    private val settingsRepository: ISettingsRepository,
    private val timingEngine: TimingEngine,
    private val audioPlayer: IAudioPlayer,
    private val hapticsController: IHapticsController,
    private val lessons: @JvmSuppressWildcards List<Lesson>,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])
    private val lesson: Lesson = lessons.first { it.id == lessonId }
    private val session = PracticeSession(lesson)
    private var currentIndex = 0
    private var pendingAnswer = ""

    sealed class UiState {
        data class Exercise(
            val exercise: morse.practice.Exercise,
            val index: Int,
            val total: Int,
            val answer: String = "",
            val result: ExerciseResult? = null,
        ) : UiState()

        data class Summary(
            val lesson: Lesson,
            val score: SessionScore,
            val mistakes: List<MistakeRecord>,
        ) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(exerciseState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        autoPlayIfAudioExercise()
    }

    fun updateAnswer(answer: String) {
        pendingAnswer = answer
    }

    fun submitAnswer() {
        val current = _uiState.value as? UiState.Exercise ?: return
        val answer = pendingAnswer
        val result = session.submitAnswer(current.exercise, answer)
        _uiState.value = current.copy(answer = answer, result = result)
    }

    fun nextExercise() {
        pendingAnswer = ""
        currentIndex++
        if (currentIndex >= session.exercises.size) {
            val score = session.getScore()
            val mistakes = session.getMistakes()
            viewModelScope.launch {
                progressRepository.recordSession(lesson, score, mistakes, timeProvider.currentEpochMillis())
            }
            _uiState.value = UiState.Summary(lesson, score, mistakes)
        } else {
            _uiState.value = exerciseState()
            autoPlayIfAudioExercise()
        }
    }

    fun playCurrentExercise() {
        val current = _uiState.value as? UiState.Exercise ?: return
        val text = audioTextFor(current.exercise) ?: return
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            timingEngine.setSpeeds(settings.wpm, settings.wpm)
            val signals = timingEngine.textToSignals(text)
            audioPlayer.playSignals(signals, settings.toneFrequencyHz)
            if (settings.hapticsEnabled) hapticsController.vibrateSignals(signals)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }

    private fun exerciseState(): UiState.Exercise = UiState.Exercise(
        exercise = session.exercises[currentIndex],
        index = currentIndex,
        total = session.exercises.size,
    )

    private fun autoPlayIfAudioExercise() {
        val current = _uiState.value as? UiState.Exercise ?: return
        if (current.exercise is Exercise.ListenAndIdentify || current.exercise is Exercise.DecodeWord) {
            playCurrentExercise()
        }
    }

    private fun audioTextFor(exercise: Exercise): String? = when (exercise) {
        is Exercise.ListenAndIdentify -> exercise.morse
        is Exercise.DecodeWord -> exercise.morse
        is Exercise.SpeedChallenge -> exercise.text
        is Exercise.ReadAndTap, is Exercise.EncodeWord -> null
    }
}
