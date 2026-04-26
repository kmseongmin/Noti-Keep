package com.android.notikeep.presentation.ui.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

/**
 * 패키지명으로 앱 아이콘을 표시하는 공통 컴포넌트.
 * PackageManager에서 앱 아이콘을 가져와 Coil로 비동기 렌더링.
 *
 * @param packageName 아이콘을 가져올 앱의 패키지명
 * @param size 아이콘 크기. 기본값 40.dp
 */
@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val context = LocalContext.current

    // packageName이 바뀔 때만 아이콘 재조회. runCatching: 앱이 삭제됐거나 없는 경우 예외 처리
    val icon = remember(packageName) {
        runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
    }

    Image(
        // Coil의 비동기 이미지 로더. Drawable 객체도 model로 전달 가능
        painter = rememberAsyncImagePainter(model = icon),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))  // 앱 아이콘 모서리 둥글게
    )
}

// ─────────────────────────────────────────
// Preview
// ─────────────────────────────────────────

/**
 * 앱 아이콘 프리뷰.
 * 실제 기기/에뮬레이터에서는 설치된 앱 아이콘이 표시됨.
 * Preview에서는 패키지를 찾지 못해 빈 이미지가 표시될 수 있음.
 */
@Preview(showBackground = true, name = "앱 아이콘 - 기본 크기")
@Composable
private fun AppIconPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppIcon(packageName = "com.kakao.talk")
            AppIcon(packageName = "com.instagram.android")
            AppIcon(packageName = "com.slack")
        }
    }
}

@Preview(showBackground = true, name = "앱 아이콘 - 큰 크기")
@Composable
private fun AppIconLargePreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        AppIcon(packageName = "com.kakao.talk", size = 72.dp)
    }
}
