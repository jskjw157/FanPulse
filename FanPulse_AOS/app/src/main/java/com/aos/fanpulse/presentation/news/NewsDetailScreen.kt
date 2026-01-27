package com.aos.fanpulse.presentation.news

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

data class RelatedNews(
    val category: String,
    val title: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen() {
    var isLiked by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(1200) }

    val relatedNewsList = remember {
        listOf(
            RelatedNews(
                category = "차트",
                title = "SEVENTEEN 새 앨범 차트 1위",
                imageUrl = ""
            ),
            RelatedNews(
                category = "뉴스",
                title = "NewJeans 글로벌 인기 급상승",
                imageUrl = ""
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "뉴스 상세",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* 뒤로가기 */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_left_arrow),
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 공유 */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_share),
                            contentDescription = "공유"
                        )
                    }
                    IconButton(onClick = {
                        isBookmarked = !isBookmarked
                    }) {
                        Icon(
                            painter = if (isBookmarked) painterResource(id = R.drawable.icon_share) else painterResource(id = R.drawable.icon_share),
                            contentDescription = "북마크",
                            tint = if (isBookmarked) Color(0xFFB794F6) else Color(0xFF333333)
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
                .background(Color.White)
                .padding(paddingValues)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(Color(0xFF1A1A2E))
                ) {
                    // 이미지 placeholder - 실제로는 AsyncImage 사용
                    // AsyncImage(
                    //     model = "your_image_url",
                    //     contentDescription = null,
                    //     modifier = Modifier.fillMaxSize(),
                    //     contentScale = ContentScale.Crop
                    // )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                        contentDescription = null,
                        tint = Color(0xFFB794F6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "2024.12.10",
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            item {
                Text(
                    text = "BTS 새 앨범 발매 예정",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB794F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_person_ex2),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "FanPulse 편집부",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "공식 기자",
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "방탄소년단(BTS)이 2025년 초 새 앨범 발매를 예고했습니다.",
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "소속사 빅히트 뮤직은 공식 SNS를 통해 \"BTS가 새로운 앨범 작업에 박차를 가하고 있다\"며 \"팬 여러분께 좋은 소식을 전할 수 있도록 최선을 다하고 있다\"고 밝혔습니다.",
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "이번 앨범은 멤버들이 직접 프로듀싱에 참여하며, 글로벌 팬들을 위한 특별한 메시지를 담을 예정입니다.",
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "업계 관계자는 \"BTS의 새 앨범은 K-POP의 새로운 장을 열 것\"이라며 \"전 세계 팬들의 기대가 매우 높다\"고 전했습니다.",
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "앨범 발매 일정과 수록곡 등 자세한 내용은 추후 공개될 예정입니다.",
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF333333)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F8F8),
                        onClick = {
                            if (isLiked) {
                                likeCount--
                            } else {
                                likeCount++
                            }
                            isLiked = !isLiked
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = if (isLiked) painterResource(R.drawable.icon_heart) else painterResource(R.drawable.icon_heart),
                                contentDescription = "좋아요",
                                tint = if (isLiked) Color(0xFFEC4899) else Color(0xFF999999),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "좋아요 ${likeCount / 1000.0}K",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F8F8),
                        onClick = { /* 댓글 */ }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.icon_chat),
                                contentDescription = "댓글",
                                tint = Color(0xFF999999),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "댓글 234",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "관련 뉴스",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(relatedNewsList.size) { index ->
                RelatedNewsItem(news = relatedNewsList[index])
                if (index < relatedNewsList.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun RelatedNewsItem(news: RelatedNews) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { /* 뉴스 클릭 */ },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F8F8)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3A3A5A))
            ) {
                // AsyncImage 사용
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = news.category,
                    fontSize = 12.sp,
                    color = Color(0xFFB794F6),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = news.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A),
                    maxLines = 2
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsDetailScreenPreview() {
    NewsDetailScreen()
}