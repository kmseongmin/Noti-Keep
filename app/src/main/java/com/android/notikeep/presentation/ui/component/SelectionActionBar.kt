package com.android.notikeep.presentation.ui.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 선택 ���드에서 목록 상단에 표시되는 액션 바.
 * - 선택 개수 표시
 * - 모두 선택 / 전체 해제 토글 버튼
 * - 삭제 버튼
 * - 선택 해제(모드 종료) 버튼
 *
 * Home / AppDetail / Conversation 세 화면에서 공통으로 재사용.
 *
 * @param selectedCount 현재 선택된 항목 수
 * @param isAllSelected 전체 선택 여부 → 버튼 라벨 "전체해제" / "모두선택" 전환
 * @param onToggleSelectAll 전체 선택 / 전체 해제 버튼 콜백
 * @param onDeleteSelected 삭제 버튼 콜백
 * @param onClearSelection 해제 버튼 콜백 (선택 모드 종료)
 */
@Composable
fun SelectionActionBar(
    selectedCount: Int,
    isAllSelected: Boolean,
    onToggleSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 선택된 항목 수 표시 (예: "3개 선택")
            Text(
                text = "${selectedCount}개 선택",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            // 전체 선택 상태이면 "전체해제", 아니면 "모두선택"
            TextButton(onClick = onToggleSelectAll) {
                Text(if (isAllSelected) "전체해제" else "모두선택")
            }
            FilledTonalButton(onClick = onDeleteSelected) {
                Text("삭제")
            }
            TextButton(onClick = onClearSelection) {
                Text("해제")
            }
        }
    }
}

// ─────────────────────────────────────────
// Preview
// ─────────────────────────────────────────

/** 선택 액션바 프리뷰 - 일부 선택 */
@Preview(showBackground = true, name = "선택 액션바 - 일부 선택")
@Composable
private fun SelectionActionBarPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        SelectionActionBar(
            selectedCount = 3,
            isAllSelected = false,
            onToggleSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** 선택 액션바 프리뷰 - 전체 선택 */
@Preview(showBackground = true, name = "선택 액션바 - 전체 선택")
@Composable
private fun SelectionActionBarAllSelectedPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        SelectionActionBar(
            selectedCount = 10,
            isAllSelected = true,
            onToggleSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** 선택 액션바 프리뷰 - 0개 선택 */
@Preview(showBackground = true, name = "선택 액션바 - 0개")
@Composable
private fun SelectionActionBarZeroPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        SelectionActionBar(
            selectedCount = 0,
            isAllSelected = false,
            onToggleSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}
