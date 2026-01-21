package com.aos.fanpulse.presentation.membership

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
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

@Composable
fun MembershipScreen (
){
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
        ){
            Image(
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(100.dp)),
                painter = painterResource(id = R.drawable.person_ex1),
                contentDescription = null,
                contentScale = ContentScale.Crop)
            Spacer(Modifier.width(16.dp))
            Column (

                verticalArrangement = Arrangement.Center
            ){
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
        Row (
            modifier = Modifier.padding(
                top = 24.dp,
                bottom = 32.dp,
                start = 16.dp,
                end = 16.dp
            )
        ){
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

            ){
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
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
                        text = "Total\nVotes",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            Column (
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .background(
                        color = colorResource(R.color.white).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ){
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.icon_post),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "89",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Posts",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            Column (
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .background(
                        color = colorResource(R.color.white).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.icon_point),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "2,450",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Points",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            Column (
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .background(
                        color = colorResource(R.color.white).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.icon_follow),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "12",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "Following",
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
        ){
            Column (
                modifier = Modifier
            ){
                Column (
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.color_10),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .fillMaxWidth()
                        .padding(20.dp),
                ){
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Column (
                            modifier = Modifier
                                .padding(9.dp)
                                .background(
                                    color = colorResource(R.color.white).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(100.dp)
                                )
                        ){
                            Icon(
                                painter = painterResource(id = R.drawable.icon_vip),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column (
                            verticalArrangement = Arrangement.Center
                        ){
                            Text(
                                "Upgrade to VIP",
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp,
                                    letterSpacing = 0.sp,
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                ),
                                color = colorResource(R.color.white)
                            )
                            Text(
                                "Unlock exclusive benefits",
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.sp,
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                ),
                                color = colorResource(R.color.white)
                            )
                        }

                        Spacer(Modifier.width(16.dp))
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = colorResource(R.color.color_text_6)
                        ),
                        shape = RoundedCornerShape(100.dp)) {
                        Text("See Plans")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "VIP Benefits",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    letterSpacing = 0.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
            )
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2열
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(2) { index ->
                    VipBenefitItem()
                }
            }
            //
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Choose Your Plan",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    letterSpacing = 0.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn {

                items(1) { index ->
                    ChooseYourPlanItem()
                }
            }
            //
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Recent Activity",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    letterSpacing = 0.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
            )
            Spacer(Modifier.height(12.dp))
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = Color.White)
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            ){
                LazyColumn {
                    items(1) { index ->
                        RecentActivityItem()
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivityItem(){
    Column {
        Row (
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ){
            Column (
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(color = colorResource(R.color.color_7))
                    .padding(11.dp)
            ){
                Icon(
                    painter = painterResource(id = R.drawable.icon_vote),
                    contentDescription = null,
                    tint = colorResource(R.color.color_1)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Voted for BTS",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Best Male Group 2024",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 0.dp),
            thickness = 1.dp,
            color = Color(0xFFEEEEEE)
        )
    }
}

@Composable
fun ChooseYourPlanItem(){
    var isSelected by remember { mutableStateOf(false) }
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.White)
            .border(
                width = 1.dp,
                color = if (isSelected) colorResource(R.color.color_1) else colorResource(R.color.white),
                shape = RoundedCornerShape(16.dp)
            )
    ){
        Column (
            modifier = Modifier.padding(16.dp)
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically

            ){
                Column (
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                ){
                    Text(
                        text = "Monthly",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            letterSpacing = 0.sp,
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "month",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.sp,
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
                Column (
                    horizontalAlignment = Alignment.End
                ){
                    Text(
                        text = "$9.99",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            letterSpacing = 0.sp,
                            textAlign = TextAlign.End,
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.color_1),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(text = "Subscribe")
                    }
                }
            }
        }
    }
}

@Composable
fun VipBenefitItem(){
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.White)
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
    ){
        Column (
            modifier = Modifier.padding(16.dp)
        ){
            Column(
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.color_7),
                        shape = RoundedCornerShape(100.dp)
                    )

            ){
                Column (
                    modifier = Modifier.padding(10.dp)
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.icon_myscreen_ex1),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Priority Support",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Fast response from our team",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MembershipPreview() {
    MembershipScreen()
}