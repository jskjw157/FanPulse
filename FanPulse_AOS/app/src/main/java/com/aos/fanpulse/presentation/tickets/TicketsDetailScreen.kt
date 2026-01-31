package com.aos.fanpulse.presentation.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Data Classes for Tickets Detail
data class TicketsDetail(
    val id: String,
    val title: String,
    val artist: String,
    val venue: String,
    val date: String,
    val seatInfo: String,
    val status: TicketsStatus,
    val TicketsNumber: String,
    val TicketsDate: String,
    val price: Int,
    val imageGradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsDetailDialog(
    Tickets: TicketsDetail,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "예매 상세",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // Concert Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = Tickets.imageGradient
                                )
                            )
                    ) {
                        // Concert image placeholder with crowd silhouette
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Tickets.artist,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title and Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Tickets.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Tickets.artist,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Surface(
                            color = Tickets.status.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = Tickets.status.displayName,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = Tickets.status.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Venue Info
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        text = Tickets.venue,
                        iconTint = Color(0xFF9C27B0)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Info
                    InfoRow(
                        icon = Icons.Default.Person,
                        text = Tickets.date,
                        iconTint = Color(0xFF9C27B0)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Seat Info
                    InfoRow(
                        icon = Icons.Default.Person,
                        text = Tickets.seatInfo,
                        iconTint = Color(0xFF9C27B0)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // QR Code Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "입장 QR 코드",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // QR Code Placeholder
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // QR Code pattern placeholder
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "QR Code",
                                        modifier = Modifier.size(150.dp),
                                        tint = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "공연 입장 전 시 제시해주세요",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tickets Information
                    Text(
                        text = "예매 정보",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFAFAFA)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            TicketsInfoRow("예매번호", Tickets.TicketsNumber)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFE0E0E0))
                            Spacer(modifier = Modifier.height(8.dp))
                            TicketsInfoRow("예매일", Tickets.TicketsDate)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFE0E0E0))
                            Spacer(modifier = Modifier.height(8.dp))
                            TicketsInfoRow("좌석", Tickets.seatInfo)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "결제 금액",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "${Tickets.price.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}원",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9C27B0)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Download Ticket Button
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Download",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "티켓 다운로드",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cancel Ticket Button
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935)
                        ),
                        shape = RoundedCornerShape(26.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = Brush.linearGradient(listOf(Color(0xFFE53935), Color(0xFFE53935)))
                        )
                    ) {
                        Text(
                            text = "예매 취소",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cancellation Policy
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF8E1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = Color(0xFFFF8F00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "취소 및 환불 규정",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF8F00)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "공연 시작 7일 전까지 전액 환불이 가능합니다. 이후 취소 시 수수료가 발생합니다.",
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFF6D4C41)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconTint: Color = Color.Gray
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun TicketsInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TicketsDetailDialogPreview() {
    TicketsDetailDialog(
        Tickets = TicketsDetail(
            id = "1",
            title = "BTS World Tour 2024",
            artist = "BTS",
            venue = "잠실 올림픽 주경기장",
            date = "2024.12.25 19:00",
            seatInfo = "A구역 12열 15번",
            status = TicketsStatus.RESERVED,
            TicketsNumber = "TK20241115001",
            TicketsDate = "2024.11.15",
            price = 150000,
            imageGradient = listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
        ),
        onDismiss = {}
    )
}