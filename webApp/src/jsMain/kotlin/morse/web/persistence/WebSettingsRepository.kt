package morse.web.persistence

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebSettingsRepository(
    private val storage: BrowserStorage,
    private val json: Json = Json,
) {
    val settings: WebSettings
        get() = storage.getString(KEY)
            ?.let { raw -> runCatching { json.decodeFromString<WebSettings>(raw) }.getOrNull() }
            ?: WebSettings()

    fun updateWpm(wpm: Int) {
        persist(settings.copy(wpm = wpm.coerceIn(5, 40)))
    }

    fun updateToneFrequency(hz: Float) {
        persist(settings.copy(toneFrequencyHz = hz))
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        persist(settings.copy(hapticsEnabled = enabled))
    }

    private fun persist(value: WebSettings) {
        storage.setString(KEY, json.encodeToString(value))
    }

    private companion object {
        private const val KEY = "web_settings"
    }
}
