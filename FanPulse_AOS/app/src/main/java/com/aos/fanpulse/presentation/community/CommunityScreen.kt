package com.aos.fanpulse.presentation.community

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(){
    Box (modifier = Modifier.fillMaxSize()) {
        Scaffold (
            containerColor = colorResource(id = R.color.white),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        Text(text = "Community")
                    },
                    actions = {
                        // 오른쪽 아이콘들 (순서대로 배치됨)
                        IconButton(onClick = { /* 검색 클릭 이벤트 */ }) {
                            Icon(painter = painterResource(id = R.drawable.icon_search), contentDescription = "검색", tint = Color.Black)
                        }
                        IconButton(onClick = { /* 알림 클릭 이벤트 */ }) {
                            Icon(painter = painterResource(id = R.drawable.icon_alarm_inactive), contentDescription = "알림", tint = Color.Black)
                        }
                    }
                )
            },
            bottomBar = {

            },

            ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(colorResource(id = R.color.white))
                    .fillMaxHeight()
            ) {

                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 12.dp
                        )
                        .height(40.dp)
                        .background(
                            color = colorResource(R.color.color_2),
                            shape = RoundedCornerShape(20.dp),
                        )
                ){
                    Spacer((Modifier.width(16.dp)))
                    Image(
                        painter = painterResource(id = R.drawable.community_ex1),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    Spacer((Modifier.width(8.dp)))
                    Text("ALL")
                    Spacer((Modifier.width(8.dp)))
                    Text("(1234 posts)")
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(painter = painterResource(id = R.drawable.icon_under_arrow), contentDescription = null, tint = Color.Unspecified)
                    Spacer((Modifier.width(16.dp)))
                }
                //      setButton
                LazyRow (
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                ){
                    items(3) { index ->
                        CommunityRadioButtonItem()
                    }
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) { index ->
                        CommunityItem()
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
fun CommunityRadioButtonItem(
) {
    Box(
        modifier = Modifier.background(
            color = colorResource(id = R.color.color_7),
            shape = RoundedCornerShape(100.dp)
        )
    ) {
        Text(
            color = colorResource(id = R.color.color_8),
            text = "Latest Posts",
            modifier = Modifier
                .padding(
                    top = 4.dp,
                    bottom = 4.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
            fontSize = 12.sp
        )
    }
}

@Composable
fun CommunityItem(){

    Column {
        Row {
            Image(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp)),
                painter = painterResource(id = R.drawable.home_ex1),
                contentDescription = null,
                contentScale = ContentScale.Crop)
            Column {
                Row {
                    Text("ARAMY_Forever")
                    Box{ Text(text = "VIP") }
                }
                Row {
                    Text("BTS")
                    Spacer((Modifier.width(8.dp)))
                    Text("- 2시간 전")
                }
            }
            Image(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp)),
                painter = painterResource(id = R.drawable.icon_list),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        Text(
            text = "BTS 새 앨범 티저 영상 보셨나요? 진짜 너무 기대돼요! \uD83D\uDC9C 컴백 준비하는 모습 보니까 벌써부터 설레네요"
        )
        Image(
            painter = painterResource(id = R.drawable.home_ex1),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp),
            contentScale = ContentScale.Crop
        )
        Row {
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
            Icon(
                painter = painterResource(id = R.drawable.icon_news),
                contentDescription = "좋아요",
                tint = Color.Unspecified
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CommunityScreen()
}