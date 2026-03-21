package morse.android.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
    val signalActive: Color,
    val signalGlow: Color,
    val rewardAmber: Color,
    val exercisePurple: Color,
    val exerciseTeal: Color,
    val exerciseOrange: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = Color.Unspecified,
        successContainer = Color.Unspecified,
        signalActive = Color.Unspecified,
        signalGlow = Color.Unspecified,
        rewardAmber = Color.Unspecified,
        exercisePurple = Color.Unspecified,
        exerciseTeal = Color.Unspecified,
        exerciseOrange = Color.Unspecified,
    )
}

val lightExtendedColors = ExtendedColors(
    success = LightSuccess,
    successContainer = LightSuccessContainer,
    signalActive = LightSignalActive,
    signalGlow = LightSignalGlow,
    rewardAmber = LightRewardAmber,
    exercisePurple = ListenPurple,
    exerciseTeal = DecodeTeal,
    exerciseOrange = EncodeOrange,
)

val darkExtendedColors = ExtendedColors(
    success = DarkSuccess,
    successContainer = DarkSuccessContainer,
    signalActive = DarkSignalActive,
    signalGlow = DarkSignalGlow,
    rewardAmber = DarkRewardAmber,
    exercisePurple = ListenPurpleDark,
    exerciseTeal = DecodeTealDark,
    exerciseOrange = EncodeOrangeDark,
)
