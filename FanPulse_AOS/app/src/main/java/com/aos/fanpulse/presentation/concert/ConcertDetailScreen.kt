package com.aos.fanpulse.presentation.concert

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

data class ConcertSchedule(
    val date: String,
    val time: String,
    val isSelected: Boolean = false,
    val isSoldOut: Boolean = false
)

data class SeatType(
    val name: String,
    val price: String,
    val isSoldOut: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcertDetailScreen(
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onReservationClick: () -> Unit = {}
) {
    var selectedScheduleIndex by remember { mutableStateOf(0) }

    val schedules = listOf(
        ConcertSchedule("2024-12-20", "19:00 시작", isSelected = true),
        ConcertSchedule("2024-12-21", "19:00 시작"),
        ConcertSchedule("2024-12-22", "18:00 시작", isSoldOut = true)
    )

    val seatTypes = listOf(
        SeatType("VIP석", "220,000원"),
        SeatType("R석", "165,000원"),
        SeatType("S석", "132,000원"),
        SeatType("A석", "매진", isSoldOut = true)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "공연 상세",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.icon_left_arrow),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            painter = painterResource(R.drawable.icon_share),
                            contentDescription = "Share"
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
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // Concert Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2D1B69),
                                Color(0xFF1E3A8A),
                                Color(0xFFD946A6)
                            )
                        )
                    )
            ) {
                // 실제 이미지로 교체 가능
                // Image(...)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title
                Text(
                    text = "BTS World Tour Seoul",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Artist
                Text(
                    text = "BTS",
                    fontSize = 16.sp,
                    color = Color(0xFFAB47BC),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "잠실 올림픽 주경기장",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = "서울특별시 송파구 올림픽로 25",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Schedule Section
                Text(
                    text = "공연 일정",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                schedules.forEachIndexed { index, schedule ->
                    ScheduleItem(
                        schedule = schedule,
                        isSelected = index == selectedScheduleIndex,
                        onClick = {
                            if (!schedule.isSoldOut) {
                                selectedScheduleIndex = index
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Seat Selection Section
                Text(
                    text = "좌석 선택",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    seatTypes.take(2).forEach { seatType ->
                        SeatTypeCard(
                            seatType = seatType,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    seatTypes.drop(2).forEach { seatType ->
                        SeatTypeCard(
                            seatType = seatType,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description Section
                Text(
                    text = "공연 소개",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "BTS의 월드투어가 서울에서 개최됩니다. 최고의 무대와 퍼포먼스 경험하세요!",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Important Info Section
                Text(
                    text = "유의사항",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                val infoItems = listOf(
                    "본 공연은 전석 지정석입니다",
                    "7세 이상 입장 가능합니다",
                    "공연 당일 신분증을 지참해주세요",
                    "티켓 예매 후 취소/환불은 공연 7일 전까지 가능합니다"
                )

                infoItems.forEach { info ->
                    InfoItem(text = info)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Reservation Button
                Button(
                    onClick = onReservationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE1BEE7)
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(
                        text = "티켓 예매하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9C27B0)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: ConcertSchedule,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                // 배경 타입(Color vs Brush)이 섞여 있을 때는 then과 background를 분리하는 게 가장 확실합니다.
                if (schedule.isSoldOut) {
                    Modifier.background(Color(0xFFF5F5F5))
                } else if (isSelected) {
                    Modifier.background(
                        Brush.horizontalGradient(listOf(Color(0xFFAB47BC), Color(0xFFEC407A)))
                    )
                } else {
                    Modifier.background(Color.White)
                }
            )
            .border(
                width = if (!isSelected && !schedule.isSoldOut) 1.dp else 0.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !schedule.isSoldOut, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = schedule.date,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (schedule.isSoldOut) Color.Gray
                    else if (isSelected) Color.White
                    else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = schedule.time,
                    fontSize = 13.sp,
                    color = if (schedule.isSoldOut) Color.Gray
                    else if (isSelected) Color.White.copy(alpha = 0.9f)
                    else Color.Gray
                )
            }

            if (schedule.isSoldOut) {
                Text(
                    text = "매진",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SeatTypeCard(
    seatType: SeatType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seatType.isSoldOut) Color(0xFFF5F5F5) else Color.White
        ),
        border = if (!seatType.isSoldOut) {
            CardDefaults.outlinedCardBorder()
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = seatType.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (seatType.isSoldOut) Color.Gray else Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = seatType.price,
                fontSize = 14.sp,
                color = if (seatType.isSoldOut) Color.Gray else Color.DarkGray
            )
        }
    }
}

@Composable
fun InfoItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFAB47BC))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.DarkGray,
            lineHeight = 20.sp
        )
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ConcertDetailScreenPreview() {
    ConcertDetailScreen()
}