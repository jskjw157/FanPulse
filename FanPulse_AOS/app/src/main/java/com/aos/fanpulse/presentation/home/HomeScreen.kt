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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.R
import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.data.remote.apiservice.StreamingEventItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventSimpleItem
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

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
        }
    }

    var isDrawerOpen by remember { mutableStateOf(false) }

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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.color_12))
            ) {
                //  메인 썸네일  state.newsItem[0]
                item {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(192.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                    ) {
                        if (state.newsItem.isNotEmpty()) {
                            AsyncImage(
                                model = state.newsItem[0].thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                // (선택 사항) thumbnailUrl이 null이거나 로딩에 실패했을 때 보여줄 이미지
                                placeholder = painterResource(id = R.drawable.home_ex1),
                                error = painterResource(id = R.drawable.home_ex1)
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp)
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = state.newsItem[0].title,
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
                                    text = state.newsItem[0].content,
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
                    Column(
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            )
                            .background(
                                color = colorResource(R.color.white),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    viewModel.goNewsScreen()
                                }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_news),
                                contentDescription = "",
                                tint = Color.Unspecified
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                modifier = Modifier,
                                text = "최신 뉴스",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                color = Color.Black,
                            )
                        }
                        state.newsItem.drop(1).forEach { item ->
                            LatestNewsItem(item){
                                viewModel.goNewsDetailScreen(it)
                            }
                        }
                    }
                }

                //  라이브 스크린
                item {
                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                end = 16.dp
                            )
                        ) {
                            Text(
                                modifier = Modifier,
                                text = "\uD83D\uDD34 Live Now",
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
                        LazyRow {
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
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                top = 24.dp,
                                end = 16.dp
                            )
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                end = 16.dp
                            )
                        ) {
                            Text(
                                modifier = Modifier,
                                text = "\uD83D\uDD25 인기 게시글",
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                modifier = Modifier,
                                text = "더보기",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                color = colorResource(id = R.color.color_1),
                            )
                        }
                        SetPopularPostItem()
                    }
                }

                //  실시간 차트
                item {
                    Column(
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                top = 24.dp,
                                end = 16.dp
                            )
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                end = 16.dp
                            )
                        ) {
                            Text(
                                modifier = Modifier,
                                text = "\uD83D\uDCCA 실시간 차트",
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                modifier = Modifier,
                                text = "전체보기",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                color = colorResource(id = R.color.color_1),
                            )
                        }

                        Spacer((Modifier.height(12.dp)))

                        Column(
                            modifier = Modifier
                                .background(
                                    color = colorResource(R.color.white),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                SetRealTimeChartItem(1)
                            }
                        }
                    }
                }

//                //  Best Male Group
//                item {
//                    Column(
//                        modifier = Modifier
//                            .padding(
//                                start = 16.dp,
//                                top = 24.dp,
//                                end = 16.dp
//                            )
//                            .fillMaxWidth()
//                    ) {
//                        Row(
//                            modifier = Modifier.padding(
//                                end = 16.dp
//                            )
//                        ) {
//                            Text(
//                                modifier = Modifier,
//                                text = "Best Male Group 2024",
//                                textAlign = TextAlign.Center,
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Normal,
//                                fontFamily = FontFamily.SansSerif,
//                                color = Color.Black,
//                            )
//                            Spacer(modifier = Modifier.weight(1f))
//                            Text(
//                                modifier = Modifier,
//                                text = "Vote Now",
//                                textAlign = TextAlign.Center,
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                fontFamily = FontFamily.SansSerif,
//                                color = colorResource(id = R.color.color_1),
//                            )
//                        }
//                        Spacer((Modifier.height(12.dp)))
//                        SetBestGroupItem()
//                    }
//                }
//
//                //  Upcoming Events
//                item {
//                    Column(
//                        modifier = Modifier
//                            .padding(
//                                start = 16.dp,
//                                top = 24.dp,
//                                end = 16.dp
//                            )
//                            .fillMaxWidth()
//                    ) {
//                        Row(
//                            modifier = Modifier.padding(
//                                end = 16.dp
//                            )
//                        ) {
//                            Text(
//                                modifier = Modifier,
//                                text = "\uD83D\uDCC5 Upcoming Events",
//                                textAlign = TextAlign.Center,
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Normal,
//                                fontFamily = FontFamily.SansSerif,
//                                color = Color.Black,
//                            )
//                            Spacer(modifier = Modifier.weight(1f))
//                            Text(
//                                modifier = Modifier.clickable{
//                                    viewModel.goLiveScreen()
//                                },
//                                text = "See All",
//                                textAlign = TextAlign.Center,
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                fontFamily = FontFamily.SansSerif,
//                                color = colorResource(id = R.color.color_1),
//                            )
//                        }
//                        Spacer((Modifier.height(12.dp)))
//
//                        state.scheduledItem.forEach { item ->
//                            UpcomingEventsItem(item)
//                        }
//                    }
//                }

                //  기타 항목
