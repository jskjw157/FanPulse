package com.aos.fanpulse.presentation.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aos.fanpulse.presentation.R
import com.aos.fanpulse.presentation.common.CommonTopAppBar

@Composable
fun SettingsScreen (
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    goSupportScreen: () -> Unit = {},
){

    val pushNotificationsEnabled by viewModel.isNotificationEnabled.collectAsState()
    var darkModeEnabled by remember { mutableStateOf(false) }

    Column (modifier = Modifier
        .fillMaxSize()
        .background(color = colorResource(R.color.white))
        .verticalScroll(rememberScrollState())
    ){
        CommonTopAppBar(
            isActiveLeftBack = true,
            onLeftBack = { onBackClick() },
            isActiveCenterTextTitle = true,
            centerTextTitle = "설정"

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
                SettingsItemWithSwitch(
                    image = R.drawable.icon_setting_4,
                    title = "푸시 알림",
                    checked = pushNotificationsEnabled,
                    onCheckedChange = { isChecked ->
                        viewModel.toggleNotification(isChecked)
                    }
                )
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
                SettingsItemWithSwitch(
                    image = R.drawable.icon_setting_6,
                    title = "다크 모드",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
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
                    modifier = Modifier.padding(16.dp)
                        .clickable{ goSupportScreen() },
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
    image: Int,
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
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
            modifier = Modifier.size(40.dp), // 배경보다 작게 설정해서 여백 확보
        )

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