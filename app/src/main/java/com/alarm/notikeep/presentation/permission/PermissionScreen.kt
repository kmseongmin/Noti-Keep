package com.alarm.notikeep.presentation.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.alarm.notikeep.presentation.theme.BackgroundGray
import com.alarm.notikeep.presentation.theme.Gray500
import com.alarm.notikeep.presentation.theme.Gray700
import com.alarm.notikeep.presentation.theme.NotiKeepDimens
import com.alarm.notikeep.presentation.theme.SkyBlue
import com.alarm.notikeep.presentation.theme.SkyBlueDark
import com.alarm.notikeep.presentation.theme.SkyBlueLight
import com.alarm.notikeep.presentation.theme.SkyBlueMedium
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.sp

@Composable
fun PermissionScreen(
    onRequestPermission: () -> Unit,
    onAppExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SkyBlueMedium.copy(alpha = 0.3f),
                        BackgroundGray,
                        Color.White
                    ),
                    startY = 0f,
                    endY = 2000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NotiKeepDimens.Space32),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space24)
        ) {
            Box(
                modifier = Modifier
                    .size(NotiKeepDimens.Size140)
                    .shadow(
                        elevation = NotiKeepDimens.Elevation12,
                        shape = CircleShape,
                        spotColor = SkyBlue.copy(alpha = 0.4f)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SkyBlueLight,
                                SkyBlueMedium.copy(alpha = 0.8f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification",
                    modifier = Modifier.size(NotiKeepDimens.Size70),
                    tint = SkyBlue
                )
            }

            Text(
                text = "알림 접근 권한이 필요합니다",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Gray700,
                letterSpacing = 0.5.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = NotiKeepDimens.Elevation8,
                        shape = RoundedCornerShape(NotiKeepDimens.Radius24),
                        spotColor = SkyBlue.copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = NotiKeepDimens.None),
                shape = RoundedCornerShape(NotiKeepDimens.Radius24)
            ) {
                Column(
                    modifier = Modifier.padding(NotiKeepDimens.Space28),
                    verticalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space18)
                ) {
                    Text(
                        text = "NotiKeep이 알림을 저장하려면",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SkyBlueDark,
                        letterSpacing = 0.3.sp
                    )

                    PermissionStep(
                        number = "1",
                        text = "아래 버튼을 눌러 설정으로 이동"
                    )
                    PermissionStep(
                        number = "2",
                        text = "NotiKeep을 활성화"
                    )
                    PermissionStep(
                        number = "3",
                        text = "뒤로가기로 자동 복귀"
                    )
                }
            }

            Spacer(modifier = Modifier.height(NotiKeepDimens.Space8))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NotiKeepDimens.ButtonHeight)
                    .shadow(
                        elevation = NotiKeepDimens.Elevation8,
                        shape = RoundedCornerShape(NotiKeepDimens.Radius16),
                        spotColor = SkyBlue.copy(alpha = 0.5f)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue
                ),
                shape = RoundedCornerShape(NotiKeepDimens.Radius16)
            ) {
                Text(
                    text = "설정으로 이동",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 0.5.sp
                )
            }

            OutlinedButton(
                onClick = onAppExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NotiKeepDimens.ButtonHeight),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Gray500
                ),
                shape = RoundedCornerShape(NotiKeepDimens.Radius16)
            ) {
                Text(
                    text = "나중에 하기",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionStep(
    number: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space12),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(NotiKeepDimens.Size32)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SkyBlue,
                            SkyBlueDark
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space2)
        ) {
            Text(
                text = "STEP $number",
                style = MaterialTheme.typography.labelSmall,
                color = SkyBlueDark,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Gray700,
                lineHeight = 22.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
    MaterialTheme {
        PermissionScreen(
            onRequestPermission = {},
            onAppExit = {}
        )
    }
}
