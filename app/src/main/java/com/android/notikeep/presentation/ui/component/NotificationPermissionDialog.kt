package com.android.notikeep.presentation.ui.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * 알림 접근 권한이 없을 때 표시하는 다이얼로그.
 * MainActivity.onResume()에서 권한 미허용 상태 감지 시 표시.
 *
 * @param onConfirm "설정으로 이동" 버튼 콜백. Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS 화면으로 이동.
 * @param onDismiss "취소" 버튼 또는 바깥 영역 클릭 콜백. 다이얼로그 숨김.
 */
@Composable
fun NotificationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림 접근 권한 필요") },
        text = { Text("저장된 알림을 확인하려면 알림 접근 권한이 필요합니다.\n설정에서 권한을 허용해 주세요.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("설정으로 이동") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

// ─────────────────────────────────────────
// Preview
// ─────────────────────────────────────────

/** 알림 권한 다이얼로그 프리뷰 */
@Preview(showBackground = true, name = "알림 권한 다이얼로그")
@Composable
private fun NotificationPermissionDialogPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        NotificationPermissionDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
