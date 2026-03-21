package morse.android.reference

import kotlin.test.Test
import kotlin.test.assertEquals

class ReferenceSectionBuilderTest {

    @Test
    fun `blank query groups entries into letters and numbers`() {
        val entries = listOf(
            ReferenceViewModel.ReferenceEntry('A', ".-"),
            ReferenceViewModel.ReferenceEntry('1', ".----"),
            ReferenceViewModel.ReferenceEntry('B', "-..."),
            ReferenceViewModel.ReferenceEntry('2', "..---"),
        )

        val sections = buildReferenceSections(entries, query = "")

        assertEquals(listOf("Letters", "Numbers"), sections.map { it.title })
        assertEquals(listOf('A', 'B'), sections[0].entries.map { it.character })
        assertEquals(listOf('1', '2'), sections[1].entries.map { it.character })
    }

    @Test
    fun `active query keeps a single matching section`() {
        val entries = listOf(
            ReferenceViewModel.ReferenceEntry('A', ".-"),
            ReferenceViewModel.ReferenceEntry('B', "-..."),
        )

        val sections = buildReferenceSections(entries, query = "A")

        assertEquals(1, sections.size)
        assertEquals("Matches", sections.single().title)
        assertEquals(listOf('A'), sections.single().entries.map { it.character })
    }
}
