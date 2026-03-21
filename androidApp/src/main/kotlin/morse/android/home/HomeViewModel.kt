package morse.android.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import morse.android.persistence.IProgressRepository
import morse.practice.Lesson
import morse.practice.TimeProvider
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val progressRepository: IProgressRepository,
    private val lessons: @JvmSuppressWildcards List<Lesson>,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    data class UiState(
        val streakDays: Int = 0,
        val overallAccuracy: Double = 0.0,
        val unlockedLessonCount: Int = 1,
        val totalLessons: Int = 0,
        val bestWpm: Int = 0,
        val bestAccuracy: Double = 0.0,
        val longestStreakDays: Int = 0,
        val focusCharacters: List<Char> = emptyList(),
        val quickPracticeLessonId: String = "",
    )

    val uiState: StateFlow<UiState> = progressRepository.sessionHistory
        .map { sessions ->
            val tracker = progressRepository.buildTracker(sessions, lessons, timeProvider)
            val summary = HomeSummaryCalculator.build(sessions, lessons, timeProvider)
            UiState(
                streakDays = tracker.getStreakDays(),
                overallAccuracy = tracker.getOverallAccuracy(),
                unlockedLessonCount = tracker.getUnlockedLessons().size,
                totalLessons = lessons.size,
                bestWpm = summary.bestWpm,
                bestAccuracy = summary.bestAccuracy,
                longestStreakDays = summary.longestStreakDays,
                focusCharacters = summary.focusCharacters,
                quickPracticeLessonId = summary.quickPracticeLessonId,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UiState(totalLessons = lessons.size),
        )
}
