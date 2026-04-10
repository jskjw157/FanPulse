package com.aos.fanpulse.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen (
    onBackClick: () -> Unit,
){

    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

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
        .verticalScroll(rememberScrollState())
    ){
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    onBackClick()
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
                SettingsItemWithSwitch(
                    icon = painterResource(id = R.drawable.icon_setting_6),
                    iconBackgroundColor = Color(0xFFF5D5E8),
                    iconTint = Color(0xFFE91E63),
                    title = "푸시 알림",
                    checked = pushNotificationsEnabled,
                    onCheckedChange = { pushNotificationsEnabled = it }
                )
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
                SettingsItemWithSwitch(
                    icon = painterResource(id = R.drawable.icon_setting_6),
                    iconBackgroundColor = Color(0xFFD5D5F5),
                    iconTint = Color(0xFF5C6BC0),
                    title = "다크 모드",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
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
        Button(
            onClick = { /* 로그아웃 처리 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEEEEEE),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("로그아웃", modifier = Modifier.padding(vertical = 8.dp))
        }
        TextButton(
            onClick = { /* 회원 탈퇴 처리 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("회원 탈퇴", color = Color.Red)
        }
    }
}

@Composable
fun SettingsItemWithSwitch(
    icon: Painter,
    iconBackgroundColor: Color,
    iconTint: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = title,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF9C27B0)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(){}
}