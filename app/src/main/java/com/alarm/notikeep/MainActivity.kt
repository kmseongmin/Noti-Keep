package com.alarm.notikeep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.alarm.notikeep.presentation.notification_list.NotificationListScreen
import com.alarm.notikeep.presentation.theme.NotiKeepTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotiKeepTheme {
                Surface {
                    NotificationListScreen(
                        onAppExit = { finish() }
                    )
                }
            }
        }
    }
}
