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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aos.fanpulse.R
import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

// ── 데이터 모델 ─────────────────────────────────────────────────────────────────
data class ChatMessage(
    val userName: String,
    val message: String,
    val timeAgo: String,
    val avatarInitial: String,
    val avatarColor: Color
)

data class ActionItem(
    val icon: @Composable () -> Unit,
    val count: String?,
    val label: String
)

// ── 샘플 데이터 ─────────────────────────────────────────────────────────────────
private val sampleChats = listOf(
    ChatMessage("민지팬123", "오늘 무대 최고에요! 🔥", "2분 전", "민", Color(0xFFAB47BC)),
    ChatMessage("하니러버",  "라이브 음색 미쳤다 ㅠㅠ",   "1분 전", "하", Color(0xFF42A5F5)),
    ChatMessage("뉴진스사랑","다들 너무 예뻐요 💕",       "방금",   "뉴", Color(0xFFEF5350))
)

// ══════════════════════════════════════════════════════════════════════════════
//  메인 화면
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun LiveDetailScreen(
    viewModel: LiveDetailViewModel = hiltViewModel(),
    liveId: String? = null,
    onBackClick: () -> Unit = {},
) {
    var messageInput by remember { mutableStateOf("") }

    val state by viewModel.collectAsState()
    LaunchedEffect(liveId) {
        if (liveId != null) {
            viewModel.getLiveDetail(liveId)
        }
    }
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            LiveDetailContract.SideEffect.NavigateHome -> {}
            is LiveDetailContract.SideEffect.ShowToast -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. 라이브 썸네일 + 오버레이
        LiveThumbnailSection(
            streamingEventDetail = state.streamingEventDetailItem!!,
            onBack = onBackClick,
            onShare = {}
        )

        // 2. 액션 바 (좋아요 / 댓글 / 선물 / 공유) 이상함 // 공유는 상단바에 있고 좋아요는 따로 해놓는게 좋을꺼 같음 나머지는 필요 없음
        ActionBar()

        HorizontalDivider(color = colorResource(R.color.color_17), thickness = 1.dp)

        // 3. 실시간 채팅
        ChatSection(
            modifier = Modifier.weight(1f),
            messages = sampleChats
        )

        HorizontalDivider(color = colorResource(R.color.color_17), thickness = 1.dp)

        // 4. 채팅 입력
        ChatInputBar(
            value = messageInput,
            onValueChange = { messageInput = it },
            onSend = { messageInput = "" }
        )
    }
}

// ── 라이브 썸네일 ──────────────────────────────────────────────────────────────
@Composable
fun LiveThumbnailSection(
    streamingEventDetail: StreamingEventDetail,
    onBack: () -> Unit,
    onShare: () -> Unit,
    )
{
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // 배경 (그라디언트로 공연 분위기 연출)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A0DAD),
                            Color(0xFFD63384),
                            Color(0xFF8B008B)
                        )
                    )
                )
        )

        // 상단 바 (뒤로가기 / 공유 / 더보기)
        CommonTopAppBar(
            setTransparentBackground = true,
            isActiveLeftBack = true,
            onLeftBack = { onBack() },
            isActiveRightShare = true,
            onRightShare = {}
        )

        // LIVE 배지 + 시청자 수
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiveBadge()
        }

        ViewerCount(
            count = streamingEventDetail.viewerCount.toString(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 16.dp, top = 52.dp)
        )

        // 하단 채널 정보 + 팔로우 버튼
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x99000000))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 채널 아바타   //  이상함 사용자 이미지 필요
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD63384)),
                contentAlignment = Alignment.Center
            ) {
                Text("N", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = streamingEventDetail.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = streamingEventDetail.artistName,
                    color = Color(0xFFDDDDDD),
                    fontSize = 12.sp
                )
            }

            // 팔로우 버튼
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.color_15)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("팔로우", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// ── LIVE 배지 ──────────────────────────────────────────────────────────────────
@Composable
fun LiveBadge() {
    Box(
        modifier = Modifier
            .background(Color(0xFFE91E8C), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.White, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("LIVE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
    }
}

// ── 시청자 수 ──────────────────────────────────────────────────────────────────
@Composable
fun ViewerCount(count: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0x99000000), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.icon_viewer), contentDescription = null,
                tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(count, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ── 액션 바 ────────────────────────────────────────────────────────────────────
@Composable
fun ActionBar(
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionBarItem(icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null,
            tint = colorResource(R.color.color_19), modifier = Modifier.size(24.dp)) },
            count = "15.2K", label = null)

        ActionBarItem(icon = { Icon(painterResource(R.drawable.icon_chat), contentDescription = null,
            tint = colorResource(R.color.color_19), modifier = Modifier.size(24.dp)) },
            count = "8.5K", label = null)

        ActionBarItem(icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null,
            tint = colorResource(R.color.color_19), modifier = Modifier.size(24.dp)) },
            count = null, label = "선물")

        ActionBarItem(icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null,
            tint = colorResource(R.color.color_19), modifier = Modifier.size(24.dp)) },
            count = null, label = "공유")
    }
}

@Composable
fun ActionBarItem(
    icon: @Composable () -> Unit,
    count: String?,
    label: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count ?: label ?: "",
            fontSize = 12.sp,
            color = colorResource(R.color.color_19),
            fontWeight = FontWeight.Medium
        )
    }
}

// ── 채팅 섹션 ──────────────────────────────────────────────────────────────────
@Composable
fun ChatSection(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>
) {
    Column(modifier = modifier.background(Color.White)) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "실시간 채팅",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colorResource(R.color.color_18)
            )
            Text(
                text = "8,542명 참여중",
                fontSize = 12.sp,
                color = colorResource(R.color.color_19)
            )
        }

        // 채팅 목록
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(msg)
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(verticalAlignment = Alignment.Top) {
        // 아바타
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(message.avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.avatarInitial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colorResource(R.color.color_18)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message.timeAgo,
                    fontSize = 11.sp,
                    color = colorResource(R.color.color_19)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.message,
                fontSize = 13.sp,
                color = colorResource(R.color.color_18),
                lineHeight = 18.sp
            )
        }
    }
}

// ── 채팅 입력창 ────────────────────────────────────────────────────────────────
@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("메시지를 입력하세요...", color = Color(0xFFBBBBBB), fontSize = 14.sp)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = colorResource(R.color.color_16),
                unfocusedContainerColor = colorResource(R.color.color_16),
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor             = colorResource(R.color.color_15)
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 전송 버튼
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.color_15)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onSend) {
                Icon(
                    Icons.Outlined.Send,
                    contentDescription = "전송",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── 프리뷰 ─────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LiveDetailScreenPreview() {
    LiveDetailScreen()
}