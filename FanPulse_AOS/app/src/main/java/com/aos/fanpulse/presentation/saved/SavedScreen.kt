package com.aos.fanpulse.presentation.saved

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
data class SavedPost(
    val id: String,
    val author: String,
    val authorBadge: String? = null,
    val timeAgo: String,
    val content: String,
    val imageRes: Int?, // nullable for posts without images
    val likes: Int,
    val comments: Int,
    val tags: List<String>,
    val imageGradient: List<Color>? = null,
    val isSaved: Boolean = true
)

enum class SavedPostTab {
    POST, COMMUNITY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(SavedPostTab.POST) }

    val savedPosts = listOf(
        SavedPost(
            id = "1",
            author = "ARMY_Forever",
            authorBadge = "VIP",
            timeAgo = "5시간 전",
            content = "BTS 새 앨범 티저 영상 보셨나요? 진짜 너무 기대됩니다!\n💜 컴백 준비하는 모습 보니 벌써부터 설레네요.",
            imageRes = 0,
            likes = 1234,
            comments = 89,
            tags = listOf("#BTS", "#컴백"),
            imageGradient = listOf(Color(0xFF6B1B9A), Color(0xFF9C4DCC))
        ),
        SavedPost(
            id = "2",
            author = "Blink_Girl",
            authorBadge = "PRO",
            timeAgo = "1시간 전",
            content = "BLACKPINK 월드투어 서울 공연 티켓팅 성공했어요!\n로제의 직캠보 수 있게 됐어요 ㅠㅠ",
            imageRes = 0,
            likes = 892,
            comments = 67,
            tags = listOf("#BLACKPINK", "#콘서트"),
            imageGradient = listOf(Color(0xFFD81B60), Color(0xFFEC407A))
        ),
        SavedPost(
            id = "3",
            author = "Kpop_Lover",
            authorBadge = "VIP",
            timeAgo = "3시간 전",
            content = "오늘 음악방송 무대 레전드였어요! 직캠 보고 또 보고\n감이샤 💙",
            imageRes = 0,
            likes = 567,
            comments = 45,
            tags = listOf("#음악방송", "#무대"),
            imageGradient = listOf(Color(0xFF1E88E5), Color(0xFF42A5F5))
        ),
        SavedPost(
            id = "4",
            author = "Music_Fan",
            authorBadge = "PRO",
            timeAgo = "2일 전",
            content = "이번 주 차트 순위 업데이트! 우리 아티스트 1위 유지 중 💪",
            imageRes = 0,
            likes = 423,
            comments = 34,
            tags = listOf("#차트", "#순위"),
            imageGradient = listOf(Color(0xFF5E35B1), Color(0xFF7E57C2))
        ),
        SavedPost(
            id = "5",
            author = "Fan_Club",
            authorBadge = "VIP",
            timeAgo = "3일 전",
            content = "팬미팅 신청 받기가 마감되었어요! 당첨 되신 분들 축하 드려요 💚",
            imageRes = 0,
            likes = 756,
            comments = 52,
            tags = listOf("#팬미팅", "#이벤트"),
            imageGradient = listOf(Color(0xFFC62828), Color(0xFFE53935))
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "저장한 게시물",
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
            // Saved Count Card
            SavedCountCard(count = savedPosts.size)

            // Tab Row
            SavedPostTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // Posts List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(savedPosts) { post ->
                    SavedPostItem(post)
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
fun SavedCountCard(count: Int) {
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
                    text = "저장한 게시물",
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
                imageVector = Icons.Default.Person,
                contentDescription = "Saved",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
fun SavedPostTabRow(
    selectedTab: SavedPostTab,
    onTabSelected: (SavedPostTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedTab == SavedPostTab.POST,
            onClick = { onTabSelected(SavedPostTab.POST) },
            label = {
                Text(
                    text = "게시물",
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
                selected = selectedTab == SavedPostTab.POST,
                borderColor = if (selectedTab == SavedPostTab.POST) Color.Transparent else Color(0xFFE0E0E0)
            )
        )

        FilterChip(
            selected = selectedTab == SavedPostTab.COMMUNITY,
            onClick = { onTabSelected(SavedPostTab.COMMUNITY) },
            label = {
                Text(
                    text = "커뮤니티",
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
                selected = selectedTab == SavedPostTab.COMMUNITY,
                borderColor = if (selectedTab == SavedPostTab.COMMUNITY) Color.Transparent else Color(0xFFE0E0E0)
            )
        )
    }
}

@Composable
fun SavedPostItem(post: SavedPost) {
    var isSaved by remember { mutableStateOf(post.isSaved) }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Author Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Author Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF9C27B0),
                                        Color(0xFFE91E63)
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = post.author,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            // Badge
                            if (post.authorBadge != null) {
                                Surface(
                                    color = if (post.authorBadge == "VIP") Color(0xFF9C27B0) else Color(0xFFE91E63),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = post.authorBadge,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = post.timeAgo,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Bookmark Button
                IconButton(
                    onClick = { isSaved = !isSaved },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Person else Icons.Default.Person,
                        contentDescription = "Save",
                        tint = if (isSaved) Color(0xFF9C27B0) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Content
            Text(
                text = post.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            if (post.tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(post.tags) { tag ->
                        Surface(
                            color = Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color(0xFF9C27B0),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Post Image
            if (post.imageRes != null && post.imageGradient != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = post.imageGradient
                            )
                        )
                ) {
                    // Placeholder for actual image
                    // Image would go here
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Interaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Likes
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = post.likes.toString(),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    // Comments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Comment",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = post.comments.toString(),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                // More options
                Text(
                    text = "저장 • 공유",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SavedPostsScreenPreview() {
    SavedScreen()
}