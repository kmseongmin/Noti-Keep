package com.android.notikeep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.notikeep.presentation.ui.theme.NotiKeepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotiKeepTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // TODO: 네비게이션 진입점
                }
            }
        }
    }
}
