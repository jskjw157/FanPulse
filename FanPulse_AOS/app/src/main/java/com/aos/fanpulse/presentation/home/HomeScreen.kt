package com.aos.fanpulse.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen () {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.loginscreen_bg),
                contentScale = ContentScale.Crop
            )
    ) {
        Scaffold (
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        IconButton(
                            onClick = { /* 검색 클릭 이벤트 */ },
                            modifier = Modifier
                                .height(28.dp)
                                .width(81.dp)
                        ) {
                            Icon(painter = painterResource(id = R.drawable.home_title), contentDescription = "검색", tint = Color.Unspecified) }
                    },
                    actions = {
                        // 오른쪽 아이콘들 (순서대로 배치됨)
                        IconButton(onClick = { /* 검색 클릭 이벤트 */ }) {
                            Icon(painter = painterResource(id = R.drawable.icon_search), contentDescription = "검색", tint = Color.Unspecified)
                        }
                        IconButton(onClick = { /* 알림 클릭 이벤트 */ }) {
                            Icon(painter = painterResource(id = R.drawable.icon_alarm_inactive), contentDescription = "알림", tint = Color.Unspecified)
                        }
                        IconButton(onClick = { /* 설정 클릭 이벤트 */ }) {
                            Icon(painter = painterResource(id = R.drawable.icon_inventory), contentDescription = "인벤", tint = Color.Unspecified)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                //  메인 썸네일
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
                    Column (
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ){
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
                            text ="글로벌 K-POP 팬들의 인터랙티브 플랫폼",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.White,
                        )
                    }
                }

                //  최신 뉴스
                Column (
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
                    Row (
                        modifier = Modifier
                            .padding(16.dp)
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.icon_news),
                            contentDescription = "검색",
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            modifier = Modifier,
                            text ="최신 뉴스",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.Black,
                        )
                    }
                    Row (
                        modifier = Modifier
                            .padding(16.dp)
                    ){
                        Text(
                            modifier = Modifier,
                            text ="뉴스",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = colorResource(id = R.color.color_1),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            modifier = Modifier,
                            text ="BTS 새 앨범 발매 예정",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.Black,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier,
                            text ="1 시간 전",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = colorResource(id = R.color.color_text_4),
                        )
                    }
                    Row (
                        modifier = Modifier
                            .padding(16.dp)
                    ){
                        Text(
                            modifier = Modifier,
                            text ="공연",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = colorResource(id = R.color.color_1),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            modifier = Modifier,
                            text ="BLACKPINK 월드투어 추가 공연",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.Black,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier,
                            text ="1 시간 전",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = colorResource(id = R.color.color_text_4),
                        )
                    }
                }

                //  Live Now
                Column(
                    modifier = Modifier.padding(
                            start = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp
                        )
                ) {
                    Row (
                        modifier = Modifier.padding(
                            end = 16.dp)
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
                            SetLazyRowItem()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetLazyRowItem(){
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
                        painter = painterResource(id = R.drawable.icon_live),
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
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HomeScreen ()
}