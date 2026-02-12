package com.alarm.notikeep.service

import android.app.Notification
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.alarm.notikeep.di.IoDispatcher
import com.alarm.notikeep.domain.model.NotificationItem
import com.alarm.notikeep.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import androidx.core.graphics.createBitmap

@AndroidEntryPoint
class NotiKeepListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    private lateinit var serviceScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // Android 시스템 알림 수신 시, 필요한 필드를 추출해서 저장 계층으로 전달한다.
        serviceScope.launch {
            try {
                val notification = sbn.notification
                val packageName = sbn.packageName
                val appName = getAppName(packageName)

                val title = notification.extras.getString("android.title")
                val content = notification.extras.getCharSequence("android.text")?.toString()
                val category = notification.category
                val conversationTitle = notification.extras.getCharSequence("android.conversationTitle")?.toString()
                val subText = notification.extras.getCharSequence("android.subText")?.toString()
                val summaryText = notification.extras.getCharSequence("android.summaryText")?.toString()
                val bigTitle = notification.extras.getCharSequence("android.title.big")?.toString()
                val isGroupConversation = notification.extras.getBoolean("android.isGroupConversation", false)
                val conversationKey = buildConversationKey(
                    sbn = sbn,
                    title = title,
                    conversationTitle = conversationTitle,
                    subText = subText,
                    summaryText = summaryText,
                    bigTitle = bigTitle,
                    isGroupConversation = isGroupConversation
                )
                val normalizedTitle = when {
                    conversationKey?.startsWith("group:") == true ->
                        conversationKey.removePrefix("group:").takeIf { it.isNotBlank() } ?: title
                    else -> title
                }

                Log.d(
                    TAG,
                    "posted: package=${sbn.packageName}, id=${sbn.id}, tag=${sbn.tag}, key=${sbn.key}, groupKey=${sbn.groupKey}, ongoing=${sbn.isOngoing}, category=$category, isGroup=$isGroupConversation, conversationKey=$conversationKey, title=$title, content=$content"
                )

                if (title.isNullOrBlank() && content.isNullOrBlank()) {
                    Log.d(
                        TAG,
                        "skip empty payload: package=${sbn.packageName}, id=${sbn.id}, key=${sbn.key}"
                    )
                    return@launch
                }

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

                val attachment = extractAttachment(notification)

                val notificationItem = NotificationItem(
                    id = 0,
                    packageName = packageName,
                    appName = appName,
                    title = normalizedTitle,
                    content = content,
                    category = category,
                    conversationKey = conversationKey,
                    timestamp = sbn.postTime,
                    attachmentData = attachment?.data,
                    attachmentMimeType = attachment?.mimeType,
                    attachmentFileName = attachment?.fileName,
                    iconData = iconData
                )

                // 실제 중복 판정/저장 여부 결정은 Repository에서 수행한다.
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
        if (::serviceScope.isInitialized) {
            serviceScope.cancel()
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun buildConversationKey(
        sbn: StatusBarNotification,
        title: String?,
        conversationTitle: String?,
        subText: String?,
        summaryText: String?,
        bigTitle: String?,
        isGroupConversation: Boolean
    ): String? {
        if (isGroupConversation) {
            val roomName = firstNonBlank(
                conversationTitle,
                summaryText,
                bigTitle,
                subText
            )
            if (!roomName.isNullOrBlank() && roomName != title) {
                return "group:$roomName"
            }

            val groupFallback = firstNonBlank(sbn.groupKey, sbn.tag, sbn.key)
            return groupFallback?.let { "group:$it" }
        }

        val dmName = firstNonBlank(conversationTitle, title, subText)
        return dmName?.let { "dm:$it" }
    }

    private fun firstNonBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }
    }

    private fun extractAttachment(notification: Notification): AttachmentPayload? {
        // 1) BigPicture 스타일 이미지
        val picture = notification.extras.get(Notification.EXTRA_PICTURE) as? Bitmap
        if (picture != null) {
            return AttachmentPayload(
                data = bitmapToPngBytes(picture),
                mimeType = "image/png",
                fileName = "notification_image_${System.currentTimeMillis()}.png"
            )
        }

        // 2) MessagingStyle data uri (사진/파일)
        val messageAttachment = extractFromMessagingStyle(notification)
        if (messageAttachment != null) return messageAttachment

        // 3) 일반 stream uri 첨부
        val streamUri = notification.extras.get("android.stream") as? Uri
        if (streamUri != null) {
            return copyUriToPayload(streamUri, null)
        }

        return null
    }

    private fun extractFromMessagingStyle(notification: Notification): AttachmentPayload? {
        return try {
            val rawMessages = notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return null
            val messages = Notification.MessagingStyle.Message.getMessagesFromBundleArray(rawMessages)
            val latestWithData = messages.lastOrNull { it.dataUri != null } ?: return null
            copyUriToPayload(latestWithData.dataUri!!, latestWithData.dataMimeType)
        } catch (_: Exception) {
            null
        }
    }

    private fun copyUriToPayload(uri: Uri, explicitMimeType: String?): AttachmentPayload? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val data = inputStream.readBytesUpTo(MAX_ATTACHMENT_BYTES)
            if (data.isEmpty()) return null

            val mimeType = explicitMimeType ?: contentResolver.getType(uri) ?: "application/octet-stream"
            val fileName = uri.lastPathSegment ?: "attachment_${System.currentTimeMillis()}"
            AttachmentPayload(data = data, mimeType = mimeType, fileName = fileName)
        } catch (_: Exception) {
            null
        }
    }

    private fun bitmapToPngBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun InputStream.readBytesUpTo(maxBytes: Int): ByteArray {
        use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            val output = ByteArrayOutputStream()
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                val remaining = maxBytes - total
                if (remaining <= 0) break
                val toWrite = minOf(read, remaining)
                output.write(buffer, 0, toWrite)
                total += toWrite
                if (total >= maxBytes) break
            }
            return output.toByteArray()
        }
    }

    private data class AttachmentPayload(
        val data: ByteArray,
        val mimeType: String,
        val fileName: String
    )

    companion object {
        private const val TAG = "NotiKeepListener"
        private const val MAX_ATTACHMENT_BYTES = 8 * 1024 * 1024
    }
}
