package com.alarm.notikeep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.alarm.notikeep.presentation.navigation.NotiKeepNavHost
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
                    NotiKeepNavHost(
                        onAppExit = { finish() }
                    )
                }
            }
        }
    }
}
