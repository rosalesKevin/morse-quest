package morse.android.practice

import androidx.lifecycle.SavedStateHandle

enum class QuickStartDifficulty(val routeValue: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    companion object {
        fun fromRouteValue(value: String?): QuickStartDifficulty =
            entries.firstOrNull { it.routeValue == value } ?: MEDIUM
    }
}

sealed class PracticeLaunchConfig {
    abstract fun toRoute(): String

    data class Lesson(val lessonId: String) : PracticeLaunchConfig() {
        override fun toRoute(): String = "practice?mode=lesson&lessonId=$lessonId"
    }

    data class QuickStart(
        val difficulty: QuickStartDifficulty,
        val wpmOverride: Int? = null,
    ) : PracticeLaunchConfig() {
        override fun toRoute(): String {
            val base = "practice?mode=quick_start&difficulty=${difficulty.routeValue}"
            return if (wpmOverride != null) "$base&wpm=$wpmOverride" else base
        }
    }

    companion object {
        fun lesson(lessonId: String): PracticeLaunchConfig = Lesson(lessonId)

        fun quickStart(
            difficulty: QuickStartDifficulty,
            wpmOverride: Int? = null,
        ): PracticeLaunchConfig = QuickStart(difficulty, wpmOverride)

        fun fromSavedState(savedStateHandle: SavedStateHandle): PracticeLaunchConfig {
            val mode = savedStateHandle.get<String>("mode")
            return if (mode == "quick_start") {
                val wpmOverride = savedStateHandle.get<String?>("wpm")?.toIntOrNull()
                    ?: savedStateHandle.get<Int?>("wpm")
                QuickStart(
                    difficulty = QuickStartDifficulty.fromRouteValue(savedStateHandle["difficulty"]),
                    wpmOverride = wpmOverride,
                )
            } else {
                Lesson(lessonId = checkNotNull(savedStateHandle["lessonId"]))
            }
        }
    }
}
