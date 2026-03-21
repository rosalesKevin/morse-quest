package morse.web.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import morse.practice.Lesson
import morse.web.audio.WebAudioPlayer
import morse.web.persistence.WebProgressRepository
import morse.web.persistence.WebSettingsRepository

data class HomeStats(
    val streakDays: Int,
    val unlockedLessons: Int,
    val totalLessons: Int,
    val overallAccuracy: Double,
)

class HomePageState(
    private val lessons: List<Lesson>,
    private val progressRepository: WebProgressRepository,
    private val settingsRepository: WebSettingsRepository,
    private val audioPlayer: WebAudioPlayer,
) {
    val quickDemoText: String = "SOS"

    var stats by mutableStateOf(snapshot())
        private set

    fun refresh() {
        stats = snapshot()
    }

    fun playQuickDemo() {
        audioPlayer.playText(quickDemoText, settingsRepository.settings)
    }

    private fun snapshot(): HomeStats {
        val tracker = progressRepository.buildTracker()
        return HomeStats(
            streakDays = tracker.getStreakDays(),
            unlockedLessons = tracker.getUnlockedLessons().size,
            totalLessons = lessons.size,
            overallAccuracy = tracker.getOverallAccuracy(),
        )
    }
}
