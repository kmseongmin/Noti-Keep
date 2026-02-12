package com.alarm.notikeep.data.local.database

import android.content.Context
import android.util.Log
import com.alarm.notikeep.R
import com.alarm.notikeep.data.local.dao.NotificationDao
import com.alarm.notikeep.data.local.entity.NotificationEntity
import com.alarm.notikeep.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSampleDataInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun initializeIfNeeded() = withContext(ioDispatcher) {
        val existingCount = notificationDao.getNotificationCount()
        if (existingCount > 0) return@withContext

        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60L * 60L * 1000L
        val yesterday = now - oneDayMillis
        val sampleImageBytes = loadSampleImageBytes()

        val samples = listOf(
            NotificationEntity(
                packageName = "com.notikeep.sample.general",
                appName = "샘플데이터",
                title = "일반알람 샘플",
                content = null,
                timestamp = now - 120_000L,
                attachmentData = sampleImageBytes,
                attachmentMimeType = "image/png",
                attachmentFileName = "sample_general_image.png"
            ),
            NotificationEntity(
                packageName = "com.notikeep.sample.sms",
                appName = "샘플데이터",
                title = "SMS 샘플",
                content = "길동: 어제 보낸 메시지",
                category = "msg",
                conversationKey = "sms:민수",
                timestamp = yesterday - 10L * 60L * 1000L
            ),
            NotificationEntity(
                packageName = "com.notikeep.sample.sms",
                appName = "샘플데이터",
                title = "SMS 샘플",
                content = "길동: 오늘 보낸 메시지",
                category = "msg",
                conversationKey = "sms:민수",
                timestamp = now - 60_000L
            )
        )

        notificationDao.insertAll(samples)
        Log.d(TAG, "inserted sample notifications: count=${samples.size}")
    }

    companion object {
        private const val TAG = "NotiKeepSampleInit"
    }

    private fun loadSampleImageBytes(): ByteArray? {
        return runCatching {
            context.resources.openRawResource(R.drawable.sampleimg).use { input ->
                input.readBytes()
            }
        }.onFailure { error ->
            Log.w(TAG, "failed to load sampleimg resource", error)
        }.getOrNull()
    }
}
