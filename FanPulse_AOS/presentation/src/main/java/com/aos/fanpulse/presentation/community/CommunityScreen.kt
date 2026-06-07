package com.aos.fanpulse.presentation.community

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aos.fanpulse.domain.model.Post
import com.aos.fanpulse.presentation.R
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import com.aos.fanpulse.presentation.common.toRelativeTime
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CommunityScreen(
    navController: NavController? = null,
    goPostScreen: () -> Unit,
    goSearchScreen: () -> Unit,
    goPostDetailScreen: (String) -> Unit,
    goNotificationScreen: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
){
    var showBottomSheet by remember { mutableStateOf(false) }

    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val savedStateHandle = navController?.currentBackStackEntry?.savedStateHandle
    val isPostClosed = savedStateHandle?.getStateFlow("post_closed", false)?.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CommunityContract.SideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    LaunchedEffect(isPostClosed?.value) {
        if (isPostClosed?.value == true) {
            viewModel.loadPosts(artistCategory = null)
            savedStateHandle["post_closed"] = false
        }
    }

    var selectedFilterRadioButton by remember { mutableStateOf(state.filterItems.firstOrNull()) }

    Column {

        CommonTopAppBar(
            isActiveLeftTextTitle = true,
            leftTextTitle = "Community",
            isActiveRightSearch = true,
            onRightSearch = { goSearchScreen() },
            isActiveRightNotification = true,
            onRightNotification = { goNotificationScreen() },
        )

        Box (modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .background(colorResource(id = R.color.white))
                    .fillMaxHeight()
            ) {
                Spacer((Modifier.height(16.dp)))
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
                    Text(state.selectedArtist?.name ?: "ALL")
                    Spacer((Modifier.width(8.dp)))
                    Text("(${state.posts.size} posts)")
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
                //   Filter Button
//                LazyRow(
//                    modifier = Modifier
//                        .padding(
//                            start = 16.dp,
//                            end = 16.dp,
//                            bottom = 8.dp
//                        ),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    items(state.filterItems) { item ->
//                        CommunityRadioButtonItem(
//                            text = item.text,
//                            isSelected = (item == selectedFilterRadioButton),
//                            onClick = { selectedFilterRadioButton = item }
//                        )
//                    }
//                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 0.dp),
                    thickness = 1.dp,
                    color = Color(0xFFEEEEEE)
                )

                //  게시물
                LazyColumn(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.posts) { item ->
                        CommunityItem(
                            item,
                            goPostDetailScreen = {
                                goPostDetailScreen(item.id)
                            },
                            onLikeClick = {
                                viewModel.toggleLike(item.id)
                            },
                            onBookmarkClick = {
                                viewModel.toggleBookmark(item.id)
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
                CommunityBottomSheetScreen(
                    setShowModal = { isVisible ->
                        showBottomSheet = isVisible
                    }
                )
            }
        }
    }
}

@Composable
fun CommunityItem(
    post: Post,
    goPostDetailScreen: () -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
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

            AsyncImage(
                model = post.author.profileImageUrl,
                contentDescription = "작성자 프로필 이미지",
                placeholder = painterResource(id = R.drawable.default_user),
                error = painterResource(id = R.drawable.default_user),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            Column {
                Row {
                    Text(post.author.nickname)
                    Spacer(Modifier.width(8.dp))
                }
                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = post.author.fandom?: "소속 없음",
                        color = colorResource(R.color.color_1)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = post.createdAt.toRelativeTime(),
                        color = colorResource(R.color.color_text_3)
                    )
                }
            }
            Spacer(Modifier.weight(1f))
        }
        Text(
            modifier = Modifier.padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 12.dp
            ),
            text = post.content
        )

        if (post.imageUrls.isNotEmpty()) {
            AsyncImage(
                model = post.imageUrls[0],
                contentDescription = "썸네일",
                placeholder = painterResource(id = R.drawable.fanpulse_placeholder),
                error = painterResource(id = R.drawable.fanpulse_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp),
            )
        }

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
                    painter = if (post.isLikedByMe) painterResource(id = R.drawable.icon_heart_ena_pre) else painterResource(id = R.drawable.icon_heart_ena_nor),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.clickable { onLikeClick() }
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = post.likeCount.toString()
                )
            }
            Spacer(Modifier.weight(1f))
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(id = R.drawable.icon_chat_ena),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = post.commentCount.toString()
                )
            }
            Spacer(Modifier.weight(1f))
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(id = R.drawable.icon_share_ena),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = post.shareCount.toString()
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                painter = if (post.isBookmarkedByMe) painterResource(id = R.drawable.icon_bookmark_ena_pre) else painterResource(id = R.drawable.icon_bookmark_ena_nor),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.clickable { onBookmarkClick() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CommunityScreen(goPostDetailScreen = {}, goSearchScreen = {}, goNotificationScreen = {}, goPostScreen = {})
}