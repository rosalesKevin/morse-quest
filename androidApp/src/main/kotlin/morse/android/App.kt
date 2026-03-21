package morse.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import morse.android.nav.AppNavGraph

@Composable
fun MorseApp() {
    MaterialTheme {
        AppNavGraph()
    }
}
