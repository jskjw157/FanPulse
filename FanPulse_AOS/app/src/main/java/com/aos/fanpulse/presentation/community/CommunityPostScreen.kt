package com.aos.fanpulse.presentation.community

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aos.fanpulse.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostScreen(
    onCancel: () -> Unit,
    onPost: () -> Unit
) {
    Column {
        TopAppBar(
            navigationIcon = {
                Box(
                    modifier = Modifier.width(64.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    TextButton(onClick = onCancel) {
                        Text("취소")
                    }
                }
            },
            title = {
                Text(
                    "글쓰기",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            actions = {
                Box(
                    modifier = Modifier.width(64.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(onClick = onPost) {
                        Text("게시")
                    }
                }
            }
        )
        Text(
            text = "아티스트 선택 *"
        )
        Text(
            text = "내용 *"
        )
        Text(
            text = "이미지 첨부"
        )
        Text(
            text = "태그 (최대 5개)"
        )

        Row {
            Icon(painter = painterResource(id = R.drawable.icon_search), contentDescription = "검색", tint = Color.Black)
            Column {
                Text(text = "게시글 작성 가이드")
                Text(text = "• 타인을 존중하는 내용을 작성해주세요\n• 욕설, 비방, 허위사실은 삭제될 수 있습니다\n• 저작권을 침해하는 콘텐츠는 게시할 수 없습니다")
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