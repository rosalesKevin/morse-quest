package morse.practice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LessonCatalogTest {
    @Test
    fun buildsDefaultLessonsWithReviewNodesAfterEveryTwoStandardLessons() {
        val lessons = LessonCatalog.defaultLessons()

        assertEquals(30, lessons.size)
        assertEquals(listOf('K', 'M'), lessons.first().characters)
        assertEquals(LessonKind.Standard, lessons[0].kind)
        assertEquals(listOf('R', 'S'), lessons[1].characters)
        assertEquals(LessonKind.Standard, lessons[1].kind)
        assertEquals("Review 1", lessons[2].title)
        assertEquals(LessonKind.Review, lessons[2].kind)
        assertEquals(listOf('K', 'M', 'R', 'S'), lessons[2].characters)
        assertEquals(emptyList(), lessons[2].introducedCharacters)
        assertEquals(listOf('U', 'A'), lessons[3].characters)
        assertEquals(listOf('P', 'T'), lessons[4].characters)
        assertEquals("Review 2", lessons[5].title)
        assertEquals(listOf('K', 'M', 'R', 'S', 'U', 'A', 'P', 'T'), lessons[5].characters)
    }

    @Test
    fun standardLessonsUseOnlyTheirOwnIntroducedCharacters() {
        val lesson = LessonCatalog.defaultLessons()[1]
        val lessonCharacters = setOf('R', 'S')

        assertEquals(10, lesson.exercises.size)
        assertTrue(lesson.exercises.all { exerciseUsesOnlyCharacters(it, lessonCharacters) })
    }

    @Test
    fun reviewLessonsUseAllCharactersIntroducedSoFar() {
        val review = LessonCatalog.defaultLessons()[5]
        val reviewCharacters = KochOrder.characters.take(8).toSet()

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
            review.exercises.map { it::class },
        )
        assertTrue(review.exercises.all { exerciseUsesOnlyCharacters(it, reviewCharacters) })
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
