package com.aos.fanpulse.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.presentation.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    setBackground: Int = 0,
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
    isActiveSearchFunction: Boolean = false,
    onSearchFunction:(String) -> Unit = {},
    isActiveRightSearch: Boolean = false,       //  오른쪽 검색
    onRightSearch:() -> Unit = {},
    isActiveRightNotification: Boolean = false, //  오른쪽 알림
    onRightNotification:() -> Unit = {},
    isActiveRightMenu: Boolean = false,         //  오른쪽 메뉴
    onRightMenu:() -> Unit = {},
){

    var searchText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = when (setBackground) {
                    0 -> {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF9333EA), // 왼쪽 시작 색상
                                Color(0xFFDB2777)  // 오른쪽 끝 색상
                            )
                        )
                    }

                    1 -> {
                        SolidColor(Color.Transparent)
                    }

                    else -> {
                        SolidColor(Color.White)
                    }
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
                                tint = if (setBackground == 0) Color.Unspecified else colorResource(R.color.color_new_1)
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
                if (isActiveSearchFunction){
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp),
                        placeholder = {
                            Text(text = "검색어를 입력하세요", color = Color(0xFF999999), fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_search),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp).clickable{
                                    onSearchFunction.invoke(searchText)
                                }
                            )
                        },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_close),
                                        contentDescription = "지우기",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
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