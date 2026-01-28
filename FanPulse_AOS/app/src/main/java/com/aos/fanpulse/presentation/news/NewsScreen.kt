package com.aos.fanpulse.presentation.news

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

data class NewsItem(
    val id: Int,
    val imageRes: Int,
    val category: String,
    val title: String,
    val viewCount: String,
    val commentCount: String,
    val source: String,
    val categoryColor: Color = Color(0xFF9C27B0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("전체", "뉴스", "공연", "기사", "영상")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "뉴스",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.icon_left_arrow),
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.icon_search),
                            contentDescription = "검색"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF121212))
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // News List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(getNewsItems()) { newsItem ->
                    NewsCard(newsItem)
                }
            }
        }
    }
}

@Composable
fun NewsCard(newsItem: NewsItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFF9C27B0)
                                )
                            )
                        )
                )
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    color = newsItem.categoryColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = newsItem.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = newsItem.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_myscreen_ex1),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = newsItem.viewCount,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            painter = painterResource(R.drawable.icon_chat),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = newsItem.commentCount,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = newsItem.source,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

fun getNewsItems(): List<NewsItem> {
    return listOf(
        NewsItem(
            id = 1,
            imageRes = 0,
            category = "공연",
            title = "BTS 제 앨범 발매 예정",
            viewCount = "1.2천",
            commentCount = "2천",
            source = "스포츠조선"
        ),
        NewsItem(
            id = 2,
            imageRes = 0,
            category = "공연",
            title = "BLACKPINK 월드투어 4차 공연",
            viewCount = "982",
            commentCount = "1.5천",
            source = "텐아시아"
        ),
        NewsItem(
            id = 3,
            imageRes = 0,
            category = "공연",
            title = "SEVENTEEN 4월 컴백 1위",
            viewCount = "856",
            commentCount = "1.2천",
            source = "스타뉴스"
        ),
        NewsItem(
            id = 4,
            imageRes = 0,
            category = "기사",
            title = "NewJeans 글로벌 인기 글로벌 증명",
            viewCount = "745",
            commentCount = "980",
            source = "마이데일리"
        ),
        NewsItem(
            id = 5,
            imageRes = 0,
            category = "공연",
            title = "IVE 단독 콘서트부터 1천석 돌파",
            viewCount = "1.1천",
            commentCount = "1.8천",
            source = "OSEN"
        ),
        NewsItem(
            id = 6,
            imageRes = 0,
            category = "공연",
            title = "Stray Kids 데뷔 후의 첫번째 매진",
            viewCount = "923",
            commentCount = "1.1천",
            source = "스포츠동아"
        ),
        NewsItem(
            id = 7,
            imageRes = 0,
            category = "영상",
            title = "aespa 새 앨범 타이틀 곡 공개",
            viewCount = "1.3천",
            commentCount = "2.2천",
            source = "엑스포츠뉴스"
        ),
        NewsItem(
            id = 8,
            imageRes = 0,
            category = "공연",
            title = "TWICE 밤을 통 생생한 열정",
            viewCount = "1.5천",
            commentCount = "2.5천",
            source = "헤럴드팝"
        ),
        NewsItem(
            id = 9,
            imageRes = 0,
            category = "기사",
            title = "TXT 일본도 치고 전진",
            viewCount = "687",
            commentCount = "890",
            source = "스타뉴스"
        ),
        NewsItem(
            id = 10,
            imageRes = 0,
            category = "공연",
            title = "LE SSERAFIM 새 앨범 대박 행진 시작",
            viewCount = "1.4천",
            commentCount = "2.1천",
            source = "텐아시아"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewsScreen()
}