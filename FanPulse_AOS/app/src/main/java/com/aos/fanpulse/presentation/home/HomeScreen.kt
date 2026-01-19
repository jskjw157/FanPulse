package com.aos.fanpulse.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

@Composable
fun HomeScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.color_12))
    ) {
        // 1. 메인 썸네일
        item {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .height(192.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_ex1),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "Welcome to FanPulse",
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        modifier = Modifier,
                        text = "글로벌 K-POP 팬들의 인터랙티브 플랫폼",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White,
                    )
                }
            }
        }

        item {
            //                //  최신 뉴스
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
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_news),
                        contentDescription = "검색",
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
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "뉴스",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = colorResource(id = R.color.color_1),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        modifier = Modifier,
                        text = "BTS 새 앨범 발매 예정",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier,
                        text = "1 시간 전",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = colorResource(id = R.color.color_text_4),
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "공연",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = colorResource(id = R.color.color_1),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        modifier = Modifier,
                        text = "BLACKPINK 월드투어 추가 공연",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier,
                        text = "1 시간 전",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = colorResource(id = R.color.color_text_4),
                    )
                }
            }
        }
        item {
//                //  Live Now
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
                        modifier = Modifier,
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
                    items(10) { index ->
                        SetLiveNowItem()
                    }
                }
            }
        }
        item {
            //  인기 게시글
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
//                    LazyColumn {
//                        items(3) { index ->
//                            SetPopularPostItem()
//                        }
//                    }
            }
        }
        item {
            //  실시간 차트
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
//                            LazyColumn(
//                                verticalArrangement = Arrangement.spacedBy(12.dp)
//                            ) {
//                                items(5) { index ->
//                                    SetRealTimeChartItem(index)
//                                }
//                            }
                    }
                }
            }
        }
        item {
            //  Best Male Group
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
                        text = "Best Male Group 2024",
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier,
                        text = "Vote Now",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = colorResource(id = R.color.color_1),
                    )
                }
                Spacer((Modifier.height(12.dp)))
                SetBestGroupItem()
//                    LazyVerticalGrid(
//                        columns = GridCells.Fixed(2), // 2열
//                        modifier = Modifier.fillMaxSize(),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp),
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        items(4) { index ->
//                            SetBestGroupItem()
//                        }
//                    }
            }
        }
        item {
            //  Upcoming Events
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
                        text = "\uD83D\uDCC5 Upcoming Events",
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier,
                        text = "See All",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = colorResource(id = R.color.color_1),
                    )
                }
                Spacer((Modifier.height(12.dp)))

                Column {
                    SetUpcomingEventsItem()
//                        LazyColumn(
//                            verticalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            items(2) { index ->
//                                SetUpcomingEventsItem()
//                            }
//                        }
                }
            }
        }
        item {
            //
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        top = 24.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    )
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clickable { }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_earn_rewards),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer((Modifier.width(12.dp)))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clickable { }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_vip_club),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer((Modifier.width(12.dp)))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clickable { }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_community),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun SetLiveNowItem(){
    Column {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .height(144.dp)
                .width(256.dp)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_ex1),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
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
                        text ="125K",
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
            text ="Music Bank Live",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = Color.White,
        )
    }
}

@Composable
fun SetUpcomingEventsItem(){
    Column (
        modifier = Modifier.background(
            color = colorResource(R.color.white),
            shape = RoundedCornerShape(12.dp)
        )
    ){
        Image(
            painter = painterResource(id = R.drawable.home_group_ex2),
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
                        text = "Award Show",
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
                    text = "2024.12.15"
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "MAMA Awards 2024"
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