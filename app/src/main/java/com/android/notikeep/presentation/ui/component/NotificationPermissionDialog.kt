package com.android.notikeep.presentation.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

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
