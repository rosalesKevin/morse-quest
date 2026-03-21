package morse.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AudioMorseDecoderTest {

    // TimingEngine at 20 WPM: dot=60ms, dash=180ms, letterGap=180ms, wordGap=420ms
    // Classification thresholds: dot/dash midpoint=120ms, letter/word gap midpoint=300ms
    private val timing = TimingEngine(characterWpm = 20, effectiveWpm = 20)
    private fun decoder() = AudioMorseDecoder(timing)

    // ── tone classification ────────────────────────────────────────────

    @Test
    fun singleDotProducesE() {
        val dec = decoder()
        // Emit a tone shorter than dash threshold (120ms), then a word-gap silence to flush
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))
        dec.consume(AudioToneEvent(isTone = false, durationMs = 420)) // word-gap flushes
        val state = dec.flush()
        assertEquals("E", state.decodedText)
        assertEquals(".", state.morseText)
    }

    @Test
    fun singleDashProducesT() {
        val dec = decoder()
        dec.consume(AudioToneEvent(isTone = true, durationMs = 180))
        dec.consume(AudioToneEvent(isTone = false, durationMs = 420))
        val state = dec.flush()
        assertEquals("T", state.decodedText)
        assertEquals("-", state.morseText)
    }

    @Test
    fun dotDashSequenceProducesA() {
        val dec = decoder()
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))   // dot
        dec.consume(AudioToneEvent(isTone = false, durationMs = 60))  // intra-char gap
        dec.consume(AudioToneEvent(isTone = true, durationMs = 180))  // dash
        dec.consume(AudioToneEvent(isTone = false, durationMs = 420)) // word-gap flushes
        val state = dec.flush()
        assertEquals("A", state.decodedText)
        assertEquals(".-", state.morseText)
    }

    @Test
    fun dashDotDotDotProducesB() {
        val dec = decoder()
        val intra = 60L
        dec.consume(AudioToneEvent(isTone = true, durationMs = 180))
        dec.consume(AudioToneEvent(isTone = false, durationMs = intra))
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))
        dec.consume(AudioToneEvent(isTone = false, durationMs = intra))
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))
        dec.consume(AudioToneEvent(isTone = false, durationMs = intra))
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))
        dec.consume(AudioToneEvent(isTone = false, durationMs = 420))
        val state = dec.flush()
        assertEquals("B", state.decodedText)
        assertEquals("-...", state.morseText)
    }

    // ── gap classification ─────────────────────────────────────────────

    @Test
    fun letterGapSeparatesLetters() {
        val dec = decoder()
        // dot (E), letter-gap, dash (T)
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))
        dec.consume(AudioToneEvent(isTone = false, durationMs = 180)) // letter-gap
        dec.consume(AudioToneEvent(isTone = true, durationMs = 180))
        dec.consume(AudioToneEvent(isTone = false, durationMs = 420))
        val state = dec.flush()
        assertEquals("ET", state.decodedText)
        assertEquals(". -", state.morseText)
    }

    @Test
    fun wordGapProducesSlashInMorse() {
        val dec = decoder()
        // E, word-gap, T
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))
        dec.consume(AudioToneEvent(isTone = false, durationMs = 420)) // word-gap
        dec.consume(AudioToneEvent(isTone = true, durationMs = 180))
        dec.consume(AudioToneEvent(isTone = false, durationMs = 420))
        val state = dec.flush()
        assertEquals("E T", state.decodedText)
        assertEquals(". / -", state.morseText)
    }

    @Test
    fun unknownPatternProducesQuestionMark() {
        val dec = decoder()
        // Six dots → unknown (not in alphabet)
        repeat(6) {
            dec.consume(AudioToneEvent(isTone = true, durationMs = 60))
            if (it < 5) dec.consume(AudioToneEvent(isTone = false, durationMs = 60))
        }
        dec.consume(AudioToneEvent(isTone = false, durationMs = 420))
        val state = dec.flush()
        assertEquals("?", state.decodedText)
    }

    // ── state management ───────────────────────────────────────────────

    @Test
    fun resetClearsAllState() {
        val dec = decoder()
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))
        dec.reset()
        val state = dec.flush()
        assertEquals("", state.morseText)
        assertEquals("", state.decodedText)
    }

    @Test
    fun flushEmitsPendingPattern() {
        val dec = decoder()
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))  // dot, no gap yet
        val state = dec.flush()                                        // flush forces output
        assertEquals("E", state.decodedText)
        assertEquals(".", state.morseText)
    }

    @Test
    fun liveStateShowsPartialPatternBeforeFlush() {
        val dec = decoder()
        dec.consume(AudioToneEvent(isTone = true, durationMs = 60))
        val state = dec.consume(AudioToneEvent(isTone = false, durationMs = 30)) // intra-char
        // Pattern "." should appear in morseText even before a letter gap
        assertTrue(state.morseText.contains("."))
    }

    @Test
    fun multipleWordsDecodeCorrectly() {
        val dec = decoder()
        // K = -.- ; M = --
        fun tone(ms: Long) = dec.consume(AudioToneEvent(isTone = true, durationMs = ms))
        fun silence(ms: Long) = dec.consume(AudioToneEvent(isTone = false, durationMs = ms))
        // K: - . -
        tone(180); silence(60); tone(60); silence(60); tone(180)
        silence(420) // word gap
        // M: - -
        tone(180); silence(60); tone(180)
        silence(420)
        val state = dec.flush()
        assertEquals("K M", state.decodedText)
        assertEquals("-.- / --", state.morseText)
    }
}
