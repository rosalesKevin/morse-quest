package morse.practice

import morse.core.MorseDecoder

class PracticeSession(
    val lesson: Lesson,
) {
    val exercises: List<Exercise> = lesson.exercises

    private val results = mutableListOf<ExerciseResult>()
    private val speedTargets = mutableListOf<Int>()

    fun submitAnswer(exercise: Exercise, answer: String): ExerciseResult {
        require(exercise in exercises) { "Exercise is not part of this lesson" }

        val result = evaluate(exercise, answer)
        results += result

        if (exercise is Exercise.SpeedChallenge) {
            speedTargets += exercise.targetWpm
        }

        return result
    }

    fun getScore(): SessionScore {
        val averageWpm = if (speedTargets.isEmpty()) {
            0.0
        } else {
            speedTargets.average()
        }

        return SessionScore(
            correct = results.count { it.isCorrect },
            total = results.size,
            wpm = averageWpm,
        )
    }

    fun getMistakes(): List<MistakeRecord> {
        return results
            .flatMap { result -> result.mistakeCharacters }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedBy { it.first }
            .map { (character, count) -> MistakeRecord(character, count) }
    }

    private fun evaluate(exercise: Exercise, answer: String): ExerciseResult {
        val expectedText = expectedText(exercise)
        val expectedAnswer = expectedAnswer(exercise)
        val normalizedAnswer = normalizeAnswer(exercise, answer)
        val isCorrect = when (exercise) {
            is Exercise.ListenAndIdentify,
            is Exercise.DecodeWord,
            is Exercise.SpeedChallenge,
            -> normalizedAnswer == expectedText

            is Exercise.ReadAndTap,
            is Exercise.EncodeWord,
            -> normalizedAnswer == expectedAnswer
        }

        return ExerciseResult(
            isCorrect = isCorrect,
            expectedAnswer = expectedAnswer,
            expectedText = expectedText,
            normalizedAnswer = normalizedAnswer,
            mistakeCharacters = if (isCorrect) emptyList() else expectedCharacters(exercise),
        )
    }

    private fun expectedText(exercise: Exercise): String = when (exercise) {
        is Exercise.ListenAndIdentify -> exercise.answer.toString()
        is Exercise.ReadAndTap -> exercise.character.toString()
        is Exercise.DecodeWord -> exercise.answer.uppercase()
        is Exercise.EncodeWord -> exercise.word.uppercase()
        is Exercise.SpeedChallenge -> exercise.text.uppercase()
    }

    private fun expectedAnswer(exercise: Exercise): String = when (exercise) {
        is Exercise.ListenAndIdentify -> exercise.answer.toString()
        is Exercise.ReadAndTap -> exercise.expectedMorse
        is Exercise.DecodeWord -> exercise.answer.uppercase()
        is Exercise.EncodeWord -> exercise.expectedMorse
        is Exercise.SpeedChallenge -> exercise.text.uppercase()
    }

    private fun normalizeAnswer(exercise: Exercise, answer: String): String = when (exercise) {
        is Exercise.ReadAndTap,
        is Exercise.EncodeWord,
        -> canonicalizeMorse(answer)

        is Exercise.ListenAndIdentify,
        is Exercise.DecodeWord,
        is Exercise.SpeedChallenge,
        -> answer.trim().uppercase()
    }

    private fun canonicalizeMorse(answer: String): String {
        return answer
            .replace(Regex("\\s*/\\s*"), " / ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    private fun expectedCharacters(exercise: Exercise): List<Char> {
        val source = when (exercise) {
            is Exercise.ListenAndIdentify -> exercise.answer.toString()
            is Exercise.ReadAndTap -> exercise.character.toString()
            is Exercise.DecodeWord -> exercise.answer
            is Exercise.EncodeWord -> exercise.word
            is Exercise.SpeedChallenge -> exercise.text
        }

        return source.uppercase().toList()
    }
}
