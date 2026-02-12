package com.alarm.notikeep.presentation.notification_list

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alarm.notikeep.domain.model.NotificationItem
import com.alarm.notikeep.presentation.theme.AccentBlue
import com.alarm.notikeep.presentation.theme.BackgroundGray
import com.alarm.notikeep.presentation.theme.Gray300
import com.alarm.notikeep.presentation.theme.Gray500
import com.alarm.notikeep.presentation.theme.Gray700
import com.alarm.notikeep.presentation.theme.NotiKeepDimens
import com.alarm.notikeep.presentation.theme.SkyBlue
import com.alarm.notikeep.presentation.theme.SkyBlueDark
import com.alarm.notikeep.presentation.theme.SkyBlueLight
import com.alarm.notikeep.presentation.theme.SkyBlueMedium
import com.alarm.notikeep.util.DateTimeUtil

@Composable
fun NotificationListScreen(
    onOpenGroupDetail: (NotificationGroup) -> Unit,
    viewModel: NotificationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val groups = remember(uiState.notifications) {
        uiState.notifications.toNotificationGroups()
    }

    NotificationListContent(
        groups = groups,
        isLoading = uiState.isLoading,
        onOpenGroupDetail = { group ->
            viewModel.markThreadAsRead(group.threadKey)
            onOpenGroupDetail(group)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListContent(
    groups: List<NotificationGroup>,
    isLoading: Boolean,
    onOpenGroupDetail: (NotificationGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space12)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(NotiKeepDimens.Size36)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "NotiKeep",
                                tint = Color.White,
                                modifier = Modifier.size(NotiKeepDimens.Size20)
                            )
                        }
                        Text(
                            text = "NotiKeep",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SkyBlue
                )
            )
        },
        containerColor = BackgroundGray,
        modifier = modifier
    ) { paddingValues ->
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space20)
                ) {
                    Box(
                        modifier = Modifier
                            .size(NotiKeepDimens.Size100)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        SkyBlueLight,
                                        SkyBlueMedium
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "No notifications",
                            modifier = Modifier.size(NotiKeepDimens.Size48),
                            tint = SkyBlue
                        )
                    }
                    Text(
                        text = if (isLoading) "알림을 불러오는 중입니다" else "저장된 알림이 없습니다",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray700,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = "알림이 오면 자동으로 저장됩니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(
                        horizontal = NotiKeepDimens.Space20,
                        vertical = NotiKeepDimens.Space16
                    ),
                verticalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space16)
            ) {
                items(groups) { group ->
                    NotificationGroupCard(
                        group = group,
                        isRead = !group.hasUnread,
                        onClick = { onOpenGroupDetail(group) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationGroupCard(
    group: NotificationGroup,
    isRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotificationItemCard(
        notification = NotificationItem(
            id = 0,
            packageName = group.packageName,
            appName = group.appName,
            title = group.title,
            content = group.content,
            isRead = !group.hasUnread,
            timestamp = group.latestTimestamp,
            iconData = group.iconData
        ),
        isRead = isRead,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun NotificationItemCard(
    notification: NotificationItem,
    isRead: Boolean = false,
    showFullContent: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = NotiKeepDimens.Elevation4,
                shape = RoundedCornerShape(NotiKeepDimens.Radius16),
                spotColor = SkyBlue.copy(alpha = 0.15f)
            )
            .let {
                if (onClick != null) {
                    it.clickable(onClick = onClick)
                } else {
                    it
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = NotiKeepDimens.None),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(NotiKeepDimens.Radius16)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White,
                            SkyBlueLight.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = NotiKeepDimens.StrokeThin,
                    color = Gray300.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(NotiKeepDimens.Radius16)
                )
                .padding(NotiKeepDimens.Space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(NotiKeepDimens.Size48)
                    .shadow(
                        elevation = NotiKeepDimens.Elevation4,
                        shape = CircleShape,
                        spotColor = SkyBlue.copy(alpha = 0.3f)
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SkyBlueLight,
                                SkyBlueMedium
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                notification.iconData?.let { iconBytes ->
                    val bitmap = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "App Icon",
                        modifier = Modifier.size(NotiKeepDimens.Size28)
                    )
                } ?: Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Default Icon",
                    modifier = Modifier.size(NotiKeepDimens.Size28),
                    tint = SkyBlue
                )
            }

            Spacer(modifier = Modifier.width(NotiKeepDimens.Space12))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.appName,
                    style = MaterialTheme.typography.labelLarge,
                    color = SkyBlueDark,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )

                if (showFullContent) {
                    Text(
                        text = notification.title ?: "(제목 없음)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Gray700,
                    )
                    notification.content?.takeIf { it.isNotBlank() }?.let { content ->
                        Spacer(modifier = Modifier.height(NotiKeepDimens.Space4))
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }

                    notification.attachmentData?.let { bytes ->
                        Spacer(modifier = Modifier.height(NotiKeepDimens.Space6))
                        if (notification.attachmentMimeType?.startsWith("image/") == true) {
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Attachment Preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(NotiKeepDimens.Radius16)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Text(
                                text = "첨부파일 저장됨: ${notification.attachmentFileName ?: "unknown"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                    }
                } else {
                    val titleText = notification.title ?: "(제목 없음)"
                    val contentText = notification.content?.takeIf { it.isNotBlank() }
                        ?: if (notification.attachmentMimeType?.startsWith("image/") == true) "사진" else null
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Gray700,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(titleText)
                            }
                            if (contentText != null) {
                                append(" ")
                                withStyle(SpanStyle(color = Gray500)) {
                                    append(contentText)
                                }
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(NotiKeepDimens.Space6))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space6)
                ) {
                    Text(
                        text = DateTimeUtil.formatTimestamp(notification.timestamp),
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentBlue,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    )
                    if (!isRead) {
                        Box(
                            modifier = Modifier
                                .size(NotiKeepDimens.Size6)
                                .background(
                                    color = AccentBlue,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationListContentPreview() {
    MaterialTheme {
        NotificationListContent(
            groups = listOf(
                NotificationGroup(
                    threadKey = "com.example.app::test notification",
                    packageName = "com.example.app",
                    appName = "Example App",
                    title = "Test Notification",
                    content = "This is a test notification content",
                    latestTimestamp = System.currentTimeMillis(),
                    iconData = null,
                    totalCount = 3,
                    hasUnread = true
                ),
                NotificationGroup(
                    threadKey = "com.example.email::important email",
                    packageName = "com.example.email",
                    appName = "Email",
                    title = "Important Email",
                    content = "You have a new email from your manager regarding the project deadline",
                    latestTimestamp = System.currentTimeMillis() - 7200000,
                    iconData = null,
                    totalCount = 1,
                    hasUnread = false
                )
            ),
            isLoading = false,
            onOpenGroupDetail = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationListContentEmptyPreview() {
    MaterialTheme {
        NotificationListContent(
            groups = emptyList(),
            isLoading = false,
            onOpenGroupDetail = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationItemCardPreview() {
    MaterialTheme {
        NotificationItemCard(
            notification = NotificationItem(
                id = 1,
                packageName = "com.example.app",
                appName = "Example App",
                title = "Test Notification",
                content = "This is a test notification content with a longer text to see how it looks when it wraps to multiple lines",
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
