package com.aos.fanpulse.presentation.artist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R
import com.aos.fanpulse.presentation.notifications.NotificationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("전체", "보이그룹", "걸그룹", "솔로")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.color_12))
    ) {
        // Top App Bar
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(id = R.color.color_1)
            ),
            title = {
                Icon(
                    painter = painterResource(id = R.drawable.home_title),
                    contentDescription = "홈 타이틀 로고",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(80.dp, 24.dp)
                )
            },
            actions = {
                IconButton(onClick = { /* 검색 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_search),
                        contentDescription = "검색",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* 알림 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_search),
                        contentDescription = "알림",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* 메뉴 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_search),
                        contentDescription = "메뉴",
                        tint = Color.White
                    )
                }
            }
        )

        // Title Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "아티스트",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "좋아하는 아티스트를 팔로우하세요",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = colorResource(id = R.color.color_1),
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = colorResource(id = R.color.color_1)
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        if (index == 0) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_search),
                                contentDescription = null,
                                tint = if (selectedTab == index)
                                    colorResource(id = R.color.color_1) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else if (index == 1) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_search),
                                contentDescription = null,
                                tint = if (selectedTab == index)
                                    colorResource(id = R.color.color_1) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else if (index == 2) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_person),
                                contentDescription = null,
                                tint = if (selectedTab == index)
                                    colorResource(id = R.color.color_1) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_search),
                                contentDescription = null,
                                tint = if (selectedTab == index)
                                    colorResource(id = R.color.color_1) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index)
                                colorResource(id = R.color.color_1) else Color.Gray
                        )
                    }
                }
            }
        }

        // Artist Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.color_12)),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(getArtistList()) { artist ->
                ArtistItem(artist = artist)
            }
        }
    }
}

@Composable
fun ArtistItem(artist: Artist) {
    var isFavorite by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { /* 아티스트 상세로 이동 */ }
        ) {
            // Artist Image
            Image(
                painter = painterResource(id = artist.imageRes),
                contentDescription = artist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Ranking Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(
                        color = colorResource(id = R.color.color_1),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${artist.ranking}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Artist Name
        Text(
            text = artist.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Artist Description
        Text(
            text = artist.description,
            fontSize = 12.sp,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Followers and Like
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_person),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatFollowers(artist.followers),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { isFavorite = !isFavorite },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (isFavorite) colorResource(id = R.color.color_1) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Data Classes
data class Artist(
    val id: Int,
    val name: String,
    val description: String,
    val followers: Int,
    val imageRes: Int,
    val ranking: Int
)

// Sample Data
fun getArtistList(): List<Artist> {
    return listOf(
        Artist(1, "BTS", "글로벌 K-POP 아이콘", 2500000, R.drawable.person_ex1, 1),
        Artist(2, "BLACKPINK", "세계를 사로잡은 걸그룹", 2300000, R.drawable.person_ex1, 2),
        Artist(3, "NewJeans", "신선한 매력의 신인 그룹", 1800000, R.drawable.person_ex1, 3),
        Artist(4, "IU", "국민 여동생", 1600000, R.drawable.person_ex1, 4),
        Artist(5, "SEVENTEEN", "자체 제작 아이돌", 1500000, R.drawable.person_ex1, 5),
        Artist(6, "aespa", "미래형 걸그룹", 1400000, R.drawable.person_ex1, 6),
        Artist(7, "Stray Kids", "자작곡 강자", 1300000, R.drawable.person_ex1, 7),
        Artist(8, "IVE", "자신감 넘치는 신예", 1200000, R.drawable.person_ex1, 8),
        Artist(9, "Jungkook", "BTS 황금막내", 1100000, R.drawable.person_ex1, 9),
        Artist(10, "LE SSERAFIM", "두려움 없는 걸그룹", 1000000, R.drawable.person_ex1, 10),
        Artist(11, "TWICE", "사랑스러운 매력", 950000, R.drawable.person_ex1, 11),
        Artist(12, "TXT", "젊은이의 이야기", 900000, R.drawable.person_ex1, 12)
    )
}

fun formatFollowers(followers: Int): String {
    return when {
        followers >= 1000000 -> "${followers / 100000 / 10.0}M"
        followers >= 1000 -> "${followers / 1000}K"
        else -> followers.toString()
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ArtistScreenPreview() {
    ArtistScreen()
}