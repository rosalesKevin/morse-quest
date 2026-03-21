package morse.practice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PracticeSessionTest {
    private val lesson = Lesson(
        id = "lesson-test",
        title = "Lesson Test",
        characters = listOf('K', 'M'),
        exercises = listOf(
            Exercise.ListenAndIdentify(morse = "-.-", answer = 'K'),
            Exercise.ReadAndTap(character = 'M', expectedMorse = "--"),
            Exercise.DecodeWord(morse = "-- -.-", answer = "MK"),
            Exercise.EncodeWord(word = "KM", expectedMorse = "-.- --"),
            Exercise.SpeedChallenge(text = "KM", targetWpm = 18),
        ),
    )

    @Test
    fun evaluatesExerciseAnswersAcrossSupportedTypes() {
        val session = PracticeSession(lesson)

        assertTrue(session.submitAnswer(lesson.exercises[0], "k").isCorrect)
        assertTrue(session.submitAnswer(lesson.exercises[1], "--").isCorrect)
        assertTrue(session.submitAnswer(lesson.exercises[2], "mk").isCorrect)
        assertTrue(session.submitAnswer(lesson.exercises[3], "-.- --").isCorrect)
        assertTrue(session.submitAnswer(lesson.exercises[4], "km").isCorrect)
    }

    @Test
    fun derivesScoreAccuracyAndMistakeAggregationFromSubmittedAnswers() {
        val session = PracticeSession(lesson)

        session.submitAnswer(lesson.exercises[0], "M")
        session.submitAnswer(lesson.exercises[1], "-")
        session.submitAnswer(lesson.exercises[2], "MK")
        session.submitAnswer(lesson.exercises[3], "-.- --")
        session.submitAnswer(lesson.exercises[4], "KK")

        val score = session.getScore()

        assertEquals(2, score.correct)
        assertEquals(5, score.total)
        assertEquals(18.0, score.wpm)
        assertEquals(40.0, score.accuracy)
        assertEquals(
            listOf(
                MistakeRecord('K', 2),
                MistakeRecord('M', 2),
            ),
            session.getMistakes(),
        )
    }

    @Test
    fun returnsIncorrectResultWithExpectedAnswerMetadata() {
        val session = PracticeSession(lesson)

        val result = session.submitAnswer(lesson.exercises[1], ".")

        assertFalse(result.isCorrect)
        assertEquals("--", result.expectedAnswer)
        assertEquals("M", result.expectedText)
        assertEquals(listOf('M'), result.mistakeCharacters)
    }
}
