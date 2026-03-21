package morse.communication

import kotlin.random.Random

/**
 * MVP user identity. The [userId] is an anonymous randomly-generated hex string.
 *
 * The platform layer (Android DataStore, browser localStorage) is responsible for
 * persisting the [userId] after first creation so the same identity is reused across
 * sessions. Pass the stored string back via [UserIdentity(userId)] on subsequent starts.
 *
 * Session codes are shared out-of-band (e.g. copy/paste). Account-based identity is
 * out of scope until a future phase.
 */
data class UserIdentity(val userId: String) {
    companion object {
        fun anonymous(): UserIdentity = UserIdentity(randomHexId())
    }
}

private fun randomHexId(): String =
    Random.nextBytes(16).joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
