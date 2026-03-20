package morse.core

import kotlin.test.Test
import kotlin.test.assertEquals

class MorseAlphabetTest {
    @Test
    fun exposesCharacterAndPunctuationMappings() {
        assertEquals(".-", MorseAlphabet.characters['A'])
        assertEquals("-----", MorseAlphabet.characters['0'])
        assertEquals("..--..", MorseAlphabet.characters['?'])
    }

    @Test
    fun exposesProsignMappings() {
        assertEquals(".-.-.", MorseAlphabet.prosigns["AR"])
        assertEquals("...-.-", MorseAlphabet.prosigns["SK"])
        assertEquals("-...-", MorseAlphabet.prosigns["BT"])
    }

    @Test
    fun exposesReverseLookups() {
        assertEquals('S', MorseAlphabet.reverseCharacters["..."])
        assertEquals('!', MorseAlphabet.reverseCharacters["-.-.--"])
        assertEquals("AR", MorseAlphabet.reverseTokens[".-.-."])
    }
}
