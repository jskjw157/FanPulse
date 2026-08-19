package com.aos.fanpulse.presentation.community

import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.domain.model.Comment
import com.aos.fanpulse.domain.model.Post
import com.aos.fanpulse.presentation.R
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import com.aos.fanpulse.presentation.common.MessageInputBar
import com.aos.fanpulse.presentation.common.toRelativeTime
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CommunityDetailScreen (
    viewModel: CommunityDetailViewModel = hiltViewModel(),
    postId: String = "",
    onBackClick: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
){

    val state by viewModel.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(postId) {
        viewModel.loadData(postId)
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CommunityDetailContract.SideEffect.NavigateBack -> onBackClick()
            is CommunityDetailContract.SideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
            is CommunityDetailContract.SideEffect.NavigateToEdit -> {
                onNavigateToEdit(sideEffect.postId)
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                CommonTopAppBar(
                    setBackground = 1,
                    isActiveLeftBack = true,
                    onLeftBack = { onBackClick() },
                    isActiveCenterTextTitle = true,
                    centerTextTitle = "게시글",
                    isActiveRightThreeDot = true,
                    onRightThreeDot = { viewModel.openMenu() },
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            end = 8.dp)
                ) {
                    DropdownMenu(
                        expanded = state.isMenuExpanded,
                        onDismissRequest = { viewModel.closeMenu() }
                    ) {
                        DropdownMenuItem(
                            text = { Text("수정") },
                            onClick = { viewModel.onEditClicked(postId) }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제") },
                            onClick = { viewModel.onDeleteClicked(postId) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            MessageInputBar(
                text = state.commentInput,
                onTextChange = { viewModel.updateCommentInput(it) },
                onSendClick = { viewModel.submitComment(postId) }
            )
        }
    ) { paddingValues ->

        if (state.isLoading && state.post == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorResource(R.color.color_1))
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colorResource(R.color.white))
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    state.post?.let { post ->
                        Spacer(Modifier.height(16.dp))
                        PostDetailHeader(post)
                        Spacer(Modifier.height(16.dp))
                        Text(post.content)

                        if (post.imageUrls.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            AsyncImage(
                                model = post.imageUrls.first(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(256.dp)
                                    .background(Color.Gray, shape = RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "댓글 ${state.comments.size}",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.W700,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                            )
                        )
                    }
                }

                items(state.comments) { comment ->
                    SetCommentItem(comment)
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
@Composable
fun PostDetailHeader(post: Post) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.fanpulse_placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(100.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(post.author.nickname)
            }
            Text(post.createdAt.toRelativeTime())
        }
    }
}

@Composable
fun SetCommentItem(
    comment: Comment
){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(16.dp))
    ){
        Row (
            modifier = Modifier.padding(12.dp)
        ){
            Image(
                painter = painterResource(id = R.drawable.fanpulse_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(100.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Row {
                    Text(
                        text = comment.userId,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = comment.createdAt)
                }
                Spacer(Modifier.height(4.dp))
                Text(text = comment.content)
                Spacer(Modifier.height(8.dp))
                Row (verticalAlignment = Alignment.CenterVertically){
                    Image(
                        painter = painterResource(id = R.drawable.icon_heart),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = "23")
                    Text(text = "답글")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityPostDetailPreview() {
    CommunityDetailScreen()
}