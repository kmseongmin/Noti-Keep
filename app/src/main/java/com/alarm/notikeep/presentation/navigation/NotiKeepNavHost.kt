package com.alarm.notikeep.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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

    val initialRoute = if (NotificationPermissionUtil.isNotificationListenerEnabled(context)) {
        NotiKeepRoute.NotificationList
    } else {
        NotiKeepRoute.Permission
    }

    DisposableEffect(lifecycleOwner, context, navController) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val targetRoute = if (NotificationPermissionUtil.isNotificationListenerEnabled(context)) {
                    NotiKeepRoute.NotificationList
                } else {
                    NotiKeepRoute.Permission
                }
                if (navController.currentDestination?.route != targetRoute) {
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
        startDestination = remember { initialRoute },
        modifier = modifier
    ) {
        composable(NotiKeepRoute.Permission) {
            PermissionScreen(
                onRequestPermission = {
                    NotificationPermissionUtil.openNotificationListenerSettings(context)
                },
                onAppExit = onAppExit
            )
        }
        composable(NotiKeepRoute.NotificationList) {
            NotificationListScreen()
        }
    }
}
