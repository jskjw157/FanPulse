package com.aos.fanpulse.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.domain.model.ChartTrack
import com.aos.fanpulse.domain.model.NewsDetail
import com.aos.fanpulse.domain.model.Post
import com.aos.fanpulse.domain.model.StreamingEventItem
import com.aos.fanpulse.domain.model.StreamingEventSimpleItem
import com.aos.fanpulse.presentation.R
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    goSearchScreen: () -> Unit = {},
    goNotificationScreen: () -> Unit = {},
    goArtistScreen: () -> Unit = {},
    goChartScreen: () -> Unit = {},
    goNewsScreen: () -> Unit = {},
    goNewsDetailScreen: (String) -> Unit = {},
    goConcertScreen: () -> Unit = {},
    goCommunityScreen:() -> Unit = {},
    goCommunityDetailScreen:(String) -> Unit = {},
    goTicketsScreen: () -> Unit = {},
    goMembershipScreen: () -> Unit = {},
    goAdsScreen: () -> Unit = {},
    goFavoritesScreen: () -> Unit = {},
    goSavedScreen: () -> Unit = {},
    goSettingsScreen: () -> Unit = {},
    goSupportScreen: () -> Unit = {},
    goLiveScreen:() -> Unit = {},
    goLiveDetailScreen:(String) -> Unit = {},
) {

    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            HomeContract.SideEffect.NavigateAds -> {
                goAdsScreen()
            }
            HomeContract.SideEffect.NavigateArtist -> goArtistScreen()
            HomeContract.SideEffect.NavigateChart -> goChartScreen()
            HomeContract.SideEffect.NavigateConcert -> goConcertScreen()
            HomeContract.SideEffect.NavigateFavorites -> goFavoritesScreen()
            HomeContract.SideEffect.NavigateLive -> {
                goLiveScreen()
            }
            is HomeContract.SideEffect.NavigateLiveDetail -> {
                goLiveDetailScreen(sideEffect.liveId)
            }
            HomeContract.SideEffect.NavigateNews -> goNewsScreen()
            is HomeContract.SideEffect.NavigateNewsDetail -> {
                goNewsDetailScreen(sideEffect.newsId)
            }
            HomeContract.SideEffect.NavigateNotification -> goNotificationScreen()
            HomeContract.SideEffect.NavigateMembership -> goMembershipScreen()
            HomeContract.SideEffect.NavigateSaved -> goSavedScreen()
            HomeContract.SideEffect.NavigateSearch -> goSearchScreen()
            HomeContract.SideEffect.NavigateSettings -> goSettingsScreen()
            HomeContract.SideEffect.NavigateSupport -> goSupportScreen()
            HomeContract.SideEffect.NavigateTickets -> goTicketsScreen()
            is HomeContract.SideEffect.ShowToast -> {

            }
            HomeContract.SideEffect.NavigateCommunity -> {
                goCommunityScreen()
            }
            is HomeContract.SideEffect.NavigateCommunityDetail -> {
                goCommunityDetailScreen(sideEffect.postId)
            }
        }
    }

    var isDrawerOpen by remember { mutableStateOf(false) }

    //  Pull-to-refresh
//    val pullToRefreshState = rememberPullToRefreshState()   //  특별한 커스텀 애니메이션이 필요할때

    Box (modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            CommonTopAppBar(
                isActiveLeftImage = true,
                isActiveRightSearch = true,
                onRightSearch = { viewModel.goSearchScreen() },
                isActiveRightNotification = true,
                onRightNotification = {  viewModel.goNotificationScreen() },
                isActiveRightMenu = true,
                onRightMenu = { isDrawerOpen = true }
            )

            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.getHomeItems() },
