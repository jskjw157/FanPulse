package com.aos.fanpulse.presentation.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.R
import com.aos.fanpulse.data.remote.apiservice.SearchLiveItem
import com.aos.fanpulse.data.remote.apiservice.SearchNewsItem
import com.aos.fanpulse.presentation.artist.formatPublishedAt
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import com.aos.fanpulse.presentation.search.SearchViewModel.RecentSearchTag
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    goNewsScreen: (String) -> Unit = {},
    goLiveScreen: (String) -> Unit = {},
) {

    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchContract.SideEffect.ShowToast -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {

        CommonTopAppBar(
            setBackground = 1,
            isActiveLeftBack = true,
            onLeftBack = { onBackClick() },
            isActiveRightSearch = true,
            isActiveSearchFunction = true,
            onSearchFunction = {
                viewModel.getSearchResult(it, 10)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 검색어",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "전체 삭제",
                fontSize = 14.sp,
                color = Color(0xFF999999),
                modifier = Modifier.clickable {
                    viewModel.deleteAllRecentSearch()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.recentSearchTags.forEach { tag ->
                SearchChip(
                    tag = tag,
                    onRemove = {
                        viewModel.deleteRecentSearch(it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F8F8), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "인기 검색어",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                state.popularSearches.forEachIndexed { index, search ->
                    PopularSearchItem(search = search)
                    if (index != state.popularSearches.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn {
            if (state.newsItems.isNotEmpty()){
                item {
                    Text(
                        text = "뉴스",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
            itemsIndexed(state.newsItems){ index, item ->
                NewsItemCard(item){
                    goNewsScreen(it)
                }
            }
            if (state.liveItems.isNotEmpty()){
                item {
                    Text(
                        text = "라이브",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }

            itemsIndexed(state.liveItems) { index, item ->
                LiveItemCard(item){
                    goLiveScreen(it)
                }
            }
        }
    }
}

@Composable
fun SearchChip(
    tag: RecentSearchTag,
    onRemove: (RecentSearchTag) -> Unit
) {
    Surface(
        modifier = Modifier.wrapContentSize(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_clock),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )

            Text(
                text = tag.text,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                maxLines = 1
            )

            Icon(
                painter = painterResource(id = R.drawable.icon_close),
                contentDescription = "Remove",
                tint = Color(0xFF999999),
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .clickable(onClick = { onRemove(tag) })
            )
        }
    }
}

@Composable
fun PopularSearchItem(search: SearchViewModel.PopularSearch) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val rankColor = when (search.rank) {
            1 -> Color(0xFFB794F6)
            2 -> Color(0xFFEC4899)
            3 -> Color(0xFF8B5CF6)
            4 -> Color(0xFF7C3AED)
            else -> Color(0xFF999999)
        }

        Text(
            text = search.rank.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = rankColor,
            modifier = Modifier.width(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = search.text,
            fontSize = 15.sp,
            color = Color(0xFF333333)
        )
    }
}

@Composable
fun LiveItemCard(
    liveItem: SearchLiveItem,
    onLiveClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onLiveClick(liveItem.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            AsyncImage(
                model = liveItem.thumbnailUrl ,
                contentDescription = "News Thumbnail",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = liveItem.status,
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

                    Text(
                        text = liveItem.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = liveItem.artistName,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Box(
                        modifier = Modifier
                            .size(2.dp)
                            .background(Color.Gray, shape = CircleShape)
                    )

                    Text(
                        text = formatPublishedAt(liveItem.scheduledAt.toString()), // 시간 포맷팅 함수 필요
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun NewsItemCard(
    newsItem: SearchNewsItem,
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
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {

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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = newsItem.sourceName ?: "Unknown",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

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

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen()
}