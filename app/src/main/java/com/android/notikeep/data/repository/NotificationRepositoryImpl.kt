package com.android.notikeep.data.repository

import android.util.Log
import com.android.notikeep.data.local.dao.NotificationDao
import com.android.notikeep.data.local.entity.NotificationEntity
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.model.AppGroupSummary
import com.android.notikeep.domain.model.ConversationGroupSummary
import com.android.notikeep.domain.repository.NotificationRepository
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * NotificationRepository 구현체.
 * DAO를 주입받아 실제 DB 접근을 담당하고, data 레이어 모델을 domain 모델로 변환해 반환한다.
 */
class NotificationRepositoryImpl @Inject constructor(
    /** Room DAO. DB 쿼리의 실제 실행 주체 */
    private val dao: NotificationDao
) : NotificationRepository {

    /**
     * 앱 단위 그룹핑 목록을 페이징으로 반환.
     * - pageSize=30: 한 번에 최대 30개씩 로드
     * - enablePlaceholders=false: 아직 로드되지 않은 항목을 null로 채우지 않음
     * - DAO Row → domain Summary로 변환 후 방출
     */
    override fun getAppGroups(category: String?): Flow<PagingData<AppGroupSummary>> {
        Log.d(TAG, "getAppGroups() 호출 - category=$category")
        return Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false),
            pagingSourceFactory = { dao.getAppGroups(category) }
        ).flow.map { pagingData ->
            pagingData.map { row ->
                AppGroupSummary(
                    packageName = row.packageName,
                    appName = row.appName,
                    latestTitle = row.latestTitle,
                    latestContent = row.latestContent,
                    latestCategory = row.latestCategory,
                    latestReceivedAt = row.latestReceivedAt,
                    totalCount = row.totalCount,
                    unreadCount = row.unreadCount
                )
            }
        }
    }

    /**
     * 특정 앱의 대화방 단위 그룹핑 목록을 페이징으로 반환.
     * - pageSize=30
     * - DAO Row → domain Summary로 변환 후 방출
     */
    override fun getConversationGroupsByApp(packageName: String): Flow<PagingData<ConversationGroupSummary>> {
        Log.d(TAG, "getConversationGroupsByApp() 호출 - packageName=$packageName")
        return Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false),
            pagingSourceFactory = { dao.getConversationGroupsByApp(packageName) }
        ).flow.map { pagingData ->
            pagingData.map { row ->
                ConversationGroupSummary(
                    conversationKey = row.conversationKey,
                    packageName = row.packageName,
                    appName = row.appName,
                    latestTitle = row.latestTitle,
                    latestContent = row.latestContent,
                    latestSubText = row.latestSubText,
                    latestCategory = row.latestCategory,
                    latestReceivedAt = row.latestReceivedAt,
                    count = row.count,
                    unreadCount = row.unreadCount
                )
            }
        }
    }

    /**
     * 특정 대화방 알림 목록을 페이징으로 반환.
     * - pageSize=40: 대화 화면은 한 번에 더 많이 로드
     * - Entity → domain AppNotification으로 변환 후 방출
     */
    override fun getNotificationsByConversation(
        packageName: String,
        conversationKey: String
    ): Flow<PagingData<AppNotification>> {
        Log.d(TAG, "getNotificationsByConversation() 호출 - packageName=$packageName, conversationKey=$conversationKey")
        return Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = false),
            pagingSourceFactory = { dao.getNotificationsByConversation(packageName, conversationKey) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    /** 특정 앱의 최신 앱 이름을 실시간으로 반환 (Room이 변경 시 자동 emit) */
    override fun getLatestAppName(packageName: String): Flow<String?> =
        dao.getLatestAppName(packageName)

    /** 홈 화면 "모두 선택"용 패키지명 전체 목록 1회 조회 */
    override suspend fun getAllPackageNames(category: String?): List<String> {
        val result = dao.getAllPackageNames(category)
        Log.d(TAG, "getAllPackageNames() - category=$category, 결과 ${result.size}건")
        return result
    }

    /** AppDetail 화면 "모두 선택"용 conversationKey 전체 목록 1회 조회 */
    override suspend fun getAllConversationKeysByApp(packageName: String): List<String> {
        val result = dao.getAllConversationKeysByApp(packageName)
        Log.d(TAG, "getAllConversationKeysByApp() - packageName=$packageName, 결과 ${result.size}건")
        return result
    }

    /** Conversation 화면 "모두 선택"용 알림 ID 전체 목록 1회 조회 */
    override suspend fun getAllNotificationIdsByConversation(
        packageName: String,
        conversationKey: String
    ): List<Long> {
        val result = dao.getAllNotificationIdsByConversation(packageName, conversationKey)
        Log.d(TAG, "getAllNotificationIdsByConversation() - conversationKey=$conversationKey, 결과 ${result.size}건")
        return result
    }

    /** 특정 앱 전체 알림 읽음 처리 (현재 미사용) */
    override suspend fun markAppAsRead(packageName: String) {
        Log.d(TAG, "markAppAsRead() - packageName=$packageName")
        dao.markAppAsRead(packageName)
    }

    /**
     * 특정 대화방 전체 알림 읽음 처리.
     * ConversationViewModel.init 에서 호출 → 화면 진입 즉시 isRead=1로 UPDATE.
     */
    override suspend fun markConversationAsRead(packageName: String, conversationKey: String) {
        Log.d(TAG, "markConversationAsRead() - packageName=$packageName, conversationKey=$conversationKey")
        dao.markConversationAsRead(packageName, conversationKey)
    }

    /** 선택된 앱 패키지들의 알림 전체 삭제 */
    override suspend fun deleteNotificationsByPackages(packageNames: List<String>) {
        Log.d(TAG, "deleteNotificationsByPackages() - 대상: $packageNames")
        dao.deleteNotificationsByPackages(packageNames)
    }

    /** 선택된 대화방들의 알림 삭제 */
    override suspend fun deleteNotificationsByConversations(
        packageName: String,
        conversationKeys: List<String>
    ) {
        Log.d(TAG, "deleteNotificationsByConversations() - packageName=$packageName, 대상: $conversationKeys")
        dao.deleteNotificationsByConversations(packageName, conversationKeys)
    }

    /** 선택된 알림 ID들만 삭제 */
    override suspend fun deleteNotificationsByIds(ids: List<Long>) {
        Log.d(TAG, "deleteNotificationsByIds() - 대상 id: $ids")
        dao.deleteNotificationsByIds(ids)
    }

    /**
     * 알림 1건 저장.
     * domain AppNotification → NotificationEntity로 변환 후 INSERT.
     */
    override suspend fun saveNotification(notification: AppNotification) {
        Log.d(TAG, "saveNotification() - [${notification.appName}] ${notification.title}")
        dao.insertNotification(NotificationEntity.fromDomain(notification))
    }

    /** 알림 1건 삭제 */
    override suspend fun deleteNotification(notification: AppNotification) {
        Log.d(TAG, "deleteNotification() - id=${notification.id}")
        dao.deleteNotification(NotificationEntity.fromDomain(notification))
    }

    companion object {
        private const val TAG = "NotificationRepo"
    }
}