//                state = pullToRefreshState,   //  특별한 커스텀 애니메이션이 필요할때
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(id = R.color.color_12)),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .height(192.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                        ) {
                            if (state.newsItem.isNotEmpty()) {
                                AsyncImage(
                                    model = state.newsItem.firstOrNull()?.thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.fanpulse_placeholder),
                                    error = painterResource(id = R.drawable.fanpulse_placeholder)
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        modifier = Modifier,
                                        text = state.newsItem.firstOrNull()?.title ?: "",
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color.White,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        modifier = Modifier,
                                        text = state.newsItem.firstOrNull()?.content ?: "",
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }

                    //  최신 뉴스
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.goNewsScreen() }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_news),
                                        contentDescription = "뉴스 아이콘",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Unspecified
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        text = "최신 뉴스",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color.LightGray.copy(alpha = 0.5f),
                                    thickness = 1.dp
                                )

                                Column(
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    state.newsItem.drop(1).forEach { item ->
                                        LatestNewsItem(item) {
                                            viewModel.goNewsDetailScreen(it)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //  라이브 스크린
                    item {
                        Column(
                            modifier = Modifier
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.padding(
                                    end = 16.dp
                                )
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "VOD",
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color.Black,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    modifier = Modifier.clickable{
                                        viewModel.goLiveScreen()
                                    },
                                    text = "View All",
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                    color = colorResource(id = R.color.color_1),
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(state.streamingEventItem) { item ->
                                    LiveNowItem(item){
                                        viewModel.goLiveDetailScreen(it)
                                    }
                                }
                            }
                        }
                    }

                    //  인기 게시글
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.padding(
                                    end = 16.dp
                                )
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "인기 게시글",
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color.Black,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    modifier = Modifier.clickable{
                                        viewModel.goCommunityScreen()
                                    },
                                    text = "더보기",
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                    color = colorResource(id = R.color.color_1),
                                )
                            }
                        }
                    }
                    itemsIndexed(state.posts) { index, item ->
                        if (index < 3) SetPopularPostItem(post = item){
                            viewModel.goCommunityDetailScreen(item.id)
                        }
                    }

                    //  실시간 차트
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.padding(
                                    end = 16.dp
                                )
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "실시간 차트",
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color.Black,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    modifier = Modifier.clickable{
                                        viewModel.goChartScreen()
                                    },
                                    text = "전체보기",
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                    color = colorResource(id = R.color.color_1),
                                )
                            }
                            Spacer((Modifier.height(12.dp)))
                        }
                    }

                    itemsIndexed(state.chartTracks) { index, item ->
                        SetRealTimeChartItem(
                            rank = index + 1,
                            item = item
                        )
                    }
                }
            }
        }

        //  메뉴
        RightDrawer(
            viewModel = viewModel,
            isOpen = isDrawerOpen,
            onDismiss = { isDrawerOpen = false },
            onMenuItemClick = { menuItem ->
                isDrawerOpen = false
                when(menuItem){
                    "artist" -> {
                        viewModel.goArtistScreen()
                    }
                    "chart" -> {
                        viewModel.goChartScreen()
                    }
                    "news" -> {
                        viewModel.goNewsScreen()
                    }
                    "concert" -> {
                        viewModel.goConcertScreen()
                    }
                    "tickets" -> {
                        viewModel.goTicketsScreen()
                    }
                    "membership" -> {
                        viewModel.goMembershipScreen()
                    }
                    "ads" -> {
                        viewModel.goAdsScreen()
                    }
                    "favorites" -> {
                        viewModel.goFavoritesScreen()
                    }
                    "saved" -> {
                        viewModel.goSavedScreen()
                    }
                    "settings" -> {
                        viewModel.goSettingsScreen()
                    }
                    "customer_service" -> {
                        viewModel.goSupportScreen()
                    }
                }
            }
        )
    }
}

@Composable
fun RightDrawer(
    viewModel: HomeViewModel,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onMenuItemClick: (String) -> Unit
) {
    val density = LocalDensity.current
    val drawerWidth = 280.dp
    val drawerWidthPx = with(density) { drawerWidth.toPx() }

    val offsetX by animateFloatAsState(
        targetValue = if (isOpen) 0f else drawerWidthPx,
        animationSpec = tween(durationMillis = 300),
        label = "drawerOffset"
    )

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offsetX
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .width(drawerWidth)
                .fillMaxHeight(),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                CommonTopAppBar(
                    isActiveLeftTextTitle = true,
                    leftTextTitle = "메뉴",
                    isActiveRightClose = true,
                    onRightClose = { onDismiss() },
                )

                Divider(color = Color.LightGray.copy(alpha = 0.3f))

                // Menu Items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    viewModel.setDrawerMenuItems().forEach { item ->
                        DrawerMenuItem(
                            iconRes = item.iconRes,
                            text = item.text,
                            onClick = { onMenuItemClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    iconRes: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun LatestNewsItem(
    newsDetail :NewsDetail,
    goNewsDetail : (String) -> Unit
){
    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                goNewsDetail(newsDetail.id)
            }
    ) {
        Text(
            modifier = Modifier,
            text = newsDetail.category,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = colorResource(id = R.color.color_1),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            modifier = Modifier,
            text = newsDetail.title,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier,
            text = newsDetail.publishedAt,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = colorResource(id = R.color.color_text_4),
        )
    }
}

@Composable
fun LiveNowItem(
    streamingEventItem: StreamingEventItem,
    goLiveDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { goLiveDetail(streamingEventItem.id) }
            .padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = streamingEventItem.thumbnailUrl,
                contentDescription = "라이브 썸네일",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.fanpulse_placeholder),
                error = painterResource(id = R.drawable.fanpulse_placeholder)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_watch),
                    contentDescription = "시청자 수",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = streamingEventItem.viewerCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = streamingEventItem.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun SetPopularPostItem(
    post: Post,
    goCommunityDetailScreen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .clickable { goCommunityDetailScreen() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (post.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = post.imageUrls[0],
                    contentDescription = "썸네일",
                    placeholder = painterResource(id = R.drawable.fanpulse_placeholder),
                    error = painterResource(id = R.drawable.fanpulse_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = post.content,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.author.nickname,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = if (post.isLikedByMe) painterResource(id = R.drawable.icon_heart_ena_pre) else painterResource(id = R.drawable.icon_heart_ena_nor),
                            contentDescription = "좋아요",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = post.likeCount.toString(),
                            color = Color.Gray
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // 댓글 영역
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_chat_ena),
                            contentDescription = "댓글",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = post.commentCount.toString(),
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetRealTimeChartItem(
    rank: Int,
    item: ChartTrack,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.width(16.dp))

        AsyncImage(
            model = item.imageUrl,
            contentDescription = "앨범 커버 이미지",
            placeholder = painterResource(id = R.drawable.fanpulse_placeholder),
            error = painterResource(id = R.drawable.fanpulse_placeholder),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp) // 정사각형 크기
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = item.artistName,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HomeScreen ()
}