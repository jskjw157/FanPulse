package com.aos.fanpulse.presentation.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R
import java.nio.file.WatchEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen (
    onCancel: () -> Unit,
){
    val robotoBold16 = TextStyle(
        fontFamily = FontFamily.SansSerif, // Roboto 기본 적용
        fontWeight = FontWeight.W700,      // Bold
        fontSize = 16.sp,
        lineHeight = 24.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false     // 불필요한 폰트 위아래 패딩 제거
        ),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
    Column (modifier = Modifier
        .fillMaxSize()
        .background(color = colorResource(R.color.white))
    ){
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    onCancel()
                }) {
                    Icon(painter = painterResource(id = R.drawable.icon_left_arrow), contentDescription = null, tint = Color.Black)
                }
            },
            title = {
                Text(
                    "설정",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = robotoBold16,
                    color = Color.Black
                )
            },
            actions = {}
        )
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Text("계정")
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = colorResource(R.color.color_4),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
            ){
                Row (
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Image(modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(100.dp)),
                        painter = painterResource(id = R.drawable.icon_setting_1),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Text("프로필 수정")
                    Spacer(Modifier.weight(1f))
                    Image(modifier = Modifier
                        .width(14.dp)
                        .height(14.dp),
                        painter = painterResource(id = R.drawable.icon_right_arrow),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                }
                Row (
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Image(modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(100.dp)),
                        painter = painterResource(id = R.drawable.icon_setting_2),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Text("비밀번호 변경")
                    Spacer(Modifier.weight(1f))
                    Image(modifier = Modifier
                        .width(14.dp)
                        .height(14.dp),
                        painter = painterResource(id = R.drawable.icon_right_arrow),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                }
                Row (
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Image(modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(100.dp)),
                        painter = painterResource(id = R.drawable.icon_setting_3),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Text("개인정보 보호")
                    Spacer(Modifier.weight(1f))
                    Image(modifier = Modifier
                        .width(14.dp)
                        .height(14.dp),
                        painter = painterResource(id = R.drawable.icon_right_arrow),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                }


            }
        }
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Text("알림")
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = colorResource(R.color.color_4),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
            ){
                Row (
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Image(modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(100.dp)),
                        painter = painterResource(id = R.drawable.icon_setting_5),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Text("알림 설정")
                    Spacer(Modifier.weight(1f))
                    Image(modifier = Modifier
                        .width(14.dp)
                        .height(14.dp),
                        painter = painterResource(id = R.drawable.icon_right_arrow),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                }
            }
        }
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Text("화면")
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = colorResource(R.color.color_4),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
            ){
                Row (
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Image(modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(100.dp)),
                        painter = painterResource(id = R.drawable.icon_setting_7),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Text("언어")
                    Spacer(Modifier.weight(1f))
                    Image(modifier = Modifier
                        .width(14.dp)
                        .height(14.dp),
                        painter = painterResource(id = R.drawable.icon_right_arrow),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                }
            }
        }
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Text("지원")
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = colorResource(R.color.color_4),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
            ){
                Row (
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Image(modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(100.dp)),
                        painter = painterResource(id = R.drawable.icon_setting_8),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Text("도움말")
                    Spacer(Modifier.weight(1f))
                    Image(modifier = Modifier
                        .width(14.dp)
                        .height(14.dp),
                        painter = painterResource(id = R.drawable.icon_right_arrow),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                }
                Row (
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Image(modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(100.dp)),
                        painter = painterResource(id = R.drawable.icon_setting_9),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Text("고객센터")
                    Spacer(Modifier.weight(1f))
                    Image(modifier = Modifier
                        .width(14.dp)
                        .height(14.dp),
                        painter = painterResource(id = R.drawable.icon_right_arrow),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                }
                Row (
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Image(modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(100.dp)),
                        painter = painterResource(id = R.drawable.icon_setting_10),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Text("앱 정보")
                    Spacer(Modifier.weight(1f))
                    Image(modifier = Modifier
                        .width(14.dp)
                        .height(14.dp),
                        painter = painterResource(id = R.drawable.icon_right_arrow),
                        contentDescription = null,
                        contentScale = ContentScale.Crop)
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(){}
}