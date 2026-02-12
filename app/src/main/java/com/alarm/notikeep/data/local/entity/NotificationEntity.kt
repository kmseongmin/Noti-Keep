package com.alarm.notikeep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String?,
    val content: String?,
    @ColumnInfo(defaultValue = "NULL")
    val category: String? = null,
    @ColumnInfo(defaultValue = "NULL")
    val conversationKey: String? = null,
    val timestamp: Long,
    @ColumnInfo(defaultValue = "0")
    val isRead: Boolean = false,
    val attachmentData: ByteArray? = null,
    @ColumnInfo(defaultValue = "NULL")
    val attachmentMimeType: String? = null,
    @ColumnInfo(defaultValue = "NULL")
    val attachmentFileName: String? = null,
    val iconData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationEntity

        if (id != other.id) return false
        if (packageName != other.packageName) return false
        if (appName != other.appName) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (category != other.category) return false
        if (conversationKey != other.conversationKey) return false
        if (timestamp != other.timestamp) return false
        if (isRead != other.isRead) return false
        if (attachmentMimeType != other.attachmentMimeType) return false
        if (attachmentFileName != other.attachmentFileName) return false
        if (attachmentData != null) {
            if (other.attachmentData == null) return false
            if (!attachmentData.contentEquals(other.attachmentData)) return false
        } else if (other.attachmentData != null) return false
        if (iconData != null) {
            if (other.iconData == null) return false
            if (!iconData.contentEquals(other.iconData)) return false
        } else if (other.iconData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + appName.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        result = 31 * result + (conversationKey?.hashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + isRead.hashCode()
        result = 31 * result + (attachmentData?.contentHashCode() ?: 0)
        result = 31 * result + (attachmentMimeType?.hashCode() ?: 0)
        result = 31 * result + (attachmentFileName?.hashCode() ?: 0)
        result = 31 * result + (iconData?.contentHashCode() ?: 0)
        return result
    }
}
