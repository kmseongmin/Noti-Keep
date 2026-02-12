package com.alarm.notikeep.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alarm.notikeep.presentation.notification_list.NotificationListScreen
import com.alarm.notikeep.presentation.permission.PermissionScreen
import com.alarm.notikeep.presentation.permission.util.NotificationPermissionUtil

@Composable
fun NotiKeepNavHost(
    onAppExit: () -> Unit,
) {
    val context = LocalContext.current
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
            NotificationListScreen()
        }
    }
}
