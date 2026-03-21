package morse.android.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTokensTest {

    @Test
    fun `light palette matches phase 09 spec`() {
        assertEquals(Color(0xFFFFFFFF), LightSurface)
        assertEquals(Color(0xFFF5F7FA), LightSurfaceVariant)
        assertEquals(Color(0xFF1A73E8), LightPrimary)
        assertEquals(Color(0xFF1C1C1E), LightOnSurface)
        assertEquals(Color(0xFF6E6E73), LightOnSurfaceVariant)
    }

    @Test
    fun `dark palette matches phase 09 spec`() {
        assertEquals(Color(0xFF0E0E10), DarkSurface)
        assertEquals(Color(0xFF1C1C1E), DarkSurfaceVariant)
        assertEquals(Color(0xFF4DA3FF), DarkPrimary)
        assertEquals(Color(0xFFF2F2F7), DarkOnSurface)
        assertEquals(Color(0xFF8E8E93), DarkOnSurfaceVariant)
    }

    @Test
    fun `typography follows the simplified phase 09 scale`() {
        assertEquals(40.sp, AppTypography.displayLarge.fontSize)
        assertEquals(28.sp, AppTypography.headlineLarge.fontSize)
        assertEquals(20.sp, AppTypography.headlineMedium.fontSize)
        assertEquals(16.sp, AppTypography.bodyLarge.fontSize)
        assertEquals(14.sp, AppTypography.bodyMedium.fontSize)
        assertEquals(12.sp, AppTypography.bodySmall.fontSize)
        assertEquals(28.sp, MorseDisplayTextStyle.fontSize)
        assertEquals(16.sp, MorseInlineTextStyle.fontSize)
        assertEquals(28.sp, StatValueTextStyle.fontSize)
    }
}
