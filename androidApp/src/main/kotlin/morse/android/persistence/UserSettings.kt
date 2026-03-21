package morse.android.persistence

data class UserSettings(
    val wpm: Int = 20,
    val toneFrequencyHz: Float = 700f,
    val hapticsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val audioProfile: AudioProfile = AudioProfile.PURE
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AudioProfile {
    PURE, SOFT, TELEGRAPH
}
