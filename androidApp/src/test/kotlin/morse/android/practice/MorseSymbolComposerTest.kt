package morse.android.practice

import kotlin.test.Test
import kotlin.test.assertEquals

class MorseSymbolComposerTest {

    @Test
    fun `composer builds canonical morse with letter and word gaps`() {
        val composer = MorseSymbolComposer()

        composer.appendDot()
        composer.appendDash()
        composer.appendLetterGap()
        composer.appendDot()
        composer.appendWordGap()
        composer.appendDash()

        assertEquals(".- . / -", composer.answer)
    }

    @Test
    fun `delete removes separators and symbols cleanly`() {
        val composer = MorseSymbolComposer(".- / -")

        composer.deleteLast()
        assertEquals(".- / ", composer.answer)
        composer.deleteLast()
        assertEquals(".-", composer.answer)
    }

    @Test
    fun `clear resets the answer`() {
        val composer = MorseSymbolComposer(".- / -")

        composer.clear()

        assertEquals("", composer.answer)
    }
}
