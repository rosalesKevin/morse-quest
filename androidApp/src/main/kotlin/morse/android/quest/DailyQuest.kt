package morse.android.quest

import morse.practice.Exercise
import java.time.LocalDate

data class DailyQuest(
    val date: LocalDate,
    val difficulty: DailyQuestDifficulty,
    val questions: List<Exercise>,
    val estimatedMinutes: Int,
    val exerciseSummary: Map<ExerciseKind, Int>,
    val characters: List<Char>,
)
