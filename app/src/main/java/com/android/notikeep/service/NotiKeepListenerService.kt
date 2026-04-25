package com.android.notikeep.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.usecase.SaveNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotiKeepListenerService : NotificationListenerService() {

    @Inject
    lateinit var saveNotificationUseCase: SaveNotificationUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString("android.title").orEmpty()
        val content = extras.getCharSequence("android.text")?.toString().orEmpty()

        // 제목과 내용이 모두 비어있으면 저장하지 않음
        if (title.isBlank() && content.isBlank()) return

        val appName = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        }.getOrDefault(sbn.packageName)

        scope.launch {
            saveNotificationUseCase(
                AppNotification(
                    packageName = sbn.packageName,
                    appName = appName,
                    title = title,
                    content = content,
                    receivedAt = sbn.postTime,
                    category = sbn.notification.category
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
