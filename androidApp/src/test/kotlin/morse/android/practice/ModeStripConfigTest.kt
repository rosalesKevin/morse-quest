package morse.android.practice

import morse.practice.Exercise
import org.junit.Assert.assertEquals
import org.junit.Test

class ModeStripConfigTest {

    @Test
    fun `ListenAndIdentify maps to correct config`() {
        val exercise = Exercise.ListenAndIdentify(morse = ".-", answer = 'A')
        val config = ModeStripConfig.from(exercise)
        assertEquals("Listen & Identify", config.label)
        assertEquals("Type the letter you hear", config.hint)
        assertEquals(ModeStripIcon.HEADPHONES, config.icon)
    }

    @Test
    fun `ReadAndTap maps to correct config`() {
        val exercise = Exercise.ReadAndTap(character = 'A', expectedMorse = ".-")
        val config = ModeStripConfig.from(exercise)
        assertEquals("Read & Tap", config.label)
        assertEquals("Tap the Morse for this character", config.hint)
        assertEquals(ModeStripIcon.TAP, config.icon)
    }

    @Test
    fun `DecodeWord maps to correct config`() {
        val exercise = Exercise.DecodeWord(morse = ".- -...", answer = "AB")
        val config = ModeStripConfig.from(exercise)
        assertEquals("Decode", config.label)
        assertEquals("Type the word these signals spell", config.hint)
        assertEquals(ModeStripIcon.PUZZLE, config.icon)
    }

    @Test
    fun `EncodeWord maps to correct config`() {
        val exercise = Exercise.EncodeWord(word = "AB", expectedMorse = ".- -...")
        val config = ModeStripConfig.from(exercise)
        assertEquals("Encode", config.label)
        assertEquals("Tap the Morse for this word", config.hint)
        assertEquals(ModeStripIcon.BROADCAST, config.icon)
    }

    @Test
    fun `SpeedChallenge maps to correct config`() {
        val exercise = Exercise.SpeedChallenge(text = "HELLO", targetWpm = 15)
        val config = ModeStripConfig.from(exercise)
        assertEquals("Speed Round", config.label)
        assertEquals("Transcribe what you hear at speed", config.hint)
        assertEquals(ModeStripIcon.ZAP, config.icon)
    }
}
