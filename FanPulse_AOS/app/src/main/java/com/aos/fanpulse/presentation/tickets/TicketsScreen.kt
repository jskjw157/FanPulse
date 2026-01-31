package com.aos.fanpulse.presentation.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

// Data Classes
data class TicketsStats(
    val totalTicketss: Int,
    val completedTicketss: Int
)

data class Tickets(
    val id: String,
    val title: String,
    val artist: String,
    val venue: String,
    val date: String,
    val seatInfo: String,
    val price: Int,
    val status: TicketsStatus,
    val imageGradient: List<Color>
)

enum class TicketsStatus(val displayName: String, val color: Color) {
    RESERVED("예매완료", Color(0xFF4CAF50)),
    CANCELLED("취소", Color(0xFFE53935)),
    CONFIRMED("확정", Color(0xFF2196F3))
}

enum class TicketsFilterTab {
    ALL, RESERVED, CANCELLED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(TicketsFilterTab.ALL) }

    val Ticketss = listOf(
        Tickets(
            id = "1",
            title = "BTS World Tour 2024",
            artist = "BTS",
            venue = "잠실 올림픽 주경기장",
            date = "2024.12.25 19:00",
            seatInfo = "A구역 12열 15번",
            price = 150000,
            status = TicketsStatus.RESERVED,
            imageGradient = listOf(Color(0xFF6B1B9A), Color(0xFFD946A6))
        ),
        Tickets(
            id = "2",
            title = "BLACKPINK Born Pink Tour",
            artist = "BLACKPINK",
            venue = "고척 스카이돔",
            date = "2024.12.31 18:00",
            seatInfo = "VIP석 5열 8번",
            price = 200000,
            status = TicketsStatus.RESERVED,
            imageGradient = listOf(Color(0xFFE91E63), Color(0xFFEC407A))
        ),
        Tickets(
            id = "3",
            title = "Seventeen Be The Sun",
            artist = "Seventeen",
            venue = "KSPO DOME",
            date = "2024.11.10 19:00",
            seatInfo = "B구역 8열 20번",
            price = 120000,
            status = TicketsStatus.CANCELLED,
            imageGradient = listOf(Color(0xFFFF6F00), Color(0xFF42A5F5))
        ),
        Tickets(
            id = "4",
            title = "IU The Golden Hour",
            artist = "IU",
            venue = "올림픽공원 제2경기장",
            date = "2024.10.20 18:30",
            seatInfo = "R석 15열 12번",
            price = 99000,
            status = TicketsStatus.CONFIRMED,
            imageGradient = listOf(Color(0xFFFFB74D), Color(0xFFBA68C8))
        )
    )

    val filteredTicketss = when (selectedFilter) {
        TicketsFilterTab.ALL -> Ticketss
        TicketsFilterTab.RESERVED -> Ticketss.filter { it.status == TicketsStatus.RESERVED }
        TicketsFilterTab.CANCELLED -> Ticketss.filter { it.status == TicketsStatus.CANCELLED }
    }

    val stats = TicketsStats(
        totalTicketss = Ticketss.size,
        completedTicketss = Ticketss.count { it.status == TicketsStatus.RESERVED || it.status == TicketsStatus.CONFIRMED }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "예매 내역",
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
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
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
            // Stats Card
            TicketsStatsCard(stats)

            // Filter Tabs
            TicketsFilterTabs(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // Ticketss List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredTicketss) { Tickets ->
                    TicketsItem(Tickets)
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
fun TicketsStatsCard(stats: TicketsStats) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF9C27B0),
                        Color(0xFFE91E63)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "총 예매 내역",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stats.totalTicketss}건",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "예정된 공연",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stats.completedTicketss}건",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TicketsFilterTabs(
    selectedFilter: TicketsFilterTab,
    onFilterSelected: (TicketsFilterTab) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == TicketsFilterTab.ALL,
                onClick = { onFilterSelected(TicketsFilterTab.ALL) },
                label = {
                    Text(
                        text = "전체",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF9C27B0),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF0F0F0),
                    labelColor = Color.Gray
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == TicketsFilterTab.ALL,
                    borderColor = if (selectedFilter == TicketsFilterTab.ALL) Color.Transparent else Color(0xFFE0E0E0)
                )
            )
        }

        item {
            FilterChip(
                selected = selectedFilter == TicketsFilterTab.RESERVED,
                onClick = { onFilterSelected(TicketsFilterTab.RESERVED) },
                label = {
                    Text(
                        text = "예매완료",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF9C27B0),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF0F0F0),
                    labelColor = Color.Gray
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == TicketsFilterTab.RESERVED,
                    borderColor = if (selectedFilter == TicketsFilterTab.RESERVED) Color.Transparent else Color(0xFFE0E0E0)
                )
            )
        }

        item {
            FilterChip(
                selected = selectedFilter == TicketsFilterTab.CANCELLED,
                onClick = { onFilterSelected(TicketsFilterTab.CANCELLED) },
                label = {
                    Text(
                        text = "취소/환불",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF9C27B0),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF0F0F0),
                    labelColor = Color.Gray
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == TicketsFilterTab.CANCELLED,
                    borderColor = if (selectedFilter == TicketsFilterTab.CANCELLED) Color.Transparent else Color(0xFFE0E0E0)
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun TicketsItem(Tickets: Tickets) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Concert Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = Tickets.imageGradient
                        )
                    )
            ) {
                // Placeholder for concert image
                // Image would go here
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tickets Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = Tickets.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    // Status Badge
                    Surface(
                        color = Tickets.status.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = Tickets.status.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Tickets.status.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = Tickets.artist,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Venue
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Tickets.venue,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Date",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Tickets.date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Seat Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Seat",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Tickets.seatInfo,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Price and Action Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "결제 금액",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${Tickets.price.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}원",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
            }

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0F0F0),
                    contentColor = Color.Gray
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "취소 요청",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TicketsHistoryScreenPreview() {
    TicketsScreen()
}