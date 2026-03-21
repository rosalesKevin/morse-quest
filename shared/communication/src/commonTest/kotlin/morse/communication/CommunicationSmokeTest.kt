package morse.communication

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CommunicationSmokeTest {

    @Test
    fun anonymousUserIdIsNonEmpty() {
        val identity = UserIdentity.anonymous()
        assertTrue(identity.userId.isNotEmpty())
    }

    @Test
    fun anonymousUserIdsAreUnique() {
        val a = UserIdentity.anonymous()
        val b = UserIdentity.anonymous()
        assertNotEquals(a.userId, b.userId)
    }
}
