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
import com.rosan.ruto.ui.compose.ConversationListScreen
import com.rosan.ruto.ui.compose.ConversationScreen
import com.rosan.ruto.ui.compose.GuideScreen
import com.rosan.ruto.ui.compose.HomeScreen
import com.rosan.ruto.ui.compose.LlmModelListScreen
import com.rosan.ruto.ui.compose.MultiTaskPreviewScreen
import com.rosan.ruto.ui.compose.ScreenListScreen
import com.rosan.ruto.ui.compose.SplashScreen

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
        composable(Destinations.SCREEN_LIST) {
            ScreenListScreen(navController, insets)
        }
        composable(Destinations.CONVERSATION_LIST) {
            ConversationListScreen(navController, insets)
        }
        composable(Destinations.LLM_MODEL_LIST) {
            LlmModelListScreen(navController, insets)
        }
        composable(
            route = "${Destinations.CONVERSATION}/{conversationId}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getLong("conversationId") ?: 0
            ConversationScreen(navController, insets, conversationId)
        }
        composable(
            route = "${Destinations.MULTI_TASK_PREVIEW}/{displayIds}",
            arguments = listOf(
                navArgument("displayIds") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val displayIds = backStackEntry.arguments?.getString("displayIds")?.split(",")
                ?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            MultiTaskPreviewScreen(navController, displayIds)
        }
    }
}
