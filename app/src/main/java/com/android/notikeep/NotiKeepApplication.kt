package com.android.notikeep

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * 앱 Application 클래스.
 * @HiltAndroidApp: Hilt 의존성 주입 컴포넌트 트리의 루트를 생성.
 * 이 어노테이션이 없으면 Hilt DI가 동작하지 않음.
 */
@HiltAndroidApp
class NotiKeepApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("NotiKeepApp", "Application 시작")
    }
}
