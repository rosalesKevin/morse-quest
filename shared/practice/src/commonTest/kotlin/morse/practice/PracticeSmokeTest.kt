package morse.practice

import kotlin.test.Test
import kotlin.test.assertEquals

class PracticeSmokeTest {
    @Test
    fun scaffoldMarkerIsExposed() {
        assertEquals("practice", PracticeScaffold.moduleName)
    }
}
