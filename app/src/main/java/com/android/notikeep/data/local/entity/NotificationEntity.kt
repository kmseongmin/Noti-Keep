package com.android.notikeep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.notikeep.domain.model.AppNotification

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val receivedAt: Long,
    val category: String?
) {
    fun toDomain() = AppNotification(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        content = content,
        receivedAt = receivedAt,
        category = category
    )

    companion object {
        fun fromDomain(n: AppNotification) = NotificationEntity(
            id = n.id,
            packageName = n.packageName,
            appName = n.appName,
            title = n.title,
            content = n.content,
            receivedAt = n.receivedAt,
            category = n.category
        )
    }
}
