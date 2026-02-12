package com.alarm.notikeep.presentation.permission.util

import android.content.Context
import android.content.Intent
import android.provider.Settings

object NotificationPermissionUtil {

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(packageName) == true
    }

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(":settings:fragment_args_key", context.packageName)
            putExtra(":settings:show_fragment_args", android.os.Bundle().apply {
                putString("package", context.packageName)
            })
        }
        context.startActivity(intent)
    }
}
