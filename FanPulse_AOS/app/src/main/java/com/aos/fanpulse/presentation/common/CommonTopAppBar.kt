package com.aos.fanpulse.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    setTransparentBackground: Boolean = false,
    isActiveLeftTextTitle: Boolean = false,     //  왼쪽 제목
    leftTextTitle: String? = null,
    onLeftTextTitle:() -> Unit = {},
    isActiveLeftImage: Boolean = false,         //  왼쪽 제목 이미지
    leftImage: Int? = null,
    isActiveLeftBack: Boolean = false,          //  왼쪽 뒤로가기
    onLeftBack:() -> Unit = {},
    isActiveCenterTextTitle: Boolean = false,   //  중앙 제목
    centerTextTitle: String? = null,
    isActiveRightClose: Boolean = false,        //  오른쪽 X
    onRightClose:() -> Unit = {},
    isActiveRightWrite: Boolean = false,        //  오른쪽 게시
    onRightWrite:() -> Unit = {},
    isActiveRightSetting: Boolean = false,      //  오른쪽 설정
    onRightSetting:() -> Unit = {},
    isActiveRightRefresh: Boolean = false,      //  오른쪽 갱신
    onRightRefresh:() -> Unit = {},
    isActiveRightShare: Boolean = false,        //  오른쪽 공유
    onRightShare:() -> Unit = {},
    isActiveRightBookmark: Boolean = false,     //  오른쪽 북마크
    onRightBookmark:() -> Unit = {},
    isActiveRightSearch: Boolean = false,       //  오른쪽 검색
    onRightSearch:() -> Unit = {},
    isActiveRightNotification: Boolean = false, //  오른쪽 알림
    onRightNotification:() -> Unit = {},
    isActiveRightMenu: Boolean = false,         //  오른쪽 메뉴
    onRightMenu:() -> Unit = {}
){

    Box(
        modifier = Modifier.fillMaxWidth()
            .background(
                brush = if (setTransparentBackground) {
                    SolidColor(Color.Transparent)
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF9333EA), // 왼쪽 시작 색상
                            Color(0xFFDB2777)  // 오른쪽 끝 색상
                        )
                    )
                }
            )
    ) {

        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            //  왼쪽
            navigationIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActiveLeftBack){
                        IconButton(
                            onClick = { onLeftBack() },
                            modifier = Modifier
                                .height(28.dp)
                                .wrapContentWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_left_arrow),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                    }
                    if (isActiveLeftImage){
                        Spacer(Modifier.width(16.dp))
                        IconButton(
                            onClick = { onLeftTextTitle() },
                            modifier = Modifier
                                .height(28.dp)
                                .width(81.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.home_title),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                    }
                    if (isActiveLeftTextTitle){
                        TextButton(
                            onClick = { onLeftTextTitle() },
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                        ) {
                            Text(
                                text = leftTextTitle.toString(),
                                fontSize = 18.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            //  중앙
            title = {
                if (isActiveCenterTextTitle){
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = centerTextTitle.toString(),
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.clickable { /* 클릭 */ }
                        )
                    }
                }
            },
            //  오른쪽
            actions = {
                if (isActiveRightClose){
                    IconButton(onClick = { onRightClose() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_close),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
                if (isActiveRightWrite){
                    IconButton(onClick = { onRightWrite() }) {
                        Text(
                            text = "게시",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
                if (isActiveRightSetting){
                    IconButton(onClick = { onRightSetting() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_settings),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
                if (isActiveRightRefresh){
                    IconButton(onClick = { onRightRefresh() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_refresh),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
                if (isActiveRightShare){
                    IconButton(onClick = { onRightShare() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_share),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
                if (isActiveRightSearch){
                    IconButton(onClick = { onRightSearch() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_search),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
                if (isActiveRightNotification){
                    IconButton(onClick = { onRightNotification() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_alarm_inactive),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
                if (isActiveRightMenu){
                    IconButton(
                        onClick = { onRightMenu() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_inventory),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CommonTopAppBar()
}