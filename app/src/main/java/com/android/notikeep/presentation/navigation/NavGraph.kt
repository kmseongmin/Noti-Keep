package com.android.notikeep.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.notikeep.presentation.appdetail.AppDetailScreen
import com.android.notikeep.presentation.conversation.ConversationScreen
import com.android.notikeep.presentation.home.HomeScreen

/**
 * 앱 전체 네비게이션 그래프.
 * 화면 구조: HomeScreen → AppDetailScreen → ConversationScreen
 *
 * 각 라우트는 @Serializable data class/object로 타입 안전 네비게이션 사용.
 * (kotlin-serialization + navigation-compose)
 */
@Composable
fun NavGraph() {
    /** NavController: 뒤로가기, navigate() 등 화면 전환 제어 */
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeRoute) {

        // 홈 화면: 앱 단위 알림 목록
        composable<HomeRoute> {
            HomeScreen(
                onAppClick = { packageName ->
                    // 앱 클릭 → 해당 앱의 AppDetail 화면으로 이동
                    navController.navigate(AppDetailRoute(packageName))
                }
            )
        }

        // 앱 상세 화면: 특정 앱의 대화방 목록
        composable<AppDetailRoute> {
            AppDetailScreen(
                onBack = { navController.popBackStack() },
                onConversationClick = { packageName, conversationKey ->
                    // 대화방 클릭 → 해당 대화 내역 화면으로 이동
                    navController.navigate(
                        ConversationRoute(
                            packageName = packageName,
                            conversationKey = conversationKey
                        )
                    )
                }
            )
        }

        // 대화 내역 화면: 특정 대화방의 알림 메시지 목록
        composable<ConversationRoute> {
            ConversationScreen(onBack = { navController.popBackStack() })
        }
    }
}
