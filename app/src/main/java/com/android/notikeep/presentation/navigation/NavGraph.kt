package com.android.notikeep.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.notikeep.presentation.appdetail.AppDetailScreen
import com.android.notikeep.presentation.conversation.ConversationScreen
import com.android.notikeep.presentation.home.HomeScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeRoute) {

        composable<HomeRoute> {
            HomeScreen(
                onAppClick = { packageName ->
                    navController.navigate(AppDetailRoute(packageName))
                }
            )
        }

        composable<AppDetailRoute> {
            AppDetailScreen(
                onBack = { navController.popBackStack() },
                onConversationClick = { packageName, conversationKey ->
                    navController.navigate(
                        ConversationRoute(
                            packageName = packageName,
                            conversationKey = conversationKey
                        )
                    )
                }
            )
        }

        composable<ConversationRoute> {
            ConversationScreen(onBack = { navController.popBackStack() })
        }
    }
}
