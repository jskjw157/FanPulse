package com.aos.fanpulse.presentation.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R
import com.aos.fanpulse.presentation.notifications.Notification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    goPostScreen: () -> Unit,
    goSearchScreen: () -> Unit,
    goPostDetailScreen: () -> Unit,
    goNotificationScreen: () -> Unit,
){
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            title = {
                Text(text = "Community")
            },
            actions = {
                // 오른쪽 아이콘들 (순서대로 배치됨)
                IconButton(onClick = { goSearchScreen() }) {
                    Icon(painter = painterResource(id = R.drawable.icon_search), contentDescription = null, tint = Color.Black)
                }
                IconButton(onClick = { goNotificationScreen()}) {
                    Icon(painter = painterResource(id = R.drawable.icon_alarm_inactive), contentDescription = null, tint = Color.Black)
                }
            }
        )
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
                        CommunityItem(
                            goPostDetailScreen = {
                                //  게시물에 대한 정보가 필요
                                goPostDetailScreen()
                            }
                        )
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
//            ModalBottomSheet(
//                onDismissRequest = { showBottomSheet = false },
//                sheetState = sheetState,
//                containerColor = Color.White, // 시트 배경색
//                dragHandle = { BottomSheetDefaults.DragHandle() }
//            ) {
                BottomSheetContent{
                    showBottomSheet = it
                }
//            }
            }
        }
    }
}

data class Artist(
    val name: String,
    val posts: Int,
    val verified: Boolean = false
)

@Composable
fun BottomSheetContent(
    setShowModal: (Boolean) -> Unit
) {

    var searchQuery by remember { mutableStateOf("") }
    var selectedArtist by remember { mutableStateOf<String?>(null) }

    val artists = listOf(
        Artist("1234", 1234, verified = true),
        Artist("IU", 456),
        Artist("BLACKPINK", 389),
        Artist("SEVENTEEN", 267),
        Artist("NewJeans", 198),
        Artist("Stray Kids", 156),
        Artist("TWICE", 234),
        Artist("TXT", 145),
        Artist("ENHYPEN", 178),
        Artist("ITZY", 123),
        Artist("LE SSERAFIM", 167),
        Artist("BNFITN", 89)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { setShowModal(false) }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Select Artist",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = {
                        setShowModal(false)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.icon_close),
                            contentDescription = null,
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("아티스트 검색...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.icon_search),
                            contentDescription = null,
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFEEEEEE),
                        focusedBorderColor = Color(0xFF9C27B0)
                    ),
                    singleLine = true
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artists.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }) { artist ->
                        ArtistCard(
                            artist = artist,
                            isSelected = selectedArtist == artist.name,
                            onClick = { selectedArtist = artist.name }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist: Artist,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFF3E5F5) else Color.White,
        border = BorderStroke(
            2.dp,
            if (isSelected) Color(0xFF9C27B0) else Color(0xFFEEEEEE)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Verified Badge
            if (artist.verified) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9C27B0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "인증",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Artist Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF9C27B0) else Color(0xFFE1BEE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0xFFBA68C8))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Artist Name
                Text(
                    text = artist.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Post Count
                Text(
                    text = "${artist.posts}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
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
                goPostDetailScreen()
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
                painter = painterResource(id = R.drawable.person_ex1),
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
    CommunityScreen({},{},{}, {})
}