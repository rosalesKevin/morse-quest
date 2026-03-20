package morse.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TapInputParserTest {
    private val timingEngine = TimingEngine(characterWpm = 20, effectiveWpm = 20)

    @Test
    fun recognizesDotInput() {
        val parser = TapInputParser(timingEngine)

        parser.onPress(0L)
        parser.onRelease(60L)

        assertEquals("E", parser.onIdle(1_320L))
    }

    @Test
    fun recognizesDashInput() {
        val parser = TapInputParser(timingEngine)

        parser.onPress(0L)
        parser.onRelease(180L)

        assertEquals("T", parser.onIdle(1_440L))
    }

    @Test
    fun flushesLettersWithinAWord() {
        val parser = TapInputParser(timingEngine)

        parser.onPress(0L)
        parser.onRelease(60L)
        assertNull(parser.onIdle(180L))

        parser.onPress(120L)
        parser.onRelease(300L)

        assertEquals("A", parser.onIdle(1_560L))
    }

    @Test
    fun flushesWordsAfterLongerIdlePeriods() {
        val parser = TapInputParser(timingEngine)

        parser.onPress(0L)
        parser.onRelease(60L)
        parser.onIdle(480L)

        parser.onPress(800L)
        parser.onRelease(860L)

        assertEquals("E E", parser.onIdle(2_200L))
    }

    @Test
    fun ignoresDebouncedNoise() {
        val parser = TapInputParser(timingEngine, minimumPressDurationMs = 30L)

        parser.onPress(0L)
        parser.onRelease(10L)

        assertNull(parser.onIdle(1_260L))
    }

    @Test
    fun resetClearsBufferedInput() {
        val parser = TapInputParser(timingEngine)

        parser.onPress(0L)
        parser.onRelease(60L)
        parser.reset()

        assertNull(parser.onIdle(1_260L))
    }
}
