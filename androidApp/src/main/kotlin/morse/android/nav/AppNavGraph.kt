package morse.android.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import morse.android.audiodecode.AudioDecodeScreen
import morse.android.freestyle.FreestyleScreen
import morse.android.home.HomeScreen
import morse.android.learn.LearnScreen
import morse.android.practice.PracticeLaunchConfig
import morse.android.practice.PracticeScreen
import morse.android.reference.ReferenceScreen
import morse.android.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Learn : Screen("learn")
    object Practice : Screen("practice?mode={mode}&lessonId={lessonId}&difficulty={difficulty}&wpm={wpm}") {
        fun createRoute(config: PracticeLaunchConfig) = config.toRoute()
    }
    object Reference : Screen("reference")
    object Settings : Screen("settings")
    object AudioDecode : Screen("audiodecode")
    object Freestyle : Screen("freestyle")
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLearn = { navController.navigate(Screen.Learn.route) },
                onNavigateToPractice = { config ->
                    navController.navigate(Screen.Practice.createRoute(config))
                },
                onNavigateToReference = { navController.navigate(Screen.Reference.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAudioDecode = { navController.navigate(Screen.AudioDecode.route) },
                onNavigateToFreestyle = { navController.navigate(Screen.Freestyle.route) },
            )
        }
        composable(Screen.Learn.route) {
            LearnScreen(
                onNavigateToPractice = { lessonId ->
                    navController.navigate(Screen.Practice.createRoute(PracticeLaunchConfig.lesson(lessonId)))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.Practice.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "lesson"
                },
                navArgument("lessonId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("difficulty") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("wpm") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            PracticeScreen(onFinished = { navController.popBackStack() })
        }
        composable(Screen.Reference.route) {
            ReferenceScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AudioDecode.route) {
            AudioDecodeScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Freestyle.route) {
            FreestyleScreen(onBack = { navController.popBackStack() })
        }
    }
}
