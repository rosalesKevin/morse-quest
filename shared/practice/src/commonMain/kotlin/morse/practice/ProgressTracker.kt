package morse.practice

class ProgressTracker(
    private val timeProvider: TimeProvider,
    private val lessons: List<Lesson> = LessonCatalog.defaultLessons(),
) {
    private val sessions = mutableListOf<RecordedSession>()

    fun recordSession(
        lesson: Lesson,
        score: SessionScore,
        mistakes: List<MistakeRecord>,
        recordedAtEpochMillis: Long = timeProvider.currentEpochMillis(),
    ) {
        sessions += RecordedSession(
            lesson = lesson,
            score = score,
            mistakes = mistakes,
            recordedAtEpochMillis = recordedAtEpochMillis,
        )
    }

    fun getUnlockedLessons(): List<Lesson> {
        if (lessons.isEmpty()) {
            return emptyList()
        }

        val unlocked = mutableListOf(lessons.first())
        for (index in 1 until lessons.size) {
            val previousLesson = lessons[index - 1]
            if (isLessonUnlocked(index, previousLesson)) {
                unlocked += lessons[index]
            } else {
                break
            }
        }

        return unlocked
    }

    fun getWeakCharacters(): List<Char> {
        val attemptsByCharacter = mutableMapOf<Char, Int>()
        val errorsByCharacter = mutableMapOf<Char, Int>()

        sessions.forEach { session ->
            characterAttempts(session.lesson).forEach { (character, attempts) ->
                attemptsByCharacter[character] = (attemptsByCharacter[character] ?: 0) + attempts
            }
            session.mistakes.forEach { mistake ->
                errorsByCharacter[mistake.character] = (errorsByCharacter[mistake.character] ?: 0) + mistake.count
            }
        }

        return attemptsByCharacter
            .keys
            .sorted()
            .filter { character ->
                val attempts = attemptsByCharacter[character] ?: 0
                val errors = errorsByCharacter[character] ?: 0
                attempts > 0 && errors.toDouble() / attempts.toDouble() > 0.2
            }
    }

    fun getStreakDays(): Int {
        val today = epochDay(timeProvider.currentEpochMillis())
        val sessionDays = sessions.map { epochDay(it.recordedAtEpochMillis) }.distinct().sorted()
        if (sessionDays.isEmpty() || sessionDays.last() != today) {
            return 0
        }

        var streak = 0
        var expectedDay = today
        for (day in sessionDays.asReversed()) {
            if (day != expectedDay) {
                break
            }
            streak++
            expectedDay--
        }

        return streak
    }

    fun getOverallAccuracy(): Double {
        val totalCorrect = sessions.sumOf { it.score.correct }
        val totalAttempts = sessions.sumOf { it.score.total }
        return if (totalAttempts == 0) 0.0 else (totalCorrect.toDouble() / totalAttempts.toDouble()) * 100.0
    }

    private fun isLessonUnlocked(index: Int, previousLesson: Lesson): Boolean {
        val previousScores = sessions
            .filter { it.lesson.id == previousLesson.id }
            .map { it.score }

        return if (index == 1) {
            previousScores.any { it.accuracy >= 90.0 } || previousScores.count { it.accuracy >= 80.0 } >= 2
        } else {
            previousScores.count { it.accuracy >= 80.0 } >= 3
        }
    }

    private fun characterAttempts(lesson: Lesson): Map<Char, Int> {
        val counts = mutableMapOf<Char, Int>()
        lesson.exercises.forEach { exercise ->
            expectedCharacters(exercise).forEach { character ->
                counts[character] = (counts[character] ?: 0) + 1
            }
        }
        return counts
    }

    private fun expectedCharacters(exercise: Exercise): List<Char> = when (exercise) {
        is Exercise.ListenAndIdentify -> listOf(exercise.answer)
        is Exercise.ReadAndTap -> listOf(exercise.character)
        is Exercise.DecodeWord -> exercise.answer.uppercase().toList()
        is Exercise.EncodeWord -> exercise.word.uppercase().toList()
        is Exercise.SpeedChallenge -> exercise.text.uppercase().toList()
    }

    private fun epochDay(epochMillis: Long): Long = epochMillis / DAY_MILLIS

    private data class RecordedSession(
        val lesson: Lesson,
        val score: SessionScore,
        val mistakes: List<MistakeRecord>,
        val recordedAtEpochMillis: Long,
    )

    private companion object {
        private const val DAY_MILLIS = 86_400_000L
    }
}
