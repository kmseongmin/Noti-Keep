package com.android.notikeep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.notikeep.domain.model.AppNotification

/**
 * Room DB 테이블 "notifications"에 대응하는 Entity.
 * 한 행 = 알림 1건.
 * presentation 레이어에서 직접 사용하지 않고 toDomain()으로 변환 후 사용한다.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    /** PK. Room이 INSERT 시 자동 증가 */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 알림 발송 앱 패키지명 (예: "com.kakao.talk") */
    val packageName: String,

    /** 사람이 읽을 수 있는 앱 이름 */
    val appName: String,

    /** 보낸 사람 이름 또는 알림 제목 */
    val title: String,

    /** 알림 본문. bigText 우선, 없으면 text */
    val content: String,

    /** 단체 채팅방 이름. 1:1이면 null */
    val subText: String?,

    /** Android 표준 알림 카테고리 문자열 */
    val category: String?,

    /** 알림 수신 시각 (밀리초 타임스탬프) */
    val receivedAt: Long,

    /** 읽음 여부. 기본값 false(안읽음). ConversationScreen 진입 시 true로 업데이트 */
    val isRead: Boolean = false,

    /** 발신자 프로필 이미지 저장 경로 (앱 내부 저장소). 없으면 null */
    val senderIconPath: String? = null,

    /** 첨부 미디어 파일 저장 경로 (앱 내부 저장소). 없으면 null */
    val mediaPath: String? = null,

    /** 미디어 MIME 타입 (예: "image/png"). 없으면 null */
    val mediaMimeType: String? = null
) {
    /** Entity → 도메인 모델 변환. Repository에서 Flow 방출 전 호출 */
    fun toDomain() = AppNotification(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        content = content,
        subText = subText,
        category = category,
        receivedAt = receivedAt,
        isRead = isRead,
        senderIconPath = senderIconPath,
        mediaPath = mediaPath,
        mediaMimeType = mediaMimeType
    )

    companion object {
        /** 도메인 모델 → Entity 변환. Repository에서 DB 저장 전 호출 */
        fun fromDomain(n: AppNotification) = NotificationEntity(
            id = n.id,
            packageName = n.packageName,
            appName = n.appName,
            title = n.title,
            content = n.content,
            subText = n.subText,
            category = n.category,
            receivedAt = n.receivedAt,
            isRead = n.isRead,
            senderIconPath = n.senderIconPath,
            mediaPath = n.mediaPath,
            mediaMimeType = n.mediaMimeType
        )
    }
}
