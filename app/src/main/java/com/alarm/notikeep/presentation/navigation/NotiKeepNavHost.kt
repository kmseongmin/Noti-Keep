package com.alarm.notikeep.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.alarm.notikeep.presentation.notification_detail.NotificationDetailScreen
import com.alarm.notikeep.presentation.notification_list.NotificationListScreen
import com.alarm.notikeep.presentation.permission.PermissionScreen
import com.alarm.notikeep.presentation.permission.util.NotificationPermissionUtil

@Composable
fun NotiKeepNavHost(
    onAppExit: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = rememberNavController()

    val initialRoute: NotiKeepNavRoute = if (NotificationPermissionUtil.isNotificationListenerEnabled(context)) {
        NotiKeepNavRoute.NotificationList
    } else {
        NotiKeepNavRoute.Permission
    }

    NavHost(
        navController = navController,
        startDestination = initialRoute,
    ) {
        composable<NotiKeepNavRoute.Permission> {
            PermissionScreen(
                onRequestPermission = {
                    NotificationPermissionUtil.openNotificationListenerSettings(context)
                },
                onAppExit = onAppExit
            )
        }
        composable<NotiKeepNavRoute.NotificationList> {
            NotificationListScreen(
                onOpenGroupDetail = { group ->
                    navController.navigate(
                        NotiKeepNavRoute.NotificationGroupDetail(
                            packageName = group.packageName,
                            threadKey = group.threadKey,
                            title = group.title
                        )
                    )
                }
            )
        }
        composable<NotiKeepNavRoute.NotificationGroupDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NotiKeepNavRoute.NotificationGroupDetail>()
            NotificationDetailScreen(
                packageName = route.packageName,
                threadKey = route.threadKey,
                title = route.title,
                onBack = { navController.popBackStack() }
            )
        }
    }

    DisposableEffect(lifecycleOwner, context, navController) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val hasPermission = NotificationPermissionUtil.isNotificationListenerEnabled(context)
                val isPermissionScreen =
                    navController.currentDestination?.hasRoute<NotiKeepNavRoute.Permission>() == true

                if (hasPermission && isPermissionScreen) {
                    navController.navigate(NotiKeepNavRoute.NotificationList) {
                        popUpTo<NotiKeepNavRoute.Permission> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
