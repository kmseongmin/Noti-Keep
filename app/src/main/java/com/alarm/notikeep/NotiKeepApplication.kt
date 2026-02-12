package com.alarm.notikeep

import android.app.Application
import com.alarm.notikeep.data.local.database.NotificationSampleDataInitializer
import com.alarm.notikeep.di.IoDispatcher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NotiKeepApplication : Application() {

    @Inject
    lateinit var sampleDataInitializer: NotificationSampleDataInitializer

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + ioDispatcher).launch {
            sampleDataInitializer.initializeIfNeeded()
        }
    }
}
