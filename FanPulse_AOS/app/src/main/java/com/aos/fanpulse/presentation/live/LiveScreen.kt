package com.aos.fanpulse.presentation.live

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.presentation.tickets.InfoRow

private val LightGrayBackground = Color(0xFFF7F8FA)
private val PrimaryPurple = Color(0xFF8B5CF6)
private val LiveRed = Color(0xFFEC4899)
private val OnSaleGreen = Color(0xFF22C55E)
private val SoldOutGray = Color(0xFF6B7280)

@Composable
fun LiveScreen(

) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. 메인 라이브 배너
        MainLiveBanner()

        // 2. More Live Streams 섹션
        LiveStreamsSection()

        // 3. Upcoming Concerts 섹션
        UpcomingConcertsSection()
    }
}

// 1. 메인 라이브 배너
@Composable
fun MainLiveBanner() {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // 배경 이미지 (실제 앱에서는 Coil 같은 라이브러리 사용)
            // 여기서는 플레이스홀더 색상으로 대체
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFC0CB), Color(0xFF800080)) // 분홍->보라 그라데이션
                        )
                    )
            )

            // 이미지 소스 매핑 (실제 이미지가 있다면 painterResource 사용)
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery), // 플레이스홀더
                contentDescription = "NewJeans Live",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f) // 그라데이션 위에 겹침
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
                Badge(backgroundColor = LiveRed, text = "LIVE", showDot = true)
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
                    Text("24.5K", color = Color.White, fontSize = 12.sp)
                }
            }

            // 하단 텍스트 정보
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "NewJeans 컴백 쇼케이스",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NewJeans Official",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

// 2. More Live Streams 섹션
@Composable
fun LiveStreamsSection() {
    Column {
        SectionTitle("More Live Streams")
        Spacer(modifier = Modifier.height(16.dp))

        // 더미 데이터
        val liveItems = listOf(
            LiveStreamItemData(
                title = "BTS Fan Meeting Special",
                artist = "BTS",
                views = "89.2K",
                duration = "1:45:20",
                bgColor = Color(0xFFE0BBE4) // 보라색 톤
            ),
            LiveStreamItemData(
                title = "BLACKPINK Behind The...",
                artist = "BLACKPINK",
                views = "67.8K",
                duration = "0:58:12",
                bgColor = Color(0xFFFFC0CB) // 분홍색 톤
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            liveItems.forEach { item ->
                LiveStreamListItem(data = item)
            }
        }
    }
}

data class LiveStreamItemData(
    val title: String,
    val artist: String,
    val views: String,
    val duration: String,
    val bgColor: Color
)

@Composable
fun LiveStreamListItem(data: LiveStreamItemData) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
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
                    .background(data.bgColor)
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
                    Badge(backgroundColor = LiveRed, text = "LIVE", showDot = true, scale = 0.7f)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 텍스트 정보 영역
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data.artist,
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
                        text = data.views,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // 재생 시간
            Text(
                text = data.duration,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}

// 3. Upcoming Concerts 섹션
@Composable
fun UpcomingConcertsSection() {
    Column {
        SectionTitle("Upcoming Concerts")
        Spacer(modifier = Modifier.height(16.dp))

        // 더미 데이터
        val concertItems = listOf(
            ConcertItemData(
                title = "NewJeans 1st World Tour",
                date = "Jan 10, 2025",
                location = "Gocheok Sky Dome",
                status = ConcertStatus.ON_SALE,
                bgColor = Color(0xFFC084FC) // 연보라 그라데이션 느낌
            ),
            ConcertItemData(
                title = "TWICE Encore Concert",
                date = "Jan 18, 2025",
                location = "KSPO Dome",
                status = ConcertStatus.SOLD_OUT,
                bgColor = Color(0xFF60A5FA) // 파란색 그라데이션 느낌
            )
        )

        // 가로 스크롤 (LazyRow)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 16.dp) // 마지막 아이템 여백
        ) {
            items(concertItems) { item ->
                ConcertCard(data = item)
            }
        }
    }
}

enum class ConcertStatus { ON_SALE, SOLD_OUT }
data class ConcertItemData(
    val title: String,
    val date: String,
    val location: String,
    val status: ConcertStatus,
    val bgColor: Color
)

@Composable
fun ConcertCard(data: ConcertItemData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.width(200.dp)
    ) {
        Column {
            // 포스터 이미지 영역 (그라데이션 플레이스홀더)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(data.bgColor, data.bgColor.copy(alpha = 0.6f))
                        )
                    )
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
                    when (data.status) {
                        ConcertStatus.ON_SALE -> Badge(backgroundColor = OnSaleGreen, text = "On Sale")
                        ConcertStatus.SOLD_OUT -> Badge(backgroundColor = SoldOutGray, text = "Sold Out")
                    }
                }
            }

            // 공연 정보 영역
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = data.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(40.dp) // 두 줄 확보
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 날짜
                InfoRow(icon = Icons.Default.DateRange, text = data.date)
                Spacer(modifier = Modifier.height(4.dp))
                // 장소
                InfoRow(icon = Icons.Default.LocationOn, text = data.location)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
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