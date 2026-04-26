package com.android.notikeep.domain.model

/**
 * 단일 알림을 나타내는 도메인 모델.
 * DB Entity → toDomain() 변환을 거쳐 presentation 레이어까지 전달된다.
 */
data class AppNotification(
    /** DB auto-generated PK. 새 알림 생성 시 기본값 0 */
    val id: Long = 0,

    /** 알림을 발송한 앱의 패키지명 (예: "com.kakao.talk") */
    val packageName: String,

    /** 사람이 읽을 수 있는 앱 이름 (예: "카카오톡") */
    val appName: String,

    /** 보낸 사람 이름. 단체 채팅방의 경우 실제 발신자 이름 */
    val title: String,

    /** 메시지 본문 내용 */
    val content: String,

    /**
     * 단체 채팅방 이름.
     * - 단체 채팅: 방 이름 (예: "개발팀 단톡")
     * - 1:1 채팅 / 일반 알림: null
     */
    val subText: String?,

    /**
     * 알림 카테고리 (Android 표준 상수).
     * 예: Notification.CATEGORY_MESSAGE = "msg"
     * null 이면 카테고리 미분류
     */
    val category: String?,

    /** 알림이 수신된 시각 (System.currentTimeMillis() 기준 밀리초) */
    val receivedAt: Long,

    /** 읽음 여부. false = 안읽음, true = 읽음 */
    val isRead: Boolean = false,

    /** 발신자 프로필 이미지를 앱 내부 저장소에 저장한 절대 경로. 없으면 null */
    val senderIconPath: String? = null,

    /** 미디어(이미지·동영상 등)를 앱 내부 저장소에 저장한 절대 경로. 없으면 null */
    val mediaPath: String? = null,

    /** mediaPath 파일의 MIME 타입 (예: "image/png", "image/jpeg"). 없으면 null */
    val mediaMimeType: String? = null
) {
    /**
     * 대화 그룹핑 키.
     * - subText(단체방 이름)가 있으면 방 이름으로 묶음
     * - 없으면 title(보낸 사람)로 묶음
     * → AppDetail 화면에서 대화 목록을 그룹핑할 때 사용
     */
    val conversationKey: String get() = subText?.takeIf { it.isNotBlank() } ?: title
}
