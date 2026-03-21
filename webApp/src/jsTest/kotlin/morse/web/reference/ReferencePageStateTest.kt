package morse.web.reference

import morse.web.createTestDependencies
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReferencePageStateTest {

    @Test
    fun filtersMorseEntriesByCharacterOrPattern() {
        val dependencies = createTestDependencies()
        val state = ReferencePageState(
            audioPlayer = dependencies.audioPlayer,
            settingsRepository = dependencies.settingsRepository,
        )

        state.updateQuery(".-")
        assertTrue(state.entries.any { it.character == 'A' })

        state.updateQuery("Z")
        assertEquals(listOf('Z'), state.entries.map { it.character })
    }
}
