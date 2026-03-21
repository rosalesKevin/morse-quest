package morse.android.practice

import androidx.lifecycle.SavedStateHandle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PracticeLaunchConfigTest {

    @Test
    fun `lesson route preserves lesson id`() {
        val route = PracticeLaunchConfig.lesson("lesson-4").toRoute()

        assertEquals(
            "practice?mode=lesson&lessonId=lesson-4",
            route,
        )
    }

    @Test
    fun `quick start route preserves difficulty and wpm override`() {
        val route = PracticeLaunchConfig.quickStart(
            difficulty = QuickStartDifficulty.HARD,
            wpmOverride = 12,
        ).toRoute()

        assertEquals(
            "practice?mode=quick_start&difficulty=hard&wpm=12",
            route,
        )
    }

    @Test
    fun `quick start saved state parses optional wpm string`() {
        val config = PracticeLaunchConfig.fromSavedState(
            SavedStateHandle(
                mapOf(
                    "mode" to "quick_start",
                    "difficulty" to "easy",
                    "wpm" to "12",
                ),
            ),
        )

        assertIs<PracticeLaunchConfig.QuickStart>(config)
        assertEquals(QuickStartDifficulty.EASY, config.difficulty)
        assertEquals(12, config.wpmOverride)
    }
}
