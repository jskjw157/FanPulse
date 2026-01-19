package com.aos.fanpulse.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostScreen(
    onCancel: () -> Unit,
    onPost: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column {
        TopAppBar(
            modifier = Modifier.height(48.dp),
            navigationIcon = {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.CenterStart
                ) {
                    TextButton(onClick = onCancel) {
                        Text(
                            text = "취소",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            title = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "게시글 작성",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier.width(64.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(onClick = onPost) {
                        Text(
                            text = "게시",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        )
        Column (
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize()
        ){
            Text(
                text = "아티스트 선택 *"
            )
            Spacer(Modifier.height(12.dp))
/*            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(6) { index ->
                    CommunityRadioButtonItem()
                }
            }*/
            Spacer(Modifier.height(16.dp))
            Text(text = "내용 *")
            Spacer(Modifier.height(12.dp))
            LimitedTextField()
            Text(text = "이미지 첨부")
            Spacer(Modifier.height(12.dp))
            ImageUploadSection(){}
            Spacer(Modifier.height(16.dp))
            Text(text = "태그 (최대 5개)")
            Spacer(Modifier.height(12.dp))
            TagInputSection()
            Spacer(Modifier.height(50.dp))
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(R.color.color_12), RoundedCornerShape(16.dp)
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
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun LimitedTextField() {
    var text by remember { mutableStateOf("") }
    val maxLength = 500

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TextField(
            value = text,
            onValueChange = {
                if (it.length <= maxLength) {
                    text = it
                }
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp)),
            placeholder = { Text(
                "팬 여러분과 공유하고 싶은 이야기를 작성해주세요...",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = colorResource(R.color.color_text_4)
                )
            ) },
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
fun ImageUploadSection(onClick: () -> Unit) {
    val stroke = Stroke(
        width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
    val cornerColor = colorResource(R.color.color_11)
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
            .clickable { onClick() },
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
                text = "이미지 추가 (최대 5장)",
                color = colorResource(R.color.color_text_3),
                fontSize = 14.sp
            )
        }
    }
}
@Composable
fun TagInputSection() {
    var tagInput by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>() }

    val onAddTag = {
        if (tagInput.isNotBlank()) {
            tags.add(tagInput.trim())
            tagInput = "" 
        }
    }

    Column() {
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
                keyboardActions = KeyboardActions(onDone = { onAddTag() }),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tagInput.isEmpty()) {
                            Text(
                                text = "태그 입력 후 엔터",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
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
                    .clickable { onAddTag() },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "추가",
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        }

        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                AssistChip(
                    onClick = { tags.remove(tag) },
                    label = { Text("#$tag") },
//                    trailingIcon = { Icon(Icons.Default.Close, contentSize = 12.dp) }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun CommunityPreview() {
    CommunityPostScreen(
        onCancel = {},
        onPost = {}
    )
}