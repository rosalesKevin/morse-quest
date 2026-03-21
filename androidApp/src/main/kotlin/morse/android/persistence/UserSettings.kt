package morse.android.persistence

data class UserSettings(
    val wpm: Int = 20,
    val toneFrequencyHz: Float = 700f,
    val hapticsEnabled: Boolean = true,
)
