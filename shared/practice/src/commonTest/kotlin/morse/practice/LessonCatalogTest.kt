package morse.practice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LessonCatalogTest {
    @Test
    fun buildsDefaultLessonsWithTwoNewCharactersAtATime() {
        val lessons = LessonCatalog.defaultLessons()

        assertEquals(20, lessons.size)
        assertEquals(listOf('K', 'M'), lessons.first().characters)
        assertEquals(listOf('R', 'S'), lessons[1].characters)
    }

    @Test
    fun createsDeterministicExercisesUsingOnlyUnlockedCharacters() {
        val lesson = LessonCatalog.defaultLessons()[2]
        val unlockedCharacters = KochOrder.characters.take(6).toSet()

        assertEquals(10, lesson.exercises.size)
        assertEquals(
            listOf(
                Exercise.ListenAndIdentify::class,
                Exercise.ReadAndTap::class,
                Exercise.DecodeWord::class,
                Exercise.EncodeWord::class,
                Exercise.SpeedChallenge::class,
                Exercise.ListenAndIdentify::class,
                Exercise.ReadAndTap::class,
                Exercise.DecodeWord::class,
                Exercise.EncodeWord::class,
                Exercise.SpeedChallenge::class,
            ),
            lesson.exercises.map { it::class },
        )
        assertTrue(lesson.exercises.all { exerciseUsesOnlyCharacters(it, unlockedCharacters) })
    }

    private fun exerciseUsesOnlyCharacters(exercise: Exercise, unlockedCharacters: Set<Char>): Boolean {
        val content = when (exercise) {
            is Exercise.ListenAndIdentify -> exercise.answer.toString()
            is Exercise.ReadAndTap -> exercise.character.toString()
            is Exercise.DecodeWord -> exercise.answer
            is Exercise.EncodeWord -> exercise.word
            is Exercise.SpeedChallenge -> exercise.text
        }

        return content.all { it in unlockedCharacters }
    }
}
