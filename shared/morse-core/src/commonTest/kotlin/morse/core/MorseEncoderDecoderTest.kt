package morse.core

import kotlin.test.Test
import kotlin.test.assertEquals

class MorseEncoderDecoderTest {
    private val timingEngine = TimingEngine()
    private val encoder = MorseEncoder(timingEngine)

    @Test
    fun encodesLettersWordsAndNormalization() {
        assertEquals("... --- ...", encoder.encode("sos"))
        assertEquals(".... .. / - .... . .-. .", encoder.encode("HI THERE"))
        assertEquals(".... .. / - .... . .-. .", encoder.encode("hi   there"))
    }

    @Test
    fun encodesPunctuationSkipsUnknownCharactersAndSupportsProsigns() {
        assertEquals(".- ..--..", encoder.encode("A?"))
        assertEquals(".- -...", encoder.encode("A#b"))
        assertEquals(".-.-. / ...-.- / -...-", encoder.encode("AR SK BT"))
    }

    @Test
    fun decodesCanonicalAndPermissiveInputForms() {
        assertEquals("SOS", MorseDecoder.decode("... --- ..."))
        assertEquals("HI THERE", MorseDecoder.decode(".... .. / - .... . .-. ."))
        assertEquals("HI THERE", MorseDecoder.decode(".... ..  - .... . .-. ."))
    }

    @Test
    fun decodesUnknownPatternsAsFallback() {
        assertEquals("?", MorseDecoder.decode("...---..."))
    }
}
