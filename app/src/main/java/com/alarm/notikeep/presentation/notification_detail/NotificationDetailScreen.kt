package com.alarm.notikeep.presentation.notification_detail

import android.graphics.BitmapFactory
import com.alarm.notikeep.domain.model.NotificationItem
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alarm.notikeep.domain.notification.NotificationClassifier
import com.alarm.notikeep.presentation.theme.BackgroundGray
import com.alarm.notikeep.presentation.theme.Gray500
import com.alarm.notikeep.presentation.theme.Gray700
import com.alarm.notikeep.presentation.theme.NotiKeepDimens
import com.alarm.notikeep.presentation.theme.SkyBlue
import com.alarm.notikeep.presentation.theme.SkyBlueLight
import com.alarm.notikeep.util.DateTimeUtil
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    packageName: String,
    threadKey: String,
    title: String?,
    onBack: () -> Unit,
    viewModel: NotificationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val detailItems = remember(uiState.notifications, threadKey) {
        uiState.notifications
            .filter { NotificationClassifier.threadKey(it) == threadKey }
            .sortedBy { it.timestamp }
    }
    val topBarTitle = remember(detailItems, packageName, title) {
        resolveTopBarTitle(
            detailItems = detailItems,
            fallbackPackageName = packageName,
            fallbackTitle = title
        )
    }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = topBarTitle)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (detailItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.isLoading) "알림을 불러오는 중입니다" else "표시할 알림이 없습니다",
                    style = MaterialTheme.typography.bodyLarge
                )
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
                verticalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space8)
            ) {
                itemsIndexed(detailItems) { index, notification ->
                    val previous = detailItems.getOrNull(index - 1)
                    if (previous == null || !isSameDay(previous.timestamp, notification.timestamp)) {
                        DateHeader(timestamp = notification.timestamp)
                    }
                    ChatMessageBubble(notification = notification)
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    notification: NotificationItem,
    modifier: Modifier = Modifier
) {
    val senderAndMessage = parseSenderAndMessage(notification.content)
    val senderName = senderAndMessage.sender ?: notification.title ?: notification.appName
    val isMessage = NotificationClassifier.isMessageNotification(notification)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Avatar(
            notification = notification,
            senderName = senderName,
            isMessage = isMessage
        )
        Spacer(modifier = Modifier.width(NotiKeepDimens.Space8))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(
                    start = NotiKeepDimens.Space8,
                    bottom = NotiKeepDimens.Space4
                )
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(NotiKeepDimens.Radius16),
                shadowElevation = NotiKeepDimens.Elevation4
            ) {
                Column {
                    val hasTextMessage = senderAndMessage.message.isNotBlank()
                    if (hasTextMessage) {
                        Text(
                            text = senderAndMessage.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray700,
                            modifier = Modifier.padding(
                                horizontal = NotiKeepDimens.Space12,
                                vertical = NotiKeepDimens.Space8
                            )
                        )
                    }

                    notification.attachmentData?.let { attachmentBytes ->
                        val attachmentBitmap = remember(attachmentBytes) {
                            BitmapFactory.decodeByteArray(attachmentBytes, 0, attachmentBytes.size)
                        }

                        if (notification.attachmentMimeType?.startsWith("image/") == true && attachmentBitmap != null) {
                            Image(
                                bitmap = attachmentBitmap.asImageBitmap(),
                                contentDescription = "Attachment",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(156.dp)
                                    .padding(
                                        start = NotiKeepDimens.Space12,
                                        end = NotiKeepDimens.Space12,
                                        top = if (hasTextMessage) NotiKeepDimens.Space4 else NotiKeepDimens.Space12,
                                        bottom = NotiKeepDimens.Space12
                                    )
                                    .clip(RoundedCornerShape(NotiKeepDimens.Radius16)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "첨부파일: ${notification.attachmentFileName ?: "unknown"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500,
                                modifier = Modifier.padding(
                                    start = NotiKeepDimens.Space12,
                                    end = NotiKeepDimens.Space12,
                                    bottom = NotiKeepDimens.Space12
                                )
                            )
                        }
                    }
                }
            }

            Text(
                text = DateTimeUtil.formatTimestamp(notification.timestamp, "HH:mm"),
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                modifier = Modifier.padding(
                    top = NotiKeepDimens.Space4,
                    start = NotiKeepDimens.Space8,
                    end = NotiKeepDimens.Space8
                )
            )
        }
    }
}

