package com.aos.fanpulse.presentation.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.data.remote.apiservice.NewsItem
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

enum class ArtistTab {
    OVERVIEW, NEWS, SCHEDULE
}

@Composable
fun ArtistDetailScreen(
    viewModel: ArtistDetailViewModel = hiltViewModel(),
    artistId: String? = null,
    goNewsDetail: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(artistId) {
        if (artistId != null) {
            viewModel.getArtistDetail(artistId)
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ArtistDetailContract.SideEffect.NavigateNewsDetail -> {
                goNewsDetail(sideEffect.newsId)
            }
            is ArtistDetailContract.SideEffect.ShowToast -> {}
        }
    }

    ArtistDetailContent(
        state = state,
        viewModel,
        onBackClick = onBackClick
    )
}

@Composable
fun ArtistDetailContent(
    state: ArtistDetailContract.ArtistDetailState,
    viewModel: ArtistDetailViewModel,
    onBackClick: () -> Unit
){

    var selectedTab by remember { mutableStateOf(ArtistTab.OVERVIEW) }

    if (state.artistDetail == null || state.newsItems == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CommonTopAppBar(
                isActiveLeftBack = true,
                onLeftBack = { onBackClick() },
                isActiveCenterTextTitle = true,
                centerTextTitle = "아티스트",
                isActiveRightShare = true,
                onRightShare = {  }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Artist Header Image
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        // Artist silhouette would go here
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = state.artistDetail.name,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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
                                text = state.artistDetail.description.toString(),
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
                                items(state.artistDetail.members) { member ->
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

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }

                        //  성과 관련해서 설정이 필요
                    }
                    ArtistTab.NEWS -> {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }


                        items(
                            items = state.newsItems.content,
                            key = { it.id } // UUID 사용
                        ) { newsItem ->
                            NewsItemCard(newsItem = newsItem, onNewsClick = {
                                viewModel.goNewsDetailScreen(it)
                            })
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
fun NewsItemCard(
    newsItem: NewsItem,
    onNewsClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onNewsClick(newsItem.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min), // 높이를 내용에 맞춤
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 썸네일 이미지 영역
            AsyncImage(
                model = newsItem.thumbnailUrl ,//?: R.drawable.placeholder_news, // 이미지 없을 시 대체 이미지
                contentDescription = "News Thumbnail",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            // 2. 텍스트 정보 영역
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // 카테고리 태그 (RELEASE, TOUR 등)
                    Text(
                        text = newsItem.category,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 뉴스 제목
                    Text(
                        text = newsItem.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }

                // 하단 정보 (출처 및 시간)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = newsItem.sourceName ?: "Unknown",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // 구분 점
                    Box(
                        modifier = Modifier
                            .size(2.dp)
                            .background(Color.Gray, shape = CircleShape)
                    )

                    Text(
                        text = formatPublishedAt(newsItem.publishedAt), // 시간 포맷팅 함수 필요
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

fun formatPublishedAt(isoString: String): String {
    return try {
        isoString.substring(0, 10).replace("-", ".")
    } catch (e: Exception) {
        isoString
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ArtistDetailScreenPreview() {
    ArtistDetailScreen()
}