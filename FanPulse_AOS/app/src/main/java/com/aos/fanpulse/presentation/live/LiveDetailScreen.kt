package com.aos.fanpulse.presentation.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 가상의 채팅 데이터 모델
data class ChatMessage(
    val sender: String,
    val message: String,
    val timestamp: String,
    val profileInitial: String = sender.take(1) // 간단한 프로필 이니셜
)

// 가상의 채팅 데이터 목록 (예시)
val sampleChats = listOf(
    ChatMessage("민지팬123", "오늘 무대 최고예요! 🔥", "2분 전"),
    ChatMessage("하니러버", "라이브 음색 미쳤다 ㅠㅠ", "1분 전"),
    ChatMessage("뉴진스사랑", "다들 너무 예뻐요 💕", "방금")
)

@Composable
fun LiveDetailScreen() {
    // Scaffold를 사용하여 기본 상하단 구조 잡기
    Scaffold(
        topBar = { /* 필요하다면 앱바 */ },
        bottomBar = { /* 필요하다면 하단 탭바 */ }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. 상단 비디오 및 방송 정보 영역 (전체 높이의 절반 정도 할당)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f) // 화면의 50%를 차지
                    .background(Color.Black) // 실제 비디오는 여기에 배치
            ) {
                // 가상의 비디오 이미지 (사진 속 공연 모습)
                // 실제 앱에서는 Video Player 뷰가 들어갈 자리입니다.
                // Image(painter = painterResource(id = R.drawable.video_placeholder), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

                // 가상의 비디오 영역을 나타내기 위해 검은 배경 위에 텍스트로 대체
                Text(text = "LIVE VIDEO AREA", modifier = Modifier.align(Alignment.Center), color = Color.White, fontSize = 24.sp)

                // 비디오 영역 위에 겹쳐지는 정보들 (오버레이)
                LiveOverlay()
            }

            // 2. 중간 인터랙션 바 (고정된 높이)
            InteractionBar()

            Divider(color = Color.LightGray, thickness = 1.dp) // 구분선

            // 3. 하단 채팅 영역 (나머지 전체 높이)
            ChatSection(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
fun LiveOverlay() {
    // LIVE 태그와 시청자 수 (상단 우측)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LIVE 빨간 점과 텍스트
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.Red, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        // 시청자 수
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "24,583", color = Color.White, fontSize = 12.sp)
        }
    }

    // 하단 텍스트 가독성을 위한 그라데이션 오버레이
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // 오버레이 높이
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    )

    // 프로필 정보와 팔로우 버튼 (하단 좌/우)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 영역
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 프로필 이미지 (가상)
            // Image(painter = painterResource(id = R.drawable.profile_pic), contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                // 실제 프로필 이미지는 여기에
                Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.align(Alignment.Center), tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            // 타이틀 및 작성자
            Column {
                Text(text = "NewJeans 컴백 쇼케이스", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "NewJeans Official", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun InteractionBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InteractionItem(icon = Icons.Default.FavoriteBorder, text = "15.2K")
        InteractionItem(icon = Icons.Default.Close, text = "선물") // 선물 아이콘 예시 (Clost는 임시)
        InteractionItem(icon = Icons.Default.Share, text = "공유")
    }
}

@Composable
fun InteractionItem(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = text, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun ChatSection(modifier: Modifier = Modifier) {
    var chatMessages by remember { mutableStateOf(sampleChats) }
    var newMessageText by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        // 채팅 제목 및 인원수
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "실시간 채팅", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Text(text = "8,542명 참여중", fontSize = 12.sp, color = Color.Gray)
        }

        Divider(color = Color.LightGray, thickness = 1.dp) // 구분선

        // 실시간 채팅 목록
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // 남은 공간 차지
            reverseLayout = true, // 아래에서 위로 스크롤
            contentPadding = PaddingValues(bottom = 16.dp) // 입력창 위에 여백
        ) {
            items(chatMessages.reversed()) { chat ->
                ChatItem(chat = chat)
            }
        }

        // 메시지 입력창
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 입력 필드
            OutlinedTextField(
                value = newMessageText,
                onValueChange = { newMessageText = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White),
                placeholder = { Text(text = "메시지를 입력하세요...", color = Color.Gray, fontSize = 14.sp) },
                singleLine = true,
                shape = CircleShape,
                trailingIcon = {
                    // 전송 아이콘
                    IconButton(
                        onClick = {
                            if (newMessageText.isNotBlank()) {
                                chatMessages = chatMessages + ChatMessage("나", newMessageText, "방금")
                                newMessageText = ""
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "전송", tint = Color.Red, modifier = Modifier.size(24.dp))
                    }
                },
            )
        }
    }
}

@Composable
fun ChatItem(chat: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 가상의 프로필 이미지 (이니셜/색상)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (chat.sender == "나") Color.Blue else Color.Magenta)
        ) {
            Text(
                text = chat.profileInitial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        // 닉네임, 시간, 메시지
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = chat.sender, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = chat.timestamp, fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = chat.message, fontSize = 14.sp, color = Color.Black)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LiveStreamScreenPreview() {
    LiveDetailScreen()
}