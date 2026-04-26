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

/**
 * Room DB 관련 의존성을 제공하는 Hilt 모듈.
 * @InstallIn(SingletonComponent::class): 앱 생명주기 동안 싱글톤으로 유지.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * NotiKeepDatabase 싱글톤 인스턴스 제공.
     *
     * - 파일명: "notikeep.db" (앱 데이터 디렉토리에 저장됨)
     * - fallbackToDestructiveMigration(): DB 버전이 올라갈 때 마이그레이션 정책이 없으면
     *   기존 DB를 삭제하고 새로 생성 (개발 중 스키마 변경이 잦을 때 편리)
     *
     * @param context Application Context (@ApplicationContext 한정자로 주입)
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotiKeepDatabase =
        Room.databaseBuilder(context, NotiKeepDatabase::class.java, "notikeep.db")
            .fallbackToDestructiveMigration()
            .build()

    /**
     * NotificationDao 인스턴스 제공.
     * DB 인스턴스에서 DAO를 추출해 반환.
     * @Singleton 없음 → DB가 싱글톤이므로 DAO도 사실상 동일 인스턴스 재사용.
     *
     * @param db 위의 provideDatabase()가 제공하는 DB 인스턴스
     */
    @Provides
    fun provideNotificationDao(db: NotiKeepDatabase): NotificationDao = db.notificationDao()
}
