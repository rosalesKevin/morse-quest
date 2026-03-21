package morse.android

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import morse.android.nav.AppNavGraph

@Composable
fun MorseApp(darkTheme: Boolean = isSystemInDarkTheme()) {
    morse.android.theme.MorseTheme(darkTheme = darkTheme) {
        AppNavGraph()
    }
}
