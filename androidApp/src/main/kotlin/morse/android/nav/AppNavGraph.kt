package morse.android.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import morse.android.home.HomeScreen
import morse.android.learn.LearnScreen
import morse.android.practice.PracticeScreen
import morse.android.reference.ReferenceScreen
import morse.android.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Learn : Screen("learn")
    object Practice : Screen("practice/{lessonId}") {
        fun createRoute(lessonId: String) = "practice/$lessonId"
    }
    object Reference : Screen("reference")
    object Settings : Screen("settings")
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLearn = { navController.navigate(Screen.Learn.route) },
                onNavigateToPractice = { lessonId ->
                    navController.navigate(Screen.Practice.createRoute(lessonId))
                },
                onNavigateToReference = { navController.navigate(Screen.Reference.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.Learn.route) {
            LearnScreen(
                onNavigateToPractice = { lessonId ->
                    navController.navigate(Screen.Practice.createRoute(lessonId))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.Practice.route,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
        ) {
            PracticeScreen(onFinished = { navController.popBackStack() })
        }
        composable(Screen.Reference.route) {
            ReferenceScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
