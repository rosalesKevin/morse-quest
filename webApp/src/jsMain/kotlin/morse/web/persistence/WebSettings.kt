package morse.web.persistence

import kotlinx.serialization.Serializable

@Serializable
data class WebSettings(
    val wpm: Int = 20,
    val toneFrequencyHz: Float = 700f,
    val hapticsEnabled: Boolean = true,
)
