package com.android.notikeep.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.notikeep.presentation.appdetail.AppDetailScreen
import com.android.notikeep.presentation.conversation.ConversationScreen
import com.android.notikeep.presentation.home.HomeScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onAppClick = { packageName ->
                    navController.navigate("appDetail/${URLEncoder.encode(packageName, "UTF-8")}")
                }
            )
        }

        composable(
            route = "appDetail/{packageName}",
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            AppDetailScreen(
                onBack = { navController.popBackStack() },
                onConversationClick = { packageName, conversationKey ->
                    val p = URLEncoder.encode(packageName, "UTF-8").replace("+", "%20")
                    val k = URLEncoder.encode(conversationKey, "UTF-8").replace("+", "%20")
                    navController.navigate("conversation/$p/$k")
                }
            )
        }

        composable(
            route = "conversation/{packageName}/{conversationKey}",
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType },
                navArgument("conversationKey") { type = NavType.StringType }
            )
        ) {
            ConversationScreen(onBack = { navController.popBackStack() })
        }
    }
}
