package com.alarm.notikeep.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alarm.notikeep.presentation.notification_list.NotificationListScreen
import com.alarm.notikeep.presentation.permission.PermissionScreen
import com.alarm.notikeep.presentation.permission.util.NotificationPermissionUtil

@Composable
fun NotiKeepNavHost(
    onAppExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = rememberNavController()

    val initialRoute: NotiKeepNavRoute = if (NotificationPermissionUtil.isNotificationListenerEnabled(context)) {
        NotiKeepNavRoute.NotificationList
    } else {
        NotiKeepNavRoute.Permission
    }

    DisposableEffect(lifecycleOwner, context, navController) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val targetRoute: NotiKeepNavRoute = if (NotificationPermissionUtil.isNotificationListenerEnabled(context)) {
                    NotiKeepNavRoute.NotificationList
                } else {
                    NotiKeepNavRoute.Permission
                }
                val isOnTarget = when (targetRoute) {
                    NotiKeepNavRoute.Permission ->
                        navController.currentDestination?.hasRoute<NotiKeepNavRoute.Permission>() == true
                    NotiKeepNavRoute.NotificationList ->
                        navController.currentDestination?.hasRoute<NotiKeepNavRoute.NotificationList>() == true
                }
                if (!isOnTarget) {
                    navController.navigate(targetRoute) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
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

    NavHost(
        navController = navController,
        startDestination = initialRoute,
        modifier = modifier
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
            NotificationListScreen()
        }
    }
}
