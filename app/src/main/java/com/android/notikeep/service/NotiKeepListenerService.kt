package com.android.notikeep.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.webkit.MimeTypeMap
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.usecase.SaveNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * 시스템 알림을 수신해 DB에 저장하는 백그라운드 서비스.
 * NotificationListenerService: 알림 접근 권한이 허용된 경우 모든 앱의 알림을 수신할 수 있음.
 *
 * Hilt로 SaveNotificationUseCase를 주입받기 위해 @AndroidEntryPoint 사용.
 */
@AndroidEntryPoint
class NotiKeepListenerService : NotificationListenerService() {

    /** 알림을 DB에 저장하는 UseCase. Hilt가 주입 */
    @Inject
    lateinit var saveNotificationUseCase: SaveNotificationUseCase

    /**
     * 서비스 생명주기에 맞는 코루틴 스코프.
     * - SupervisorJob: 자식 코루틴 하나 실패해도 다른 코루틴에 영향 없음
     * - Dispatchers.IO: DB/파일 I/O 작업에 최적화된 스레드풀
     * onDestroy()에서 cancel() 호출로 정리
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 새 알림이 상태바에 등록될 때 시스템이 호출.
     * 알림 데이터를 파싱 → 미디어 저장 → DB 저장 순으로 처리.
     *
     * @param sbn 시스템으로부터 받은 상태바 알림 객체
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras

        // 원본 알림 데이터를 로그로 전체 출력 (디버깅용)
        logRawNotification(sbn, extras)

        // 알림 핵심 텍스트 추출
        val title = extras.getString("android.title").orEmpty()
        // bigText: 긴 메시지 전체 내용. 없으면 android.text(짧은 내용) 사용
        val content = (extras.getCharSequence("android.bigText")
            ?: extras.getCharSequence("android.text"))?.toString().orEmpty()
        // subText: 단체 채팅방 이름. 없으면 null
        val subText = extras.getCharSequence("android.subText")?.toString()

        // title과 content가 모두 비어있으면 저장 의미 없음 → 스킵
        if (title.isBlank() && content.isBlank()) {
            Log.d(TAG, "⛔ 저장 건너뜀: title/content 모두 비어있음 (${sbn.packageName})")
            return
        }

        // 패키지명으로 사용자 친화적 앱 이름 조회. 실패 시 패키지명 그대로 사용
        val appName = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        }.getOrDefault(sbn.packageName)

        // 발신자 프로필 이미지(largeIcon)를 내부 저장소에 저장
        val senderIconPath = saveLargeIcon(sbn)
        // 미디어 첨부파일(이미지/동영상 등)을 내부 저장소에 저장
        val mediaAttachment = saveMediaAttachment(sbn)

        val notification = AppNotification(
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            content = content,
            subText = subText,
            category = sbn.notification.category,
            receivedAt = sbn.postTime,   // 알림이 시스템에 등록된 시각
            senderIconPath = senderIconPath,
            mediaPath = mediaAttachment?.first,
            mediaMimeType = mediaAttachment?.second
        )

        Log.d(TAG, """
            ✅ 저장할 데이터
            ├ packageName : ${notification.packageName}
            ├ appName     : ${notification.appName}
            ├ category    : ${notification.category}
            ├ title       : ${notification.title}
            ├ subText     : ${notification.subText}
            ├ content     : ${notification.content}
            ├ mediaPath   : ${notification.mediaPath}
            ├ mediaMime   : ${notification.mediaMimeType}
            └ receivedAt  : ${notification.receivedAt}
        """.trimIndent())

        // IO 스레드에서 DB 저장 실행. runCatching으로 예외를 잡아 앱 크래시 방지
        scope.launch {
            runCatching { saveNotificationUseCase(notification) }
                .onSuccess { Log.d(TAG, "💾 DB 저장 완료: [${notification.appName}] ${notification.title}") }
                .onFailure { Log.e(TAG, "❌ DB 저장 실패: ${it.message}", it) }
        }
    }

    /**
     * 수신된 알림의 모든 원본 데이터를 로그로 출력.
     * 어떤 extras 키가 전달되는지 파악할 때 유용.
     */
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

