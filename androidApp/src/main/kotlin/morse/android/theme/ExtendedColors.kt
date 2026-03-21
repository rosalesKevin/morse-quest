package morse.android.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
    val signalAmber: Color,
    val signalGlow: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = Color.Unspecified,
        successContainer = Color.Unspecified,
        signalAmber = Color.Unspecified,
        signalGlow = Color.Unspecified
    )
}

val lightExtendedColors = ExtendedColors(
    success = LightSuccess,
    successContainer = LightSuccessContainer,
    signalAmber = LightSignalAmber,
    signalGlow = LightSignalGlow
)

val darkExtendedColors = ExtendedColors(
    success = DarkSuccess,
    successContainer = DarkSuccessContainer,
    signalAmber = DarkSignalAmber,
    signalGlow = DarkSignalGlow
)
