package com.android.notikeep.presentation.ui.component

import android.util.Log
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.android.notikeep.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

private const val TAG = "BottomAdBanner"

/**
 * 화면 하단에 표시되는 AdMob 적응형 배너 광고 컴포넌트.
 * MainActivity의 Scaffold.bottomBar에 배치됨.
 *
 * - 광고 로드 성공 전까지는 공간을 차지하지 않음 (isAdLoaded = false)
 * - 화면 너비에 맞춰 적응형(Anchored Adaptive) 배너 사이즈 자동 계산
 * - navigationBarsPadding()으로 네비게이션바 영역 침범하지 않음
 */
@Composable
fun BottomAdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current

    /** 광고 로드 완료 여부. false → AndroidView 숨김, true → 광고 표시 */
    var isAdLoaded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()  // 네비게이션 바 높이만큼 패딩
    ) {
        /**
         * dp 단위 너비를 px로 변환 후 density로 나눠 실제 dp 정수 값으로 변환.
         * AdMob API가 dp 단위 정수를 요구함.
         * coerceAtLeast(1): 0이 되는 경우 방지
         */
        val adWidthDp = with(density) { maxWidth.roundToPx() / density.density }.toInt().coerceAtLeast(1)

        /**
         * adWidthDp가 바뀔 때만 AdSize 재계산.
         * getCurrentOrientationAnchoredAdaptiveBannerAdSize: 현재 화면 방향 기준으로 최적 배너 높이 반환
         */
        val adSize = remember(adWidthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
        }

        /**
         * adWidthDp가 바뀔 때만 AdView 재생성.
         * - adUnitId: BuildConfig에서 주입된 광고 단위 ID (디버그/릴리스 별도)
         * - adListener: 광고 로드 결과에 따라 isAdLoaded 상태 업데이트
         * - loadAd: 즉시 광고 요청 시작
         */
        val adView = remember(adWidthDp) {
            AdView(context).apply {
                adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
                setAdSize(adSize)
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "광고 로드 성공")
                        isAdLoaded = true
                    }

                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        Log.w(TAG, "광고 로드 실패: ${error.message}")
                        isAdLoaded = false
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }

        /**
         * 컴포저블이 Composition에서 제거될 때 AdView.destroy() 호출.
         * 메모리 누수 및 광고 SDK 리소스 정리.
         */
        DisposableEffect(adView) {
            onDispose { adView.destroy() }
        }

        // 광고 로드 완료 시에만 AndroidView로 AdView 표시
        if (isAdLoaded) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adSize.height.dp),
                factory = { adView }
            )
        }
    }
}

// ─────────────────────────────────────────
// Preview
// ─────────────────────────────────────────

/**
 * 하단 광고 배너 로드 완료 상태 프리뷰.
 * AdMob SDK는 Preview에서 동작하지 않으므로 실제 광고 대신 플레이스홀더로 표현.
 */
@Preview(showBackground = true, name = "광고 배너 - 로드 완료 (플레이스홀더)")
@Composable
private fun BottomAdBannerLoadedPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth().height(60.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    androidx.compose.material3.Text(
                        text = "광고 영역 (약 50~60dp)",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
