package morse.android.practice

import kotlin.test.assertEquals
import morse.practice.LessonCatalog
import org.junit.Test

class LessonReferenceGuideTest {

    private val lessons = LessonCatalog.defaultLessons()

    @Test
    fun `guide items include lesson characters and morse symbols`() {
        val lesson = lessons.first()
        val items = buildLessonReferenceItems(lesson)

        assertEquals(lesson.characters, items.map { it.character })
        assertEquals(listOf("-.-", "--"), items.map { it.morse })
    }
}