    /**
     * 알림의 largeIcon(발신자 프로필 이미지)을 내부 저장소에 PNG로 저장.
     *
     * 저장 경로: {filesDir}/profiles/{packageName}/{safeTitle}.png
     * - 같은 발신자 이름이면 덮어쓰기 → 최신 프로필 이미지 유지
     * - API 23(M) 미만은 미지원 → null 반환
     *
     * @return 저장된 파일 절대 경로. 실패 또는 미지원 시 null
     */
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
            Log.d(TAG, "프로필 이미지 저장: ${file.absolutePath}")
            file.absolutePath
        }.getOrNull()
    }

    /**
     * 알림에 첨부된 미디어(이미지/동영상 등)를 내부 저장소에 저장.
     *
     * 우선순위:
     * 1. MessagingStyle 메시지 내 dataUri (카카오톡 등 메시지 앱의 사진/영상)
     * 2. BigPicture 스타일의 비트맵 이미지
     *
     * @return (저장된 파일 경로, MIME 타입) 쌍. 미디어 없거나 실패 시 null
     */
    private fun saveMediaAttachment(sbn: StatusBarNotification): Pair<String, String>? {
        val fromMessaging = saveFromMessagingStyleData(sbn)
        if (fromMessaging != null) return fromMessaging

        val fromPicture = saveFromBigPicture(sbn)
        if (fromPicture != null) return fromPicture

        return null
    }

    /**
     * MessagingStyle 메시지 배열에서 가장 최근 미디어 첨부파일을 저장.
     * Notification.EXTRA_MESSAGES 배열을 역순으로 탐색 → dataUri가 있는 첫 번째 메시지 사용.
     *
     * @return (저장 경로, MIME 타입) 쌍. 미디어 없거나 실패 시 null
     */
    private fun saveFromMessagingStyleData(sbn: StatusBarNotification): Pair<String, String>? {
        val raw = sbn.notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return null
        val messages = Notification.MessagingStyle.Message.getMessagesFromBundleArray(raw)
        // 최신 메시지부터 역순으로 탐색 (asReversed: 복사 없이 역순 뷰 반환)
        val target = messages
            .asReversed()
            .firstOrNull { it.dataUri != null && !it.dataMimeType.isNullOrBlank() }
            ?: return null

        val uri = target.dataUri ?: return null
        val mime = target.dataMimeType ?: return null
        val savedPath = copyContentUriToInternal(
            uri = uri,
            mimeType = mime,
            packageName = sbn.packageName,
            filePrefix = "msg_${sbn.postTime}_${sbn.id}"
        ) ?: return null
        return savedPath to mime
    }

    /**
     * BigPicture 스타일 알림의 이미지 비트맵을 내부 저장소에 PNG로 저장.
     * 저장 경로: {filesDir}/media/{packageName}/pic_{postTime}_{id}.png
     *
     * @return (저장 경로, "image/png") 쌍. 이미지 없거나 실패 시 null
     */
    private fun saveFromBigPicture(sbn: StatusBarNotification): Pair<String, String>? {
        val picture = sbn.notification.extras.get(Notification.EXTRA_PICTURE) as? Bitmap ?: return null
        val dir = File(filesDir, "media/${sbn.packageName}").apply { mkdirs() }
        val file = File(dir, "pic_${sbn.postTime}_${sbn.id}.png")
        runCatching {
            FileOutputStream(file).use { out ->
                picture.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
        }.onFailure {
            Log.e(TAG, "❌ 이미지 저장 실패: ${it.message}", it)
            return null
        }
        Log.d(TAG, "BigPicture 이미지 저장: ${file.absolutePath}")
        return file.absolutePath to "image/png"
    }

    /**
     * content:// URI의 데이터를 앱 내부 저장소 파일로 복사.
     * 저장 경로: {filesDir}/media/{packageName}/{filePrefix}{확장자}
     *
     * @param uri 복사할 원본 content URI
     * @param mimeType MIME 타입 (확장자 결정에 사용)
     * @param packageName 저장 디렉토리 분류용 패키지명
     * @param filePrefix 파일명 앞부분 (보통 "msg_{postTime}_{id}" 형식)
     * @return 저장된 파일 절대 경로. 실패 시 null
     */
    private fun copyContentUriToInternal(
        uri: Uri,
        mimeType: String,
        packageName: String,
        filePrefix: String
    ): String? {
        // MIME 타입으로 확장자 결정 (예: "image/jpeg" → ".jpg")
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.let { ".$it" } ?: ""
        val dir = File(filesDir, "media/$packageName").apply { mkdirs() }
        val file = File(dir, "$filePrefix$ext")
        return runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            Log.d(TAG, "미디어 복사 완료: ${file.absolutePath}")
            file.absolutePath
        }.onFailure {
            Log.e(TAG, "❌ 미디어 복사 실패: uri=$uri, mime=$mimeType, err=${it.message}", it)
        }.getOrNull()
    }

    /**
     * 서비스 종료 시 코루틴 스코프를 취소해 진행 중인 IO 작업을 정리.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() - scope 취소")
        scope.cancel()
    }

    companion object {
        private const val TAG = "NotiKeepService"
    }
}
