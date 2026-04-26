package com.android.notikeep.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.notikeep.data.local.dao.NotificationDao
import com.android.notikeep.data.local.entity.NotificationEntity

/**
 * Room 데이터베이스 정의.
 * - entities: DB 테이블로 생성될 Entity 목록
 * - version: 스키마 변경 시 증가. fallbackToDestructiveMigration() 설정으로 버전 충돌 시 DB를 재생성함
 * - exportSchema: false → schema JSON 파일 생성 비활성화
 *
 * Hilt의 DatabaseModule에서 싱글톤으로 제공됨.
 */
@Database(
    entities = [NotificationEntity::class],
    version = 3,
    exportSchema = false
)
abstract class NotiKeepDatabase : RoomDatabase() {
    /** notifications 테이블 접근 DAO 반환 */
    abstract fun notificationDao(): NotificationDao
}
