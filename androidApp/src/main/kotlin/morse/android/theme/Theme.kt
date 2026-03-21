package morse.android.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    secondaryContainer = LightSecondaryContainer,
    tertiary = LightTertiary,
    tertiaryContainer = LightTertiaryContainer,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
    errorContainer = LightErrorContainer,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    secondaryContainer = DarkSecondaryContainer,
    tertiary = DarkTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    errorContainer = DarkErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

@Composable
fun MorseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = if (darkTheme) darkExtendedColors else lightExtendedColors
    val animatedSurface = animateColorAsState(
        targetValue = colorScheme.surface,
        animationSpec = tween(durationMillis = 240),
        label = "morse_surface",
    )
    val animatedContent = animateColorAsState(
        targetValue = colorScheme.onSurface,
        animationSpec = tween(durationMillis = 240),
        label = "morse_on_surface",
    )

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = animatedSurface.value,
                contentColor = animatedContent.value,
                content = content,
            )
        }
    }
}
