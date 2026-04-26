package com.android.notikeep.di

import com.android.notikeep.data.repository.NotificationRepositoryImpl
import com.android.notikeep.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 인터페이스와 구현체를 바인딩하는 Hilt 모듈.
 * @Binds: 인터페이스(NotificationRepository)를 구현체(NotificationRepositoryImpl)에 연결.
 * → UseCase에서 NotificationRepository를 주입받으면 실제로는 NotificationRepositoryImpl이 주입됨.
 *
 * object 대신 abstract class 사용: @Binds는 abstract fun이어야 하기 때문.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * NotificationRepository 인터페이스 → NotificationRepositoryImpl 바인딩.
     * @Singleton: 앱 전체에서 동일한 인스턴스 사용.
     */
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
