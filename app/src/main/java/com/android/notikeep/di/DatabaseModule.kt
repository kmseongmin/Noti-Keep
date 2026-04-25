package com.android.notikeep.di

import android.content.Context
import androidx.room.Room
import com.android.notikeep.data.local.dao.NotificationDao
import com.android.notikeep.data.local.database.NotiKeepDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotiKeepDatabase =
        Room.databaseBuilder(context, NotiKeepDatabase::class.java, "notikeep.db").build()

    @Provides
    fun provideNotificationDao(db: NotiKeepDatabase): NotificationDao = db.notificationDao()
}
