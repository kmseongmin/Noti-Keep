package com.alarm.notikeep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String?,
    val content: String?,
    val timestamp: Long,
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
        if (timestamp != other.timestamp) return false
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
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (iconData?.contentHashCode() ?: 0)
        return result
    }
}
