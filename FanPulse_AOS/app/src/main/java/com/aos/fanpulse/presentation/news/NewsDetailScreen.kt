package com.aos.fanpulse.presentation.news

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aos.fanpulse.R
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun NewsDetailScreen(
    viewModel: NewsDetailViewModel = hiltViewModel(),
    newsId: String? = null,
    onBackClick: () -> Unit = {}
) {

    val state by viewModel.collectAsState()

    LaunchedEffect(newsId) {
        if (newsId != null) {
            viewModel.getNewsDetail(newsId)
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is NewsDetailContract.SideEffect.ShowToast -> {}
        }
    }

    NewsDetailContent(
        state = state,
        viewModel,
        onBackClick = onBackClick
    )
}

//@Composable
//fun RelatedNewsItem(news: RelatedNews) {
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp)
//            .clickable { /* 뉴스 클릭 */ },
//        shape = RoundedCornerShape(12.dp),
//        color = Color(0xFFF8F8F8)
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//
//            Box(
//                modifier = Modifier
//                    .size(80.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color(0xFF3A3A5A))
//            ) {
//                // AsyncImage 사용
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(
//                    text = news.category,
//                    fontSize = 12.sp,
//                    color = Color(0xFFB794F6),
//                    fontWeight = FontWeight.Medium
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = news.title,
//                    fontSize = 15.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color(0xFF1A1A1A),
//                    maxLines = 2
//                )
//            }
//        }
//    }
//}

@Composable
fun NewsDetailContent(
    state: NewsDetailContract.NewsDetailState,
    viewModel: NewsDetailViewModel,
    onBackClick: () -> Unit
){

    var isLiked by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(1200) }

//    val relatedNewsList = remember {
//        listOf(
//            RelatedNews(
//                category = "차트",
//                title = "SEVENTEEN 새 앨범 차트 1위",
//                imageUrl = ""
//            ),
//            RelatedNews(
//                category = "뉴스",
//                title = "NewJeans 글로벌 인기 급상승",
//                imageUrl = ""
//            )
//        )
//    }

    if (state.newsDetail == null){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CommonTopAppBar(
                isActiveLeftBack = true,
                onLeftBack = { onBackClick() },
                isActiveCenterTextTitle = true,
                centerTextTitle = "뉴스 상세",
                isActiveRightShare = true,
                onRightShare = {  },
                isActiveRightBookmark = true,
                onRightBookmark = {  }
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
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
                        text = state.newsDetail.title,
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

                        //  기자에 대한 정보
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
                            text = state.newsDetail.content,
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

//                items(relatedNewsList.size) { index ->
//                    RelatedNewsItem(news = relatedNewsList[index])
//                    if (index < relatedNewsList.size - 1) {
//                        Spacer(modifier = Modifier.height(12.dp))
//                    }
//                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsDetailScreenPreview() {
    NewsDetailScreen()
}