package com.android.notikeep.domain.model

data class AppNotification(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,       // 보낸 사람 이름
    val content: String,     // 메시지 내용
    val subText: String?,    // 단톡방 이름 (없으면 null)
    val category: String?,
    val receivedAt: Long,
    val isRead: Boolean = false,
    val senderIconPath: String? = null
) {
    // 대화 그룹핑 키: 단톡방이면 방 이름, 개인이면 보낸 사람 이름
    val conversationKey: String get() = subText?.takeIf { it.isNotBlank() } ?: title
}
