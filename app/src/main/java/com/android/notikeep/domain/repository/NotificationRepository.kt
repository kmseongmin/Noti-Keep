package com.android.notikeep.domain.repository

import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.model.AppGroupSummary
import com.android.notikeep.domain.model.ConversationGroupSummary
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

/**
 * 알림 데이터 접근을 추상화한 Repository 인터페이스.
 * domain 레이어에만 위치하며 구현체(NotificationRepositoryImpl)는 data 레이어에 있다.
 * UseCase는 이 인터페이스에만 의존 → Android 프레임워크 의존성 없이 테스트 가능.
 */
interface NotificationRepository {

    /**
     * 앱 단위 그룹핑된 알림 목록을 페이징으로 반환.
     * @param category null이면 전체, 값이 있으면 해당 카테고리만 필터링 (예: "msg")
     */
    fun getAppGroups(category: String?): Flow<PagingData<AppGroupSummary>>

    /**
     * 특정 앱 내 대화방 단위 그룹핑된 목록을 페이징으로 반환.
     * @param packageName 조회할 앱의 패키지명
     */
    fun getConversationGroupsByApp(packageName: String): Flow<PagingData<ConversationGroupSummary>>

    /**
     * 특정 대화방의 알림 목록을 페이징으로 반환 (가장 최근순 정렬).
     * @param packageName 앱 패키지명
     * @param conversationKey 대화방 키 (단체방 이름 또는 발신자 이름)
     */
    fun getNotificationsByConversation(
        packageName: String,
        conversationKey: String
    ): Flow<PagingData<AppNotification>>

    /**
     * 특정 앱의 가장 최신 앱 이름을 실시간으로 반환.
     * AppDetail 화면 타이틀 표시에 사용.
     */
    fun getLatestAppName(packageName: String): Flow<String?>

    /**
     * 현재 DB에 저장된 모든 패키지명 목록을 한 번만 조회.
     * 홈 화면 "모두 선택" 기능에서 전체 목록 파악에 사용.
     * @param category null이면 전체, 값이 있으면 해당 카테고리만
     */
    suspend fun getAllPackageNames(category: String?): List<String>

    /**
     * 특정 앱 내 모든 conversationKey 목록을 한 번만 조회.
     * AppDetail 화면 "모두 선택" 기능에 사용.
     */
    suspend fun getAllConversationKeysByApp(packageName: String): List<String>

    /**
     * 특정 대화방의 모든 알림 ID 목록을 한 번만 조회.
     * 대화 화면 "모두 선택" 기능에 사용.
     */
    suspend fun getAllNotificationIdsByConversation(packageName: String, conversationKey: String): List<Long>

    /** 특정 앱의 모든 알림을 읽음 처리 (isRead = 1). 현재 미사용 */
    suspend fun markAppAsRead(packageName: String)

    /**
     * 특정 대화방의 모든 알림을 읽음 처리 (isRead = 1).
     * ConversationViewModel 초기화 시 호출.
     */
    suspend fun markConversationAsRead(packageName: String, conversationKey: String)

    /** 선택된 앱 패키지들의 알림 전체 삭제 */
    suspend fun deleteNotificationsByPackages(packageNames: List<String>)

    /** 선택된 대화방들의 알림 전체 삭제 */
    suspend fun deleteNotificationsByConversations(packageName: String, conversationKeys: List<String>)

    /** 선택된 알림 ID들만 삭제 */
    suspend fun deleteNotificationsByIds(ids: List<Long>)

    /** 알림 1건 저장 (NotiKeepListenerService에서 호출) */
    suspend fun saveNotification(notification: AppNotification)

    /** 알림 1건 삭제 */
    suspend fun deleteNotification(notification: AppNotification)
}
