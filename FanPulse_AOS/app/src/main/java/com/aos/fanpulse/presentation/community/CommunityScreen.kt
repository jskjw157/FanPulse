package com.aos.fanpulse.presentation.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    goPostScreen: () -> Unit,
    goPostDetailScreen: () -> Unit
){
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box (modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.white))
                .fillMaxHeight()
        ) {
            Row(
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
            ) {
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
                Icon(
                    modifier = Modifier.clickable{
                        showBottomSheet = true
                    },
                    painter = painterResource(id = R.drawable.icon_under_arrow),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer((Modifier.width(16.dp)))
            }
            //      setButton
            LazyRow(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(3) { index ->
                    CommunityRadioButtonItem()
                }
            }
            LazyColumn(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { index ->
                    CommunityItem(){
                        //  게시물에 대한 정보가 필요
                        goPostDetailScreen
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                goPostScreen()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            containerColor = colorResource(R.color.color_1),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }, // 밖을 누르거나 아래로 밀면 닫힘
                sheetState = sheetState,
                containerColor = Color.White, // 시트 배경색
                dragHandle = { BottomSheetDefaults.DragHandle() } // 상단 핸들 표시
            ) {
                // 이 안에 팝업에 들어갈 내용을 작성하세요
                BottomSheetContent()
            }
        }
    }
}

@Composable
fun BottomSheetContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp) // 하단 여백 확보
    ) {
        Text(
            text = "새 게시글 작성",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 메뉴 아이템들
        Row(modifier = Modifier.padding(vertical = 12.dp)) {
            Icon(painterResource(id = R.drawable.icon_artist), contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("글쓰기")
        }
        Row(modifier = Modifier.padding(vertical = 12.dp)) {
            Icon(painterResource(id = R.drawable.icon_artist), contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("공유하기")
        }
    }
}

@Composable
fun CommunityRadioButtonItem(
) {
    var isSelected by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .background(
                // 2. 상태에 따른 배경색 분기
                color = if (isSelected) colorResource(id = R.color.color_1) else colorResource(id = R.color.color_2),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable {
                isSelected = !isSelected
            }
    ) {
        Text(
            // 4. 상태에 따른 글자색 분기
            color = if (isSelected) Color.White else Color.Black,
            text = "Latest Posts",
            modifier = Modifier
                .padding(
                    top = 4.dp,
                    bottom = 4.dp,
                    start = 12.dp, // 디자인상 좌우 패딩을 조금 더 넓히면 보기 좋습니다
                    end = 12.dp
                ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CommunityItem(
    goPostDetailScreen: () -> Unit
){
    Column (
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable{

            }
    ){
        Row (
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(100.dp)),
                painter = painterResource(id = R.drawable.ex_person),
                contentDescription = null,
                contentScale = ContentScale.Crop)
            Spacer(Modifier.width(8.dp))
            Column {
                Row {
                    Text("ARAMY_Forever")
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.background(
                            color = colorResource(R.color.color_10),
                            shape = RoundedCornerShape(16.dp)
                        )
                    ){ Text(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
                        text = "VIP",
                        color = colorResource(R.color.white)
                    ) }
                }
                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "BTS",
                        color = colorResource(R.color.color_1)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "- 2시간 전",
                        color = colorResource(R.color.color_text_3)
                    )
                }
            }
            Spacer(Modifier.weight(1f))
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
            modifier = Modifier.padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 12.dp
            ),
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
        Row (
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(id = R.drawable.icon_heart_ena),
                    contentDescription = "좋아요",
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "2,222"
                )
            }
            Spacer(Modifier.weight(1f))
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(id = R.drawable.icon_chat_ena),
                    contentDescription = "좋아요",
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "2,222"
                )
            }
            Spacer(Modifier.weight(1f))
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(id = R.drawable.icon_share_ena),
                    contentDescription = "댓글",
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "1,111"
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.icon_bookmark_ena),
                contentDescription = "좋아요",
                tint = Color.Unspecified
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CommunityScreen({},{})
}