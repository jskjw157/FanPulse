package com.aos.fanpulse.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data Classes
data class FavoriteArtist(
    val id: String,
    val name: String,
    val type: ArtistType,
    val followers: String,
    val recentActivity: String,
    val imageRes: Int, // drawable resource
    val isFavorite: Boolean = true,
    val hasNotification: Boolean = false
)

enum class ArtistType(val displayName: String) {
    BOY_GROUP("Boy Group"),
    GIRL_GROUP("Girl Group"),
    SOLO("Solo Artist")
}

enum class ArtistFilterTab {
    ALL, BOY_GROUP, GIRL_GROUP, SOLO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(ArtistFilterTab.ALL) }

    val artists = listOf(
        FavoriteArtist(
            id = "1",
            name = "BTS",
            type = ArtistType.BOY_GROUP,
            followers = "2.5M 팔로워",
            recentActivity = "새 앨범 발매 예정",
            imageRes = 0,
            hasNotification = true
        ),
        FavoriteArtist(
            id = "2",
            name = "BLACKPINK",
            type = ArtistType.GIRL_GROUP,
            followers = "2.3M 팔로워",
            recentActivity = "월드투어 티켓 오픈",
            imageRes = 0,
            hasNotification = true
        ),
        FavoriteArtist(
            id = "3",
            name = "IU",
            type = ArtistType.SOLO,
            followers = "1.8M 팔로워",
            recentActivity = "드라마 OST 발매",
            imageRes = 0,
            hasNotification = false
        ),
        FavoriteArtist(
            id = "4",
            name = "SEVENTEEN",
            type = ArtistType.BOY_GROUP,
            followers = "1.5M 팔로워",
            recentActivity = "콘서트 일정 공개",
            imageRes = 0,
            hasNotification = true
        ),
        FavoriteArtist(
            id = "5",
            name = "NewJeans",
            type = ArtistType.GIRL_GROUP,
            followers = "1.2M 팔로워",
            recentActivity = "신곡 티저 공개",
            imageRes = 0,
            hasNotification = true
        ),
        FavoriteArtist(
            id = "6",
            name = "Stray Kids",
            type = ArtistType.BOY_GROUP,
            followers = "1.1M 팔로워",
            recentActivity = "해외 공연 확정",
            imageRes = 0,
            hasNotification = false
        )
    )

    val filteredArtists = when (selectedFilter) {
        ArtistFilterTab.ALL -> artists
        ArtistFilterTab.BOY_GROUP -> artists.filter { it.type == ArtistType.BOY_GROUP }
        ArtistFilterTab.GIRL_GROUP -> artists.filter { it.type == ArtistType.GIRL_GROUP }
        ArtistFilterTab.SOLO -> artists.filter { it.type == ArtistType.SOLO }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "좋아요한 아티스트",
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
            // Favorite Count Card
            FavoriteCountCard(count = artists.size)

            // Filter Tabs
            ArtistFilterTabs(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                allCount = artists.size,
                boyGroupCount = artists.count { it.type == ArtistType.BOY_GROUP },
                girlGroupCount = artists.count { it.type == ArtistType.GIRL_GROUP },
                soloCount = artists.count { it.type == ArtistType.SOLO }
            )

            // Artists List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredArtists) { artist ->
                    ArtistItem(artist)
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
fun FavoriteCountCard(count: Int) {
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
                    text = "팔로우 중인 아티스트",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$count",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
fun ArtistFilterTabs(
    selectedFilter: ArtistFilterTab,
    onFilterSelected: (ArtistFilterTab) -> Unit,
    allCount: Int,
    boyGroupCount: Int,
    girlGroupCount: Int,
    soloCount: Int
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == ArtistFilterTab.ALL,
                onClick = { onFilterSelected(ArtistFilterTab.ALL) },
                label = {
                    Text(
                        text = "전체 ($allCount)",
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
                    selected = selectedFilter == ArtistFilterTab.ALL,
                    borderColor = if (selectedFilter == ArtistFilterTab.ALL) Color.Transparent else Color(0xFFE0E0E0)
                )
            )
        }

        item {
            FilterChip(
                selected = selectedFilter == ArtistFilterTab.BOY_GROUP,
                onClick = { onFilterSelected(ArtistFilterTab.BOY_GROUP) },
                label = {
                    Text(
                        text = "Boy Group",
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
                    selected = selectedFilter == ArtistFilterTab.BOY_GROUP,
                    borderColor = if (selectedFilter == ArtistFilterTab.BOY_GROUP) Color.Transparent else Color(0xFFE0E0E0)
                )
            )
        }

        item {
            FilterChip(
                selected = selectedFilter == ArtistFilterTab.GIRL_GROUP,
                onClick = { onFilterSelected(ArtistFilterTab.GIRL_GROUP) },
                label = {
                    Text(
                        text = "Girl Group",
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
                    selected = selectedFilter == ArtistFilterTab.GIRL_GROUP,
                    borderColor = if (selectedFilter == ArtistFilterTab.GIRL_GROUP) Color.Transparent else Color(0xFFE0E0E0)
                )
            )
        }

        item {
            FilterChip(
                selected = selectedFilter == ArtistFilterTab.SOLO,
                onClick = { onFilterSelected(ArtistFilterTab.SOLO) },
                label = {
                    Text(
                        text = "Solo",
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
                    selected = selectedFilter == ArtistFilterTab.SOLO,
                    borderColor = if (selectedFilter == ArtistFilterTab.SOLO) Color.Transparent else Color(0xFFE0E0E0)
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun ArtistItem(artist: FavoriteArtist) {
    var isFavorite by remember { mutableStateOf(artist.isFavorite) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
            // Artist Image
            Box(
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F0F0))
                ) {
                    // Placeholder for artist image
                    // Image would go here
                }

                // Notification Badge
                if (artist.hasNotification) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9C27B0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Artist Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = artist.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = artist.type.displayName,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Followers",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = artist.followers,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = artist.recentActivity,
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Notification Bell Icon
                if (!artist.hasNotification) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Notifications Off",
                            tint = Color.Gray
                        )
                    }
                }

                // Favorite Button
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFE91E63) else Color.Gray
                    )
                }
            }
        }
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun FavoriteArtistsScreenPreview() {
    FavoritesScreen()
}