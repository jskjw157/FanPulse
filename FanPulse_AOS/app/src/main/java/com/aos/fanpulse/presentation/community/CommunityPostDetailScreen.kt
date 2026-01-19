package com.aos.fanpulse.presentation.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostDetailScreen (
    onCancel: () -> Unit,
    onPost: () -> Unit
){
    Column {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {

                }) {
                    Icon(painter = painterResource(id = R.drawable.icon_left_arrow), contentDescription = null, tint = Color.Black)
                }
            },
            title = {
                Text(
                    "게시글",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            actions = {
                IconButton(onClick = {

                }) {
                    Icon(painter = painterResource(id = R.drawable.icon_list), contentDescription = null, tint = Color.Black)
                }
            }
        )
        Column (
            modifier = Modifier.padding(16.dp),
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    painter = painterResource(id = R.drawable.home_ex1),
                    contentDescription = null,
                    contentScale = ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
                Column (
                    modifier = Modifier.weight(1f)
                ){
                    Row {
                        Text("ARAMY_Forever")
                        Spacer(Modifier.width(8.dp))
                        Box{ Text(text = "VIP") }
                    }
                    Text("2시간전")
                }
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .border(
                            width = 2.dp,
                            color = colorResource(R.color.color_1),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                ) {
                    Text(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        text = "팔로우",
                        color = colorResource(R.color.color_1)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("BTS 새 앨범 티저 영상 보셨나요? 진짜 너무 기대돼요! \uD83D\uDC9C 컴백 준비하는 모습 보니까 벌써부터 설레네요. 이번 앨범도 대박날 것 같아요!")
            Spacer(Modifier.height(12.dp))
            //  토큰 틀 설정
            Image(
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .height(256.dp),
                painter = painterResource(id = R.drawable.home_ex1),
                contentDescription = null,
            )
            Spacer(Modifier.height(16.dp))
            //  좋아요, 공유
            //  토큰
            Column(
                modifier = Modifier.background(colorResource(R.color.color_text_2))
            ) {
                Column {
                    Text(
                        text = "댓글",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.W700,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            ),
                        )
                    Spacer(Modifier.height(16.dp))
                    LazyColumn (
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ){
                        items(5) { index ->
//                            SetCommentItem()
                        }
                    }
                }
            }
        }
        MessageInputBar()
    }
}

@Composable
fun SetCommentItem(){
    Row (
        modifier = Modifier.background(color = Color.White, shape = RoundedCornerShape(16.dp))
    ){
        Row (
            modifier = Modifier.padding(12.dp)
        ){
            Image(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(100.dp)),
                painter = painterResource(id = R.drawable.home_ex1),
                contentDescription = null,
                contentScale = ContentScale.Crop)
            Spacer(Modifier.width(12.dp))
            Column {
                Row {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "Blink_Girl"
                    )
                    Text(text = "1시간 전")
                }
                Spacer(Modifier.height(4.dp))
                Text(text = "저도 티저 보고 소름 돋았어요! 이번 컨셉 진짜 좋은 것 같아요")
                Spacer(Modifier.height(8.dp))
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.icon_heart),
                        contentDescription = null,
                        modifier = Modifier
                            .width(16.dp)
                            .height(16.dp),
                        contentScale = ContentScale.Crop
                    )
                    Text(text = "23")
                    Text(text = "답글")
                }
            }
        }
    }
}

@Composable
fun MessageInputBar() {
    var textState by remember { mutableStateOf("") }

    Surface(
        tonalElevation = 8.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                placeholder = { Text("메세지를 입력하세요...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colorResource(R.color.color_2),
                    unfocusedContainerColor = colorResource(R.color.color_2),
                    focusedBorderColor = colorResource(R.color.color_2),
                    unfocusedBorderColor = colorResource(R.color.color_2),
                )
            )

            IconButton(
                onClick = {
                    textState = ""
                },
                enabled = textState.isNotBlank()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_send_message),
                    contentDescription = "전송",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityPostDetailPreview() {
    CommunityPostDetailScreen(
        onCancel = {},
        onPost = {}
    )
}