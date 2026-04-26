package com.android.notikeep.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * 홈 화면 라우트. 파라미터 없음.
 * NavHost의 startDestination으로 사용.
 */
@Serializable
data object HomeRoute

/**
 * 앱 상세 화면(대화방 목록) 라우트.
 * @param packageName 표시할 앱의 패키지명. NavGraph에서 AppDetailRoute(packageName)로 생성해 전달.
 */
@Serializable
data class AppDetailRoute(
    val packageName: String
)

/**
 * 대화 내역 화면 라우트.
 * @param packageName 앱 패키지명
 * @param conversationKey 대화방 키 (단체방 이름 또는 발신자 이름)
 */
@Serializable
data class ConversationRoute(
    val packageName: String,
    val conversationKey: String
)
