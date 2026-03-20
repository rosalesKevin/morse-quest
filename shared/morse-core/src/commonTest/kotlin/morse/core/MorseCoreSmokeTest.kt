package morse.core

import kotlin.test.Test
import kotlin.test.assertEquals

class MorseCoreSmokeTest {
    @Test
    fun scaffoldMarkerIsExposed() {
        assertEquals("morse-core", MorseCoreScaffold.moduleName)
    }
}
