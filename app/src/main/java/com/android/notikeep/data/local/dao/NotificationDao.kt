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

    // subText가 있으면 subText(방 이름)로, 없으면 title(보낸 사람)로 조회
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

    @Query("SELECT appName FROM notifications WHERE packageName = :packageName ORDER BY receivedAt DESC, id DESC LIMIT 1")
    fun getLatestAppName(packageName: String): Flow<String?>

    @Query(
        """
        SELECT DISTINCT packageName
        FROM notifications
        WHERE (:category IS NULL OR category = :category)
        """
    )
    suspend fun getAllPackageNames(category: String?): List<String>

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

    @Query("UPDATE notifications SET isRead = 1 WHERE packageName = :packageName")
    suspend fun markAppAsRead(packageName: String)

    @Query("DELETE FROM notifications WHERE packageName IN (:packageNames)")
    suspend fun deleteNotificationsByPackages(packageNames: List<String>)

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

    @Query("DELETE FROM notifications WHERE id IN (:ids)")
    suspend fun deleteNotificationsByIds(ids: List<Long>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
}
