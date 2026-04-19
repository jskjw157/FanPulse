package com.aos.fanpulse.presentation.live

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.R
import com.aos.fanpulse.data.remote.apiservice.StreamingEventItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventSimpleItem
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import com.aos.fanpulse.presentation.tickets.InfoRow
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LiveScreen(
    viewModel: LiveViewModel = hiltViewModel(),
    goSearchScreen: () -> Unit = {},
    goNotificationScreen: () -> Unit = {},
    goLiveDetailScreen: (String) -> Unit = {},
) {

    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            LiveContract.SideEffect.NavigateHome -> {}
            is LiveContract.SideEffect.NavigateLiveDetail -> {
                goLiveDetailScreen(sideEffect.liveId)
            }
            is LiveContract.SideEffect.ShowToast -> {}
        }
    }

    Column {
        CommonTopAppBar(
            isActiveLeftTextTitle = true,
            leftTextTitle = "Live",
            isActiveRightSearch = true,
            onRightSearch = { goSearchScreen() },
            isActiveRightNotification = true,
            onRightNotification = { goNotificationScreen() },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            //  이상함 //  메인 라이브 배너
            MainLiveBanner(streamingEventSimpleItem = state.liveItem[0]){
                viewModel.goLiveDetailScreen(it)
            }

            //  More Live Streams
            Column {
                Text(
                    text = "More Live Streams",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.streamingEventItem.drop(1).forEach { item ->
                        LiveStreamListItem(streamingEventItem = item){
                            viewModel.goLiveDetailScreen(it)
                        }
                    }
                }
            }

            //  이상함 //  Upcoming Concerts
            Column {
                Text(
                    text = "Upcoming Concerts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(state.scheduledItem) { item ->
                        UpcomingConcertItem(streamingEventSimpleItem = item){
                            viewModel.goLiveDetailScreen(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainLiveBanner(
    streamingEventSimpleItem: StreamingEventSimpleItem,
    goLiveDetail: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable{
                goLiveDetail(streamingEventSimpleItem.id)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFC0CB), Color(0xFF800080)) // 분홍->보라 그라데이션
                        )
                    )
            )

            AsyncImage(
                model = streamingEventSimpleItem.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                // (선택 사항) thumbnailUrl이 null이거나 로딩에 실패했을 때 보여줄 이미지
                placeholder = painterResource(id = R.drawable.home_ex1),
                error = painterResource(id = R.drawable.home_ex1)
            )

            // 상단 뱃지들 (Live, View Count)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Badge
                Badge(backgroundColor = colorResource(R.color.color_6), text = "LIVE", showDot = true)
                // View Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(streamingEventSimpleItem.viewerCount.toString(), color = Color.White, fontSize = 12.sp)
                }
            }

            // 하단 텍스트 정보
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = streamingEventSimpleItem.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                //  이상함 내용이 들어있어야함
                Text(
                    text = streamingEventSimpleItem.platform,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun LiveStreamListItem(
    streamingEventItem: StreamingEventItem,
    goLiveDetail: (String) -> Unit,
    )
{
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
            .clickable{
                goLiveDetail(streamingEventItem.id)
            }
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 썸네일 이미지 영역
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 70.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.3f),
                    contentScale = ContentScale.Crop
                )
                // LIVE 뱃지
                Box(modifier = Modifier.padding(6.dp)) {
                    Badge(backgroundColor = colorResource(R.color.color_6), text = "LIVE", showDot = true, scale = 0.7f)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 텍스트 정보 영역
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = streamingEventItem.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = streamingEventItem.artistName,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = streamingEventItem.viewerCount.toString(),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // 이상함  //   재생 시간
            Text(
                text = streamingEventItem.scheduledAt,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}

@Composable
fun UpcomingConcertItem(
    streamingEventSimpleItem: StreamingEventSimpleItem,
    goLiveDetail: (String) -> Unit,
    ) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.width(200.dp)
            .clickable{
                goLiveDetail(streamingEventSimpleItem.id)
            }
    ) {
        Column {
            // 포스터 이미지 영역 (그라데이션 플레이스홀더)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.3f),
                    contentScale = ContentScale.Crop
                )
                // 상태 뱃지
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    //  이상함
                    when (streamingEventSimpleItem.status) {
                        "" -> Badge(backgroundColor = colorResource(R.color.color_13), text = "On Sale")
                        "" -> Badge(backgroundColor = colorResource(R.color.color_text_3), text = "Sold Out")
                    }
                }
            }

            // 공연 정보 영역
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = streamingEventSimpleItem.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(40.dp) // 두 줄 확보
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 날짜
                InfoRow(icon = Icons.Default.DateRange, text = streamingEventSimpleItem.scheduledAt)
                Spacer(modifier = Modifier.height(4.dp))
                // 이상함 // 장소
                InfoRow(icon = Icons.Default.LocationOn, text = streamingEventSimpleItem.platform)
            }
        }
    }
}

// 작은 뱃지 (Live, On Sale 등)
@Composable
fun Badge(
    backgroundColor: Color,
    text: String,
    showDot: Boolean = false,
    scale: Float = 1f
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = (8 * scale).dp, vertical = (4 * scale).dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size((6 * scale).dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width((4 * scale).dp))
        }
        Text(
            text = text,
            color = Color.White,
            fontSize = (11 * scale).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LiveScreen()
}