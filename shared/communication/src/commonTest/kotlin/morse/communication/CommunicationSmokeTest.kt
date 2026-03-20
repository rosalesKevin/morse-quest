package morse.communication

import kotlin.test.Test
import kotlin.test.assertFalse

class CommunicationSmokeTest {
    @Test
    fun scaffoldPlaceholderStartsNotReady() {
        assertFalse(CommunicationPlaceholder().ready)
    }
}
