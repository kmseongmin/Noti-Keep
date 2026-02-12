package com.alarm.notikeep.service

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.alarm.notikeep.domain.model.NotificationItem
import com.alarm.notikeep.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import androidx.core.graphics.createBitmap

@AndroidEntryPoint
class NotiKeepListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        serviceScope.launch {
            try {
                val notification = sbn.notification
                val packageName = sbn.packageName
                val appName = getAppName(packageName)

                val title = notification.extras.getString("android.title")
                val content = notification.extras.getCharSequence("android.text")?.toString()

                val iconData = try {
                    val icon = notification.smallIcon?.loadDrawable(this@NotiKeepListenerService)
                    icon?.let { drawable ->
                        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)

                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.toByteArray()
                    }
                } catch (e: Exception) {
                    null
                }

                val notificationItem = NotificationItem(
                    id = 0,
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    content = content,
                    timestamp = sbn.postTime,
                    iconData = iconData
                )

                notificationRepository.saveNotification(notificationItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