@Composable
private fun Avatar(
    notification: NotificationItem,
    senderName: String,
    isMessage: Boolean
) {
    val bitmap = remember(notification.iconData) {
        notification.iconData?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }
    val fallbackColors = listOf(
        Color(0xFFDCEBFF),
        Color(0xFFD7F3E6),
        Color(0xFFFFE8D6),
        Color(0xFFE7E2FF)
    )
    val bgColor = fallbackColors[senderName.hashCode().absoluteValue % fallbackColors.size]
    val initial = senderName.trim().firstOrNull()?.toString() ?: "?"

    Box(
        modifier = Modifier
            .size(NotiKeepDimens.Size36)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!isMessage && bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "App Icon",
                modifier = Modifier.size(NotiKeepDimens.Size36)
            )
            return
        }

        if (!isMessage) {
            Surface(
                color = SkyBlueLight,
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.size(NotiKeepDimens.Size36),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Default App Icon",
                        tint = SkyBlue,
                        modifier = Modifier.size(NotiKeepDimens.Size20)
                    )
                }
            }
            return
        }

        Surface(
            color = bgColor,
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.size(NotiKeepDimens.Size36),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.labelLarge,
                    color = Gray700,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DateHeader(timestamp: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = NotiKeepDimens.Space8),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Gray500.copy(alpha = 0.16f),
            shape = RoundedCornerShape(NotiKeepDimens.Radius16)
        ) {
            Text(
                text = DateTimeUtil.formatTimestamp(timestamp, "yyyy년 M월 d일 EEEE"),
                style = MaterialTheme.typography.labelSmall,
                color = Gray700,
                modifier = Modifier.padding(
                    horizontal = NotiKeepDimens.Space10,
                    vertical = NotiKeepDimens.Space4
                )
            )
        }
    }
}

private data class SenderAndMessage(
    val sender: String?,
    val message: String
)

private fun parseSenderAndMessage(content: String?): SenderAndMessage {
    val raw = content?.trim().orEmpty()
    if (raw.isEmpty()) return SenderAndMessage(sender = null, message = "")

    val colonIndex = raw.indexOf(':')
    if (colonIndex in 1..20 && colonIndex < raw.length - 1) {
        val sender = raw.substring(0, colonIndex).trim()
        val message = raw.substring(colonIndex + 1).trim()
        if (sender.isNotBlank() && message.isNotBlank()) {
            return SenderAndMessage(sender = sender, message = message)
        }
    }
    return SenderAndMessage(sender = null, message = raw)
}

private fun isSameDay(first: Long, second: Long): Boolean {
    return DateTimeUtil.formatTimestamp(first, "yyyyMMdd") ==
        DateTimeUtil.formatTimestamp(second, "yyyyMMdd")
}

private fun resolveTopBarTitle(
    detailItems: List<NotificationItem>,
    fallbackPackageName: String,
    fallbackTitle: String?
): String {
    val latest = detailItems.maxByOrNull { it.timestamp }
        ?: return fallbackTitle ?: fallbackPackageName

    if (!NotificationClassifier.isMessageNotification(latest)) {
        return latest.appName
    }

    val key = latest.conversationKey.orEmpty()
    return when {
        key.startsWith("group:") -> key.removePrefix("group:").ifBlank { latest.title ?: latest.appName }
        key.startsWith("dm:") -> key.removePrefix("dm:").ifBlank { latest.title ?: latest.appName }
        key.startsWith("sms:") -> key.removePrefix("sms:").ifBlank { latest.title ?: latest.appName }
        else -> latest.title ?: latest.appName
    }
}
