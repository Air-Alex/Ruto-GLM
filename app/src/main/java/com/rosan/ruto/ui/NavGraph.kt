package com.rosan.ruto.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rosan.ruto.ui.compose.AppListScreen
import com.rosan.ruto.ui.compose.GuideScreen
import com.rosan.ruto.ui.compose.HomeScreen
import com.rosan.ruto.ui.compose.SplashScreen
import com.rosan.ruto.ui.compose.TaskExecutionScreen
import com.rosan.ruto.ui.compose.TaskListScreen
import com.rosan.ruto.ui.compose.TaskPreviewScreen

object Destinations {
    const val SPLASH = "splash"
    const val GUIDE = "guide"
    const val HOME = "home"
    const val APP_LIST = "app_list"
    const val TASK_EXECUTION = "task_execution"
    const val TASK_LIST = "task_list"
    const val TASK_PREVIEW = "task_preview"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val insets = WindowInsets.safeContent

    NavHost(navController = navController, startDestination = Destinations.SPLASH) {
        composable(Destinations.SPLASH) {
            SplashScreen(navController, insets.asPaddingValues())
        }
        composable(Destinations.GUIDE) {
            GuideScreen(navController, insets.asPaddingValues())
        }
        composable(Destinations.HOME) {
            HomeScreen(navController, insets)
        }
        composable(Destinations.APP_LIST) {
            AppListScreen(navController, insets)
        }
        composable(Destinations.TASK_LIST) {
            TaskListScreen(navController, insets)
        }
        composable(
            route = "${Destinations.TASK_EXECUTION}/{packageName}/{apiKey}/{hostUrl}/{modelId}/{task}",
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType },
                navArgument("apiKey") { type = NavType.StringType },
                navArgument("hostUrl") { type = NavType.StringType },
                navArgument("modelId") { type = NavType.StringType },
                navArgument("task") { type = NavType.StringType },
            )
        ) {
            TaskExecutionScreen(navController, insets)
        }
        composable(
            route = "${Destinations.TASK_PREVIEW}/{taskKey}",
            arguments = listOf(
                navArgument("taskKey") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskKey = backStackEntry.arguments?.getString("taskKey") ?: ""
            TaskPreviewScreen(navController, taskKey)
        }
    }
}