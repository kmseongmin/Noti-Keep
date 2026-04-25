package com.android.notikeep.service

import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
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

        logRawNotification(sbn, extras)

        val title = extras.getString("android.title").orEmpty()
        val content = (extras.getCharSequence("android.bigText")
            ?: extras.getCharSequence("android.text"))?.toString().orEmpty()
        val subText = extras.getCharSequence("android.subText")?.toString()

        if (title.isBlank() && content.isBlank()) {
            Log.d(TAG, "⛔ 저장 건너뜀: title/content 모두 비어있음 (${sbn.packageName})")
            return
        }

        val appName = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        }.getOrDefault(sbn.packageName)

        val notification = AppNotification(
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            content = content,
            subText = subText,
            category = sbn.notification.category,
            receivedAt = sbn.postTime
        )

        Log.d(TAG, """
            ✅ 저장할 데이터
            ├ packageName : ${notification.packageName}
            ├ appName     : ${notification.appName}
            ├ category    : ${notification.category}
            ├ title       : ${notification.title}
            ├ subText     : ${notification.subText}
            ├ content     : ${notification.content}
            └ receivedAt  : ${notification.receivedAt}
        """.trimIndent())

        scope.launch {
            runCatching { saveNotificationUseCase(notification) }
                .onSuccess { Log.d(TAG, "💾 DB 저장 완료: [${notification.appName}] ${notification.title}") }
                .onFailure { Log.e(TAG, "❌ DB 저장 실패: ${it.message}", it) }
        }
    }

    private fun logRawNotification(sbn: StatusBarNotification, extras: Bundle) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📩 알림 수신: ${sbn.packageName}")
        Log.d(TAG, "├ id         : ${sbn.id}")
        Log.d(TAG, "├ tag        : ${sbn.tag}")
        Log.d(TAG, "├ postTime   : ${sbn.postTime}")
        Log.d(TAG, "├ isClearable: ${sbn.isClearable}")
        Log.d(TAG, "├ isOngoing  : ${sbn.isOngoing}")
        Log.d(TAG, "├ category   : ${sbn.notification.category}")
        Log.d(TAG, "└ extras:")
        extras.keySet().forEach { key ->
            Log.d(TAG, "    [$key] = ${extras.get(key)}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        private const val TAG = "NotiKeep"
    }
}
