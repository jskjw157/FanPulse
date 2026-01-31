package com.aos.fanpulse.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data Classes
data class Notification(
    val id: String,
    val username: String,
    val message: String,
    val timeAgo: String,
    val icon: NotificationIcon,
    val isRead: Boolean = false
)

sealed class NotificationIcon {
    data class Emoji(val emoji: String) : NotificationIcon()
    data class Icon(val icon: ImageVector, val backgroundColor: Color) : NotificationIcon()
}

enum class NotificationTab {
    ALL, UNREAD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(NotificationTab.ALL) }

    val notifications = listOf(
        Notification(
            id = "1",
            username = "ARMY_Forever",
            message = "님이 회원님의 게시글을 좋아합니다.",
            timeAgo = "5분 전",
            icon = NotificationIcon.Emoji("❤️"),
            isRead = false
        ),
        Notification(
            id = "2",
            username = "Blink_Girl",
            message = "님이 회원님의 게시글에 댓글을 남겼습니다: \"정말 멋진 글이네요!\"",
            timeAgo = "1시간 전",
            icon = NotificationIcon.Emoji("💜"),
            isRead = false
        ),
        Notification(
            id = "3",
            username = "FanPulse",
            message = "BTS 투표가 곧 마감됩니다! 지금 참여하세요.",
            timeAgo = "2시간 전",
            icon = NotificationIcon.Emoji("🏆"),
            isRead = true
        ),
        Notification(
            id = "4",
            username = "FanPulse",
            message = "새로운 이벤트가 시작되었습니다! 포인트 2배 적립 찬스!",
            timeAgo = "3시간 전",
            icon = NotificationIcon.Emoji("🎁"),
            isRead = true
        ),
        Notification(
            id = "5",
            username = "Kpop_Lover",
            message = "님이 회원님을 팔로우하기 시작했습니다.",
            timeAgo = "5시간 전",
            icon = NotificationIcon.Emoji("👤"),
            isRead = true
        ),
        Notification(
            id = "6",
            username = "FanPulse",
            message = "BLACKPINK 콘서트 티켓 예매가 시작되었습니다!",
            timeAgo = "1일 전",
            icon = NotificationIcon.Emoji("🎤"),
            isRead = true
        ),
        Notification(
            id = "7",
            username = "FanPulse",
            message = "BTS 새 앨범 발매 소식이 업데이트되었습니다.",
            timeAgo = "2일 전",
            icon = NotificationIcon.Emoji("📦"),
            isRead = true
        )
    )

    val filteredNotifications = when (selectedTab) {
        NotificationTab.ALL -> notifications
        NotificationTab.UNREAD -> notifications.filter { !it.isRead }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "알림",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            // Tab Row
            NotificationTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // Section Header
            if (filteredNotifications.isNotEmpty()) {
                Text(
                    text = "모두 읽을 처리",
                    fontSize = 13.sp,
                    color = Color(0xFF9C27B0),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Notifications List
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredNotifications) { notification ->
                    NotificationItem(notification)
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationTabRow(
    selectedTab: NotificationTab,
    onTabSelected: (NotificationTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationTabButton(
            text = "전체",
            isSelected = selectedTab == NotificationTab.ALL,
            onClick = { onTabSelected(NotificationTab.ALL) },
            modifier = Modifier.weight(1f)
        )
        NotificationTabButton(
            text = "읽지 않음",
            isSelected = selectedTab == NotificationTab.UNREAD,
            onClick = { onTabSelected(NotificationTab.UNREAD) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NotificationTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF9C27B0) else Color(0xFFF0F0F0),
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (notification.isRead) Color.White else Color(0xFFFFF8FF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon/Avatar
            Box(
                modifier = Modifier.size(48.dp)
            ) {
                when (val icon = notification.icon) {
                    is NotificationIcon.Emoji -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = icon.emoji,
                                fontSize = 24.sp
                            )
                        }
                    }
                    is NotificationIcon.Icon -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(icon.backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Online/Status indicator (small colored dot)
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9C27B0))
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Username and message in a single text with formatting
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = buildString {
                            append(notification.username)
                            append(notification.message)
                        },
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    // Unread indicator dot
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9C27B0))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time ago
                Text(
                    text = notification.timeAgo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }

    // Divider
    HorizontalDivider(
        modifier = Modifier.padding(start = 76.dp),
        thickness = 0.5.dp,
        color = Color(0xFFE0E0E0)
    )
}

// Alternative NotificationItem with username in bold
@Composable
fun NotificationItemStyled(notification: Notification) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (notification.isRead) Color.White else Color(0xFFFFF8FF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon/Avatar
            Box(
                modifier = Modifier.size(48.dp)
            ) {
                when (val icon = notification.icon) {
                    is NotificationIcon.Emoji -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = icon.emoji,
                                fontSize = 24.sp
                            )
                        }
                    }
                    is NotificationIcon.Icon -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(icon.backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Online/Status indicator
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9C27B0))
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = notification.username,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = notification.message,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color.Black
                        )
                    }

                    // Unread indicator dot
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9C27B0))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time ago
                Text(
                    text = notification.timeAgo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }

    // Divider
    HorizontalDivider(
        modifier = Modifier.padding(start = 76.dp),
        thickness = 0.5.dp,
        color = Color(0xFFE0E0E0)
    )
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen()
}