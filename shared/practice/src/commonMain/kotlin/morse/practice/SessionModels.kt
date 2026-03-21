package morse.practice

data class ExerciseResult(
    val isCorrect: Boolean,
    val expectedAnswer: String,
    val expectedText: String,
    val normalizedAnswer: String,
    val mistakeCharacters: List<Char>,
)

data class SessionScore(
    val correct: Int,
    val total: Int,
    val wpm: Double,
) {
    val accuracy: Double
        get() = if (total == 0) 0.0 else (correct.toDouble() / total.toDouble()) * 100.0
}

data class MistakeRecord(
    val character: Char,
    val count: Int,
)
