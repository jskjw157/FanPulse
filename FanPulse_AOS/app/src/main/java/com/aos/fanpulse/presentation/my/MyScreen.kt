package com.aos.fanpulse.presentation.my


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R
import com.aos.fanpulse.presentation.membership.RecentActivityItem

@Composable
fun MyScreen (){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .paint(
                painter = painterResource(id = R.drawable.loginscreen_bg),
                contentScale = ContentScale.Crop
            )
    ) {
        Row(
            modifier = Modifier
                .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(100.dp)),
                painter = painterResource(id = R.drawable.person_ex1),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(

                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Alex Kim",
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    modifier = Modifier.wrapContentHeight(Alignment.CenterVertically)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "@alexkim_fanpulse",
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Member since Dec 2023",
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }
        }
        Row(
            modifier = Modifier.padding(
                top = 24.dp,
                bottom = 32.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .background(
                        color = colorResource(R.color.white).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_vote),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "1,247",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "투표 참여",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .background(
                        color = colorResource(R.color.white).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_posting),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "89",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "게시물",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .background(
                        color = colorResource(R.color.white).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_follower),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "12",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "팔로워",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Column (
            Modifier
                .background(color = colorResource(R.color.white))
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 24.dp
                )
        ) {
            Column(
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.white),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false // 그림자가 잘리지 않도록 false 설정
                        )
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    Row {
                        Column (
                            modifier = Modifier.weight(1f)
                        ){
                            Text(
                                "보유 포인트",
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.sp,
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                            Text(
                                "12,450P",
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    lineHeight = 32.sp,
                                    letterSpacing = 0.sp,
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                ),
                                color = colorResource(R.color.color_1)
                            )
                        }
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.color_1),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(100.dp)) {
                            Text("포인트 적립")
                        }
                    }
                    Spacer(Modifier.height(25.dp))
                    Text(
                        "최근 포인트 내역",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.sp,
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    SetRecentList("광고 시청", true, 500)
                    SetRecentList("굿즈 구매", false, 2000)
                    SetRecentList("투표 참여", true, 1000)
                    Spacer(Modifier.height(12.dp))
                    Row (
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "전체 내역 보기 >",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.sp,
                                textAlign = TextAlign.Center,
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            ),
                            color = colorResource(R.color.color_1)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = Color.White)
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            ){
                LazyColumn {
                    items(1) { index ->
                        MyScreenItem(R.drawable.icon_settings, "설정", onClick = {

                        })
                    }
                }
            }
        }
    }
}

@Composable
fun MyScreenItem(resource: Int, title: String, onClick: () -> Unit){
    Column (
        modifier = Modifier.clickable(
            onClick = {onClick()}
        )
    ){
        Row (
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column (
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(color = colorResource(R.color.color_7))
                    .padding(6.dp)
            ){
                Icon(
                    painter = painterResource(id = resource),
                    contentDescription = null,
                    tint = colorResource(R.color.color_1)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    letterSpacing = 0.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
            Spacer(Modifier.height(2.dp))
            Icon(
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp),
                painter = painterResource(id = R.drawable.icon_right_arrow),
                contentDescription = null,
                tint = colorResource(R.color.color_text_4)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 0.dp),
            thickness = 1.dp,
            color = Color(0xFFEEEEEE)
        )
    }
}

@Composable
fun SetRecentList(title: String, plus: Boolean, value: Int){
    Row (
        verticalAlignment = Alignment.CenterVertically
    ){
        Spacer(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = if (plus) colorResource(R.color.color_13) else colorResource(R.color.color_14),
                    shape = CircleShape
                )
        )
        Spacer(Modifier.width(7.dp))
        Text(title,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
        Text(text = "${if (plus) "+" else "-"}$value",
            style = TextStyle(
                fontFamily = FontFamily.Default, // Roboto
                fontWeight = FontWeight.Medium,  // font-weight: 500
                fontSize = 14.sp,                // font-size: 14px
                lineHeight = 20.sp,              // line-height: 20px
                letterSpacing = 0.sp,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false   // leading-trim: NONE 대응
                )
            ),
            color = if(plus)colorResource(R.color.color_13) else colorResource(R.color.color_14)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyScreen()
}