package com.android.notikeep.service

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
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
import java.io.File
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

        val senderIconPath = saveLargeIcon(sbn)

        val notification = AppNotification(
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            content = content,
            subText = subText,
            category = sbn.notification.category,
            receivedAt = sbn.postTime,
            senderIconPath = senderIconPath
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

    private fun saveLargeIcon(sbn: StatusBarNotification): String? {
        return runCatching {
            val icon: Icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                sbn.notification.getLargeIcon() ?: return null
            } else {
                return null
            }
            val drawable = icon.loadDrawable(this) ?: return null
            val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return null

            // title을 기반으로 발신자별 파일명 생성 (동일 발신자는 덮어씀)
            val safeTitle = sbn.notification.extras.getString("android.title")
                ?.replace(Regex("[^a-zA-Z0-9가-힣]"), "_")
                ?.take(40)
                ?: return null
            val dir = File(filesDir, "profiles/${sbn.packageName}")
            dir.mkdirs()
            val file = File(dir, "$safeTitle.png")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            file.absolutePath
        }.getOrNull()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        private const val TAG = "NotiKeepService"
    }
}
