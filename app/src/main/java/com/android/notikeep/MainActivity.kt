package com.android.notikeep

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import com.android.notikeep.presentation.navigation.NavGraph
import com.android.notikeep.presentation.ui.component.BottomAdBanner
import com.android.notikeep.presentation.ui.component.NotificationPermissionDialog
import com.android.notikeep.presentation.ui.theme.NotiKeepTheme
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱 진입점 Activity.
 * 알림 접근 권한 확인 → NavGraph 표시 → 하단 광고 배너 배치를 담당.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * 알림 접근 권한 다이얼로그 표시 여부.
     * Compose 상태로 관리 → true 시 NotificationPermissionDialog 표시.
     * by mutableStateOf: onResume 등에서 값 변경 시 Compose가 자동 재구성(recomposition).
     */
    private var showPermissionDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        // 상태바/네비게이션바 뒤까지 콘텐츠를 확장 (Edge-to-Edge)
        enableEdgeToEdge()

        // AdMob SDK 초기화. 광고 로드 전에 반드시 호출 필요
        MobileAds.initialize(this)

        setContent {
            NotiKeepTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        // 시스템 인셋을 Scaffold가 자동 처리하지 않도록 0으로 설정
                        // (NavGraph 각 화면에서 개별 처리)
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        // 하단에 AdMob 배너 광고 표시
                        bottomBar = { BottomAdBanner() }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                // bottomBar(광고 배너) 높이만큼 콘텐츠 패딩 적용
                                .padding(innerPadding)
                        ) {
                            // 앱 전체 네비게이션 그래프 (Home → AppDetail → Conversation)
                            NavGraph()

                            // 알림 접근 권한이 없을 때 표시하는 다이얼로그
                            if (showPermissionDialog) {
                                NotificationPermissionDialog(
                                    onConfirm = {
                                        showPermissionDialog = false
                                        // 설정 → 알림 접근 권한 화면으로 이동
                                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                    },
                                    onDismiss = { showPermissionDialog = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 앱이 포그라운드로 돌아올 때마다 권한 상태 재확인.
     * 사용자가 설정에서 권한을 허용/거부하고 돌아오는 경우를 처리하기 위함.
     */
    override fun onResume() {
        super.onResume()
        val hasPermission = isNotificationListenerEnabled()
        Log.d(TAG, "onResume() - 알림 접근 권한: $hasPermission")
        // 권한이 없으면 다이얼로그 표시, 있으면 숨김
        showPermissionDialog = !hasPermission
    }

    /**
     * 이 앱이 알림 리스너(NotificationListenerService)로 등록되어 있는지 확인.
     * NotificationManagerCompat.getEnabledListenerPackages(): 시스템에 등록된 리스너 패키지 목록 반환.
     *
     * @return 등록되어 있으면 true, 아니면 false
     */
    private fun isNotificationListenerEnabled(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

    companion object {
        private const val TAG = "MainActivity"
    }
}
