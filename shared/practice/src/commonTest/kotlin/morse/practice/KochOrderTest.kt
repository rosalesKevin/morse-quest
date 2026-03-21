package morse.practice

import kotlin.test.Test
import kotlin.test.assertEquals

class KochOrderTest {
    @Test
    fun exposesTheSpecifiedKochCharacterOrder() {
        assertEquals(
            listOf(
                'K', 'M', 'R', 'S', 'U', 'A', 'P', 'T', 'L', 'O',
                'W', 'I', '.', 'N', 'J', 'E', 'F', '0', 'Y', 'V',
                ',', 'G', '5', '/', 'Q', '9', 'Z', 'H', '3', '8',
                'B', '?', '4', '2', '7', 'C', '1', 'D', '6', 'X',
            ),
            KochOrder.characters,
        )
    }
}
