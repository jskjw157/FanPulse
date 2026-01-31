package com.aos.fanpulse.presentation.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data Classes
data class ChartSong(
    val rank: Int,
    val title: String,
    val artist: String,
    val albumArt: Int, // drawable resource
    val rankChange: RankChange,
    val albumColor: Color
)

sealed class RankChange {
    object New : RankChange()
    data class Up(val positions: Int) : RankChange()
    data class Down(val positions: Int) : RankChange()
    object Same : RankChange()
}

enum class ChartType {
    MELON, BILLBOARD, BUGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen() {
    var selectedChart by remember { mutableStateOf(ChartType.MELON) }

    val songs = listOf(
        ChartSong(
            rank = 1,
            title = "NewJeans",
            artist = "Super Shy",
            albumArt = 0,
            rankChange = RankChange.Same,
            albumColor = Color(0xFFFFB3E6)
        ),
        ChartSong(
            rank = 2,
            title = "Jungkook (BTS)",
            artist = "Seven",
            albumArt = 0,
            rankChange = RankChange.Up(1),
            albumColor = Color(0xFF1A1A1A)
        ),
        ChartSong(
            rank = 3,
            title = "(G)I-DLE",
            artist = "Queencard",
            albumArt = 0,
            rankChange = RankChange.Down(1),
            albumColor = Color(0xFFE91E63)
        ),
        ChartSong(
            rank = 4,
            title = "aespa",
            artist = "Spicy",
            albumArt = 0,
            rankChange = RankChange.Up(1),
            albumColor = Color(0xFFB71C1C)
        ),
        ChartSong(
            rank = 5,
            title = "IVE",
            artist = "Kitsch",
            albumArt = 0,
            rankChange = RankChange.Down(1),
            albumColor = Color(0xFF4DD0E1)
        ),
        ChartSong(
            rank = 6,
            title = "NewJeans",
            artist = "Ditto",
            albumArt = 0,
            rankChange = RankChange.Same,
            albumColor = Color(0xFFB0BEC5)
        ),
        ChartSong(
            rank = 7,
            title = "NewJeans",
            artist = "Hype Boy",
            albumArt = 0,
            rankChange = RankChange.Up(1),
            albumColor = Color(0xFFFFF9C4)
        ),
        ChartSong(
            rank = 8,
            title = "STAYC",
            artist = "Teddy Bear",
            albumArt = 0,
            rankChange = RankChange.Down(1),
            albumColor = Color(0xFFFFC0CB)
        ),
        ChartSong(
            rank = 9,
            title = "NewJeans",
            artist = "OMG",
            albumArt = 0,
            rankChange = RankChange.Up(1),
            albumColor = Color(0xFFBA68C8)
        ),
        ChartSong(
            rank = 10,
            title = "LE SSERAFIM",
            artist = "Unforgiven",
            albumArt = 0,
            rankChange = RankChange.Down(1),
            albumColor = Color(0xFF00695C)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "차트 순위",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
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
            // Chart Type Tabs
            ChartTypeTabs(
                selectedChart = selectedChart,
                onChartSelected = { selectedChart = it }
            )

            // Songs List
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(songs) { index, song ->
                    ChartSongItem(song)
                }
            }
        }
    }
}

@Composable
fun ChartTypeTabs(
    selectedChart: ChartType,
    onChartSelected: (ChartType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChartTabButton(
            text = "Melon",
            icon = Icons.Default.Person,
            isSelected = selectedChart == ChartType.MELON,
            onClick = { onChartSelected(ChartType.MELON) }
        )
        ChartTabButton(
            text = "Billboard",
            icon = Icons.Default.Person,
            isSelected = selectedChart == ChartType.BILLBOARD,
            onClick = { onChartSelected(ChartType.BILLBOARD) }
        )
        ChartTabButton(
            text = "Bugs",
            icon = Icons.Default.Person,
            isSelected = selectedChart == ChartType.BUGS,
            onClick = { onChartSelected(ChartType.BUGS) }
        )
    }
}

@Composable
fun ChartTabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF9C27B0) else Color(0xFFF5F5F5),
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChartSongItem(song: ChartSong) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = "${song.rank}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (song.rank <= 3) Color(0xFFFF9800) else Color.Gray,
                modifier = Modifier.width(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Album Art
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(song.albumColor)
            ) {
                // Placeholder for album art image
                // Image would go here
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artist,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // Rank Change Indicator
            RankChangeIndicator(song.rankChange)
        }
    }
}

@Composable
fun RankChangeIndicator(rankChange: RankChange) {
    when (rankChange) {
        is RankChange.Up -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Up",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${rankChange.positions}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE53935)
                )
            }
        }
        is RankChange.Down -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Down",
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${rankChange.positions}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E88E5)
                )
            }
        }
        is RankChange.Same -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "─",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        is RankChange.New -> {
            Surface(
                color = Color(0xFFE53935),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "NEW",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ChartScreenPreview() {
    ChartScreen()
}