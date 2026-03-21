package morse.web.persistence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WebSettingsRepositoryTest {

    @Test
    fun loadsDefaultsAndPersistsCoercedUpdates() {
        val driver = InMemoryStorageDriver()
        val repository = WebSettingsRepository(BrowserStorage(driver))

        assertEquals(WebSettings(), repository.settings)

        repository.updateWpm(99)
        repository.updateToneFrequency(880f)
        repository.updateHapticsEnabled(false)

        val reloaded = WebSettingsRepository(BrowserStorage(driver))
        assertEquals(40, reloaded.settings.wpm)
        assertEquals(880f, reloaded.settings.toneFrequencyHz)
        assertFalse(reloaded.settings.hapticsEnabled)
    }

    private class InMemoryStorageDriver : StorageDriver {
        private val values = mutableMapOf<String, String>()

        override fun get(key: String): String? = values[key]

        override fun set(key: String, value: String) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}
