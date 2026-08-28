package com.aos.fanpulse.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aos.fanpulse.domain.model.NotificationModel

import com.aos.fanpulse.presentation.common.CommonTopAppBar
enum class NotificationTab {
    ALL, UNREAD
}
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(NotificationTab.ALL) }
    val notifications by viewModel.notificationList.collectAsState()
    val filteredNotifications = when (selectedTab) {
        NotificationTab.ALL -> notifications
        NotificationTab.UNREAD -> notifications.filter { !it.isRead }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {

        CommonTopAppBar(
            isActiveLeftBack = true,
            onLeftBack = { onBackClick() },
            isActiveLeftTextTitle = true,
            leftTextTitle = "알림",
        )

        // Tab Row
        NotificationTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // Section Header
        if (filteredNotifications.isNotEmpty()) {
            Text(
                text = "모두 읽음 처리",
                fontSize = 13.sp,
                color = Color(0xFF9C27B0),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { viewModel.markAllAsRead() }
            )
        }

        // Notifications List
        if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "알림이 없습니다.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(
                    items = filteredNotifications,
                    key = { it.id }
                ) { notification ->
                    NotificationItem(
                        notification = notification,
                        onItemClick = { viewModel.markAsRead(notification.id) }
                    )
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
fun NotificationItem(
    notification: NotificationModel,
    onItemClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
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
//                when (val icon = notification.icon) {
//                    is NotificationIcon.Emoji -> {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .clip(CircleShape)
//                                .background(Color(0xFFF5F5F5)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = icon.emoji,
//                                fontSize = 24.sp
//                            )
//                        }
//                    }
//                    is NotificationIcon.Icon -> {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .clip(CircleShape)
//                                .background(icon.backgroundColor),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                imageVector = icon.icon,
//                                contentDescription = null,
//                                tint = Color.White,
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
//                    }
//                }

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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = buildString {
                            append(notification.title)
                            append(" ")
                            append(notification.body)
                        },
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

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

                Text(
                    text = notification.receivedAt.toString(),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }

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
    NotificationsScreen({})
}