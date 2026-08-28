package com.aos.fanpulse.presentation.community

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.domain.model.Artist
import com.aos.fanpulse.presentation.R
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CommunityPostScreen(
    onBackClick: () -> Unit = {},
    viewModel: CommunityPostViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CommunityPostContract.SideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
            is CommunityPostContract.SideEffect.NavigateBack -> {
                onBackClick()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.white))
        ){
            CommonTopAppBar(
                isActiveLeftTextTitle = true,
                leftTextTitle = "취소",
                onLeftTextTitle = { onBackClick() },
                isActiveCenterTextTitle = true,
                centerTextTitle = "게시글 작성",
                isActiveRightTextTitle = true,
                rightTextTitle = "게시",
                onRightTextTitle = {
                    viewModel.createPost(state.selectedArtist?.name ?: "All")
                },
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(text = "아티스트 선택 *")
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // 아티스트 목록
                items(state.artists) { artist ->
                    CommunityRadioButtonItem(
                        artist = artist,
                        isSelected = (state.selectedArtist == artist),
                        onClick = {
                            viewModel.updateSelectedArtist(it)
                        }
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        Text(text = "내용 *")
                        Spacer(Modifier.height(12.dp))

                        LimitedTextField(
                            text = state.content,
                            onTextChange = { viewModel.updateContent(it) }
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(text = "이미지 첨부")
                        Spacer(Modifier.height(12.dp))

                        if (state.selectedImages.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.selectedImages) { uri ->
                                    Box {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeImage(uri) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "삭제", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        ImageUploadSection(
                            currentImageCount = state.selectedImages.size,
                            onImagesSelected = { newUris ->
                                viewModel.addImages(newUris)
                            }
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(text = "태그 (최대 5개)")
                        Spacer(Modifier.height(12.dp))
                        TagInputSection(
                            tags = state.tags,
                            onAddTag = { viewModel.addTag(it) },
                            onRemoveTag = { viewModel.removeTag(it) }
                        )

                        Spacer(Modifier.height(50.dp))
                        GuideSection()
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LimitedTextField(
    text: String,
    onTextChange: (String) -> Unit
) {
    val maxLength = 500

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = text,
            onValueChange = { onTextChange(it) }, // 상위로 전달
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp)),
            placeholder = {
                Text(
                    "팬 여러분과 공유하고 싶은 이야기를 작성해주세요...",
                    style = TextStyle(fontSize = 16.sp, color = colorResource(R.color.color_text_4))
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF3F4F6),
                unfocusedContainerColor = Color(0xFFF3F4F6),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
        )

        Text(
            text = "${text.length} / $maxLength",
            color = if (text.length >= maxLength) Color.Red else Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ImageUploadSection(
    currentImageCount: Int,
    onImagesSelected: (List<Uri>) -> Unit
) {
    val maxSelection = 5 - currentImageCount

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = if (maxSelection > 0) maxSelection else 1
        ),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                onImagesSelected(uris)
            }
        }
    )

    val stroke = Stroke(
        width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
    val cornerColor = colorResource(R.color.color_11)

    if (currentImageCount >= 5) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
            .drawBehind {
                drawRoundRect(
                    color = cornerColor,
                    style = stroke,
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clickable(enabled = currentImageCount < 5) {
                multiplePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.icon_image_add),
                contentDescription = "Camera",
                tint = colorResource(R.color.color_text_3),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "이미지 추가 ($currentImageCount/5)",
                color = colorResource(R.color.color_text_3),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TagInputSection(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit
) {
    var tagInput by remember { mutableStateOf("") }

    val handleAddTag = {
        if (tagInput.isNotBlank()) {
            onAddTag(tagInput)
            tagInput = ""
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = tagInput,
                onValueChange = { tagInput = it },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp)),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { handleAddTag() }),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tagInput.isEmpty()) {
                            Text(text = "태그 입력 후 엔터", fontSize = 14.sp, color = Color.Gray)
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .height(40.dp)
                    .width(60.dp)
                    .background(
                        color = colorResource(R.color.color_1),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .clickable { handleAddTag() },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "추가", fontSize = 14.sp, color = Color.White)
            }
        }

        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                AssistChip(
                    onClick = { onRemoveTag(tag) },
                    label = { Text("#$tag") },
                )
            }
        }
    }
}

@Composable
fun GuideSection(){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.color_12), RoundedCornerShape(16.dp)
            )
    ){
        Row (
            modifier = Modifier.padding(16.dp)
        ){
            Icon(painter = painterResource(id = R.drawable.icon_post_warning), contentDescription = null, tint = colorResource(R.color.color_1))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = "게시글 작성 가이드",
                    fontSize = 14.sp,
                    color = colorResource(R.color.color_text_5)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "• 타인을 존중하는 내용을 작성해주세요\n• 욕설, 비방, 허위사실은 삭제될 수 있습니다\n• 저작권을 침해하는 콘텐츠는 게시할 수 없습니다",
                    fontSize = 12.sp,
                    color = colorResource(R.color.color_8)
                )
            }
        }
    }
}

@Composable
fun CommunityRadioButtonItem(
    artist: Artist,
    isSelected: Boolean,
    onClick: (Artist) -> Unit
) {

    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) colorResource(id = R.color.color_1) else colorResource(id = R.color.color_2),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable { onClick(artist) }
    ) {
        Text(
            color = if (isSelected) Color.White else Color.Black,
            text = artist.name,
            modifier = Modifier
                .padding(
                    top = 4.dp,
                    bottom = 4.dp,
                    start = 12.dp,
                    end = 12.dp
                ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityPreview() {
    CommunityPostScreen()
}