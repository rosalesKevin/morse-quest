package morse.android.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import morse.android.audio.IAudioPlayer
import morse.android.haptics.IHapticsController
import morse.android.persistence.IProgressRepository
import morse.android.persistence.ISettingsRepository
import morse.core.TimingEngine
import morse.practice.Lesson
import morse.practice.TimeProvider
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val progressRepository: IProgressRepository,
    private val settingsRepository: ISettingsRepository,
    private val timingEngine: TimingEngine,
    private val audioPlayer: IAudioPlayer,
    private val hapticsController: IHapticsController,
    private val lessons: @JvmSuppressWildcards List<Lesson>,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    data class LessonItem(val lesson: Lesson, val isUnlocked: Boolean)

    sealed class UiState {
        data class LessonList(val lessons: List<LessonItem>) : UiState()
        data class LessonDetail(val lesson: Lesson, val isUnlocked: Boolean) : UiState()
    }

    private val _selectedLesson = MutableStateFlow<Lesson?>(null)

    val uiState: StateFlow<UiState> = combine(
        progressRepository.sessionHistory,
        _selectedLesson,
    ) { sessions, selectedLesson ->
        val tracker = progressRepository.buildTracker(sessions, lessons, timeProvider)
        val unlockedIds = tracker.getUnlockedLessons().map { it.id }.toSet()
        if (selectedLesson == null) {
            UiState.LessonList(lessons.map { LessonItem(it, it.id in unlockedIds) })
        } else {
            UiState.LessonDetail(selectedLesson, selectedLesson.id in unlockedIds)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UiState.LessonList(emptyList()),
    )

    fun selectLesson(lesson: Lesson) { _selectedLesson.value = lesson }
    fun back() { _selectedLesson.value = null }

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