//                item {
//                    Row(
//                        modifier = Modifier
//                            .padding(
//                                start = 16.dp,
//                                top = 24.dp,
//                                end = 16.dp,
//                                bottom = 24.dp
//                            )
//                            .fillMaxWidth()
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .aspectRatio(1f)
//                                .clickable { }
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.btn_earn_rewards),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop
//                            )
//                        }
//                        Spacer((Modifier.width(12.dp)))
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .aspectRatio(1f)
//                                .clickable { }
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.btn_vip_club),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop
//                            )
//                        }
//                        Spacer((Modifier.width(12.dp)))
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .aspectRatio(1f)
//                                .clickable { }
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.btn_community),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop
//                            )
//                        }
//                    }
//                }
            }
        }

        //  메뉴
        RightDrawer(
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
    viewModel: HomeViewModel = hiltViewModel(),
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

    val state by viewModel.collectAsState()

    // 스크림 (배경 어둡게)
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

    // Drawer 본체
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
            .clickable{
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
){
    Column (
        // 팁: Column 자체에도 Box와 동일한 수준의 너비 제한을 걸어두면,
        // 텍스트가 엄청 길어져도 이미지 너비를 뚫고 나가지 않습니다.
        modifier = Modifier.width(268.dp) // Box 너비(256) + 양옆 패딩(6+6)
            .clickable{
                goLiveDetail(streamingEventItem.id)
            }
    ){
        Box(
            modifier = Modifier
                .padding(6.dp)
                .height(144.dp)
                .width(256.dp)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            AsyncImage(
                model = streamingEventItem.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                // (선택 사항) thumbnailUrl이 null이거나 로딩에 실패했을 때 보여줄 이미지
                placeholder = painterResource(id = R.drawable.home_ex1),
                error = painterResource(id = R.drawable.home_ex1)
            )
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ){
                Row (
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.color_5),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.icon_circle),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "LIVE",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White,
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))
                Row (
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(
                            color = colorResource(R.color.black),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.icon_watch),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        modifier = Modifier,
                        text = streamingEventItem.viewerCount.toString(),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White,
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
        Text(
            modifier = Modifier,
            text = streamingEventItem.title,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = Color.White,
        )
    }
}

@Composable
fun UpcomingEventsItem(
    streamingEventSimpleItem : StreamingEventSimpleItem
){
    Column (
        modifier = Modifier.background(
            color = colorResource(R.color.white),
            shape = RoundedCornerShape(12.dp)
        )
    ){
        AsyncImage(
            model = streamingEventSimpleItem.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
                .height(176.dp)
                .clip(RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                ),
            contentScale = ContentScale.Crop,
            // (선택 사항) thumbnailUrl이 null이거나 로딩에 실패했을 때 보여줄 이미지
            placeholder = painterResource(id = R.drawable.home_ex1),
            error = painterResource(id = R.drawable.home_ex1)
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Column (
                    modifier = Modifier.background(
                        color = colorResource(id = R.color.color_7),
                        shape = RoundedCornerShape(100.dp)
                    )
                ) {
                    Text(
                        color = colorResource(id = R.color.color_8),
                        text = streamingEventSimpleItem.platform,
                        modifier = Modifier
                            .padding(
                                top = 4.dp,
                                bottom = 4.dp,
                                start = 8.dp,
                                end = 8.dp),
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    color = colorResource(id = R.color.color_text_3),
                    text = streamingEventSimpleItem.scheduledAt
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = streamingEventSimpleItem.title
            )

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {},
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, colorResource(id = R.color.color_1), RoundedCornerShape(100.dp))
                    .height(40.dp)
            ) {
                Text( text = "Get Tickets" )
            }
        }
    }
}

@Composable
fun SetPopularPostItem(){
    Spacer((Modifier.height(12.dp)))

    Row (
        modifier = Modifier.background(
            color = colorResource(R.color.white),
            shape = RoundedCornerShape(12.dp)
        )
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_ex1),
                contentDescription = null,
                modifier = Modifier
                    .width(96.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer((Modifier.width(12.dp)))
            Column {
                Text(
                    text = "BTS 콘서트 후기 - 정말 최고였어요!"
                )
                Spacer((Modifier.height(8.dp)))
                Row {
                    Text(
                        text = "4ever"
                    )
                    Spacer(Modifier.width(12.dp))
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_news),
                            contentDescription = "좋아요",
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "2,222"
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_news),
                            contentDescription = "댓글",
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "1,111"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetRealTimeChartItem(ind: Int){

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer((Modifier.width(9.dp)))
        Text(
            text = "$ind"
        )
        Spacer((Modifier.width(22.dp)))
        Image(
            painter = painterResource(id = R.drawable.home_ex1),
            contentDescription = null,
            modifier = Modifier
                .width(48.dp)
                .height(48.dp),
            contentScale = ContentScale.Crop
        )
        Spacer((Modifier.width(12.dp)))
        Column {
            Text("musicTitle")
            Text("musicArtist")
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("↑↓-")
        Spacer((Modifier.width(12.dp)))
        Text("변동 순위")
    }
}

@Composable
fun SetBestGroupItem(){
    Column (
        modifier = Modifier.background(
            color = colorResource(R.color.white),
            shape = RoundedCornerShape(12.dp)
        )
    ){
        Image(
            painter = painterResource(id = R.drawable.home_group_ex1),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                ),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "BTS"
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Image(
                    painter = painterResource(id = R.drawable.icon_person),
                    contentDescription = null,
                    modifier = Modifier
                        .width(16.dp)
                        .height(16.dp),
                    contentScale = ContentScale.Crop
                )
                Text(text = "BTS")
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.drawable.icon_heart),
                    contentDescription = null,
                    modifier = Modifier
                        .width(16.dp)
                        .height(16.dp),
                    contentScale = ContentScale.Crop
                )
                Text("850K")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {},
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .height(36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(R.drawable.loginscreen_bg),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Vote"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HomeScreen ()
}