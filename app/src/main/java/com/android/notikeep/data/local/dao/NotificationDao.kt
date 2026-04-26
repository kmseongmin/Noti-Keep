package com.android.notikeep.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.notikeep.data.local.entity.NotificationEntity
import com.android.notikeep.data.local.model.AppGroupRow
import com.android.notikeep.data.local.model.ConversationGroupRow
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    /**
     * 홈 화면용: 앱 단위로 그룹핑된 알림 목록을 페이징으로 반환.
     *
     * 동작 방식:
     * - 각 packageName 별로 가장 최신(receivedAt DESC, id DESC) 알림 1건을 대표 행으로 선택
     * - 서브쿼리로 totalCount(전체 개수), unreadCount(isRead=0 개수)를 집계
     * - category 파라미터가 null이면 전체, 값이 있으면 해당 카테고리만 필터링
     * - 결과는 최신 알림 기준 내림차순 정렬
     *
     * @param category 필터링할 카테고리. null = 전체, "msg" = 메시지만
     */
    @Query(
        """
        SELECT
            n.packageName AS packageName,
            n.appName AS appName,
            n.title AS latestTitle,
            n.content AS latestContent,
            n.category AS latestCategory,
            n.receivedAt AS latestReceivedAt,
            (
                SELECT COUNT(*)
                FROM notifications c
                WHERE c.packageName = n.packageName
                AND (:category IS NULL OR c.category = :category)
            ) AS totalCount,
            (
                SELECT COUNT(*)
                FROM notifications c
                WHERE c.packageName = n.packageName
                AND c.isRead = 0
                AND (:category IS NULL OR c.category = :category)
            ) AS unreadCount
        FROM notifications n
        WHERE (:category IS NULL OR n.category = :category)
        AND n.id = (
            SELECT c.id
            FROM notifications c
            WHERE c.packageName = n.packageName
            AND (:category IS NULL OR c.category = :category)
            ORDER BY c.receivedAt DESC, c.id DESC
            LIMIT 1
        )
        ORDER BY n.receivedAt DESC, n.id DESC
        """
    )
    fun getAppGroups(category: String?): PagingSource<Int, AppGroupRow>

    /**
     * AppDetail 화면용: 특정 앱 내에서 대화방 단위로 그룹핑된 목록을 페이징으로 반환.
     *
     * 동작 방식:
     * - conversationKey = subText(단체방 이름)가 있으면 subText, 없으면 title(발신자)
     * - 각 conversationKey 별로 가장 최신 알림 1건을 대표 행으로 선택
     * - 서브쿼리로 count(전체 개수), unreadCount(안읽은 개수)를 집계
     * - 결과는 최신 알림 기준 내림차순 정렬
     *
     * @param packageName 조회할 앱의 패키지명
     */
    @Query(
        """
        SELECT
            CASE
                WHEN n.subText IS NOT NULL AND n.subText != '' THEN n.subText
                ELSE n.title
            END AS conversationKey,
            n.packageName AS packageName,
            n.appName AS appName,
            n.title AS latestTitle,
            n.content AS latestContent,
            n.subText AS latestSubText,
            n.category AS latestCategory,
            n.receivedAt AS latestReceivedAt,
            (
                SELECT COUNT(*)
                FROM notifications c
                WHERE c.packageName = :packageName
                AND (
                    CASE
                        WHEN c.subText IS NOT NULL AND c.subText != '' THEN c.subText
                        ELSE c.title
                    END
                ) = (
                    CASE
                        WHEN n.subText IS NOT NULL AND n.subText != '' THEN n.subText
                        ELSE n.title
                    END
                )
            ) AS count
            ,
            (
                SELECT COUNT(*)
                FROM notifications c
                WHERE c.packageName = :packageName
                AND c.isRead = 0
                AND (
                    CASE
                        WHEN c.subText IS NOT NULL AND c.subText != '' THEN c.subText
                        ELSE c.title
                    END
                ) = (
                    CASE
                        WHEN n.subText IS NOT NULL AND n.subText != '' THEN n.subText
                        ELSE n.title
                    END
                )
            ) AS unreadCount
        FROM notifications n
        WHERE n.packageName = :packageName
        AND n.id = (
            SELECT c2.id
            FROM notifications c2
            WHERE c2.packageName = :packageName
            AND (
                CASE
                    WHEN c2.subText IS NOT NULL AND c2.subText != '' THEN c2.subText
                    ELSE c2.title
                END
            ) = (
                CASE
                    WHEN n.subText IS NOT NULL AND n.subText != '' THEN n.subText
                    ELSE n.title
                END
            )
            ORDER BY c2.receivedAt DESC, c2.id DESC
            LIMIT 1
        )
        ORDER BY n.receivedAt DESC, n.id DESC
        """
    )
    fun getConversationGroupsByApp(packageName: String): PagingSource<Int, ConversationGroupRow>

    /**
     * Conversation 화면용: 특정 대화방의 알림 목록을 페이징으로 반환 (최신순).
     *
     * conversationKey 매칭 규칙:
     * - subText가 null이 아니고 conversationKey와 같으면 → 단체 채팅방 알림
     * - subText가 null이고 title이 conversationKey와 같으면 → 1:1 채팅 알림
     *
     * @param packageName 앱 패키지명
     * @param conversationKey 대화방 키 (단체방 이름 또는 발신자 이름)
     */
    @Query("""
        SELECT * FROM notifications
        WHERE packageName = :packageName
        AND (
            (subText IS NOT NULL AND subText = :conversationKey) OR
            (subText IS NULL AND title = :conversationKey)
        )
        ORDER BY receivedAt DESC, id DESC
    """)
    fun getNotificationsByConversation(
        packageName: String,
        conversationKey: String
    ): PagingSource<Int, NotificationEntity>

    /**
     * AppDetail 화면 타이틀 표시용: 가장 최근에 저장된 알림의 앱 이름을 실시간으로 반환.
     * Room이 데이터 변경 시 자동으로 새 값을 emit.
     */
    @Query("SELECT appName FROM notifications WHERE packageName = :packageName ORDER BY receivedAt DESC, id DESC LIMIT 1")
    fun getLatestAppName(packageName: String): Flow<String?>

    /**
     * 홈 화면 "모두 선택" 전체 목록 파악용: 현재 DB에 있는 모든 패키지명 반환.
     * @param category null이면 전체, 값이 있으면 해당 카테고리만
     */
    @Query(
        """
        SELECT DISTINCT packageName
        FROM notifications
        WHERE (:category IS NULL OR category = :category)
        """
    )
    suspend fun getAllPackageNames(category: String?): List<String>

    /**
     * AppDetail 화면 "모두 선택" 전체 목록 파악용: 특정 앱의 모든 conversationKey 반환.
     * conversationKey 계산 방식은 getConversationGroupsByApp()과 동일.
     */
    @Query(
        """
        SELECT DISTINCT
            CASE
                WHEN subText IS NOT NULL AND subText != '' THEN subText
                ELSE title
            END AS conversationKey
        FROM notifications
        WHERE packageName = :packageName
        """
    )
    suspend fun getAllConversationKeysByApp(packageName: String): List<String>

    /**
     * Conversation 화면 "모두 선택" 전체 목록 파악용: 특정 대화방의 모든 알림 ID 반환.
     * getNotificationsByConversation()과 동일한 WHERE 조건 사용.
     */
    @Query(
        """
        SELECT id
        FROM notifications
        WHERE packageName = :packageName
        AND (
            (subText IS NOT NULL AND subText = :conversationKey) OR
            (subText IS NULL AND title = :conversationKey)
        )
        """
    )
    suspend fun getAllNotificationIdsByConversation(
        packageName: String,
        conversationKey: String
    ): List<Long>

    /**
     * 특정 앱의 모든 알림을 읽음 처리.
     * 현재 코드에서는 호출되지 않음 (MarkAppAsReadUseCase는 존재하지만 ViewModel에서 미사용).
     */
    @Query("UPDATE notifications SET isRead = 1 WHERE packageName = :packageName")
    suspend fun markAppAsRead(packageName: String)

    /**
     * 특정 대화방의 모든 알림을 읽음 처리.
     * ConversationViewModel 초기화(init) 블록에서 호출 → 화면 진입 시 자동으로 읽음 처리.
     * getNotificationsByConversation()과 동일한 WHERE 조건 사용.
     */
    @Query(
        """
        UPDATE notifications
        SET isRead = 1
        WHERE packageName = :packageName
        AND (
            (subText IS NOT NULL AND subText = :conversationKey) OR
            (subText IS NULL AND title = :conversationKey)
        )
        """
    )
    suspend fun markConversationAsRead(packageName: String, conversationKey: String)

    /** 선택된 패키지명 목록에 해당하는 알림 전체 삭제 */
    @Query("DELETE FROM notifications WHERE packageName IN (:packageNames)")
    suspend fun deleteNotificationsByPackages(packageNames: List<String>)

    /**
     * 선택된 conversationKey 목록에 해당하는 알림 삭제.
     * conversationKey 계산 방식은 getConversationGroupsByApp()과 동일.
     */
    @Query(
        """
        DELETE FROM notifications
        WHERE packageName = :packageName
        AND (
            CASE
                WHEN subText IS NOT NULL AND subText != '' THEN subText
                ELSE title
            END
        ) IN (:conversationKeys)
        """
    )
    suspend fun deleteNotificationsByConversations(
        packageName: String,
        conversationKeys: List<String>
    )

    /** 선택된 알림 ID 목록에 해당하는 알림 삭제 */
    @Query("DELETE FROM notifications WHERE id IN (:ids)")
    suspend fun deleteNotificationsByIds(ids: List<Long>)

    /**
     * 알림 1건 저장. 동일 ID가 이미 존재하면 무시(IGNORE).
     * NotiKeepListenerService → SaveNotificationUseCase → Repository → 이 메서드 순으로 호출.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: NotificationEntity)

    /** 알림 1건 삭제 (Entity 객체로 직접 삭제) */
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
}
