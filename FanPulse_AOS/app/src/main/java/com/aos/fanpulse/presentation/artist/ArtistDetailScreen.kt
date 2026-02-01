package com.aos.fanpulse.presentation.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data Classes
data class ArtistDetail(
    val name: String,
    val followers: String,
    val rank: Int,
    val monthlyListeners: String,
    val description: String,
    val members: List<String>,
    val achievements: List<Achievement>,
    val imageGradient: List<Color>
)

data class Achievement(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Person
)

enum class ArtistTab {
    OVERVIEW, NEWS, SCHEDULE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ArtistTab.OVERVIEW) }
    var isFollowing by remember { mutableStateOf(false) }

    val artist = ArtistDetail(
        name = "BTS",
        followers = "2.5M",
        rank = 1,
        monthlyListeners = "1.2M",
        description = "방탄소년단(BTS)은 대한민국의 보이 그룹으로, 2013년 6월 13일에 데뷔했습니다. 빅히트 뮤직 소속이며, 전 세계적으로 가장 성공한 K-POP 그룹 중 하나입니다.",
        members = listOf("RM", "Jin", "Suga", "J-Hope", "Jimin", "V", "Jungkook"),
        achievements = listOf(
            Achievement("Billboard Hot 100 1위"),
            Achievement("Grammy Awards 후보"),
            Achievement("유엔 연설"),
            Achievement("글로벌 앨범 판매 3천만장 돌파")
        ),
        imageGradient = listOf(
            Color(0xFF6B1B9A),
            Color(0xFF1E3A8A),
            Color(0xFFD946A6)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "아티스트",
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
                            imageVector = Icons.Default.Share,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Artist Header Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = artist.imageGradient
                            )
                        )
                ) {
                    // Artist silhouette would go here
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = artist.name,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Followers",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = artist.followers,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Rank",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "#${artist.rank}",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Follow and Share Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { isFollowing = !isFollowing },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color(0xFFF0F0F0) else Color(0xFF9C27B0),
                            contentColor = if (isFollowing) Color.Gray else Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Follow",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFollowing) "팔로잉" else "팔로우",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(24.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFFE0E0E0)))
                        )
                    ) {
                        Text(
                            text = "투표하기",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Stats Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = artist.followers,
                        label = "팔로워",
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = artist.monthlyListeners,
                        label = "투표수",
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = "#${artist.rank}",
                        label = "순위",
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tab Row
            item {
                Spacer(modifier = Modifier.height(24.dp))
                ArtistTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // Tab Content
            when (selectedTab) {
                ArtistTab.OVERVIEW -> {
                    // Overview Section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "소개",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = artist.description,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Members Section
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "멤버",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(artist.members) { member ->
                                MemberChip(member)
                            }
                        }
                    }

                    // Achievements Section
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "주요 성과",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(artist.achievements) { achievement ->
                        AchievementItem(achievement)
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
                ArtistTab.NEWS -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "뉴스 콘텐츠",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                ArtistTab.SCHEDULE -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "일정 콘텐츠",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ArtistTabRow(
    selectedTab: ArtistTab,
    onTabSelected: (ArtistTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        ArtistTabButton(
            text = "개요",
            isSelected = selectedTab == ArtistTab.OVERVIEW,
            onClick = { onTabSelected(ArtistTab.OVERVIEW) },
            modifier = Modifier.weight(1f)
        )
        ArtistTabButton(
            text = "뉴스",
            isSelected = selectedTab == ArtistTab.NEWS,
            onClick = { onTabSelected(ArtistTab.NEWS) },
            modifier = Modifier.weight(1f)
        )
        ArtistTabButton(
            text = "일정",
            isSelected = selectedTab == ArtistTab.SCHEDULE,
            onClick = { onTabSelected(ArtistTab.SCHEDULE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ArtistTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF9C27B0) else Color.Gray,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFF9C27B0))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )
        }
    }
}

@Composable
fun MemberChip(name: String) {
    Surface(
        color = Color(0xFFE1BEE7),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            color = Color(0xFF9C27B0),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = achievement.icon,
            contentDescription = null,
            tint = Color(0xFF9C27B0),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = achievement.title,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ArtistDetailScreenPreview() {
    ArtistDetailScreen()
}