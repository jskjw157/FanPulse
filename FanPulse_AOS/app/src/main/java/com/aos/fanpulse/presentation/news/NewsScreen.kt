package com.aos.fanpulse.presentation.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.R
import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel(),
    goSearchScreen: () -> Unit = {},
    goNewsDetailScreen: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
) {

    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            NewsContract.SideEffect.NavigateHome -> {}
            is NewsContract.SideEffect.NavigateNewsDetail -> {
                goNewsDetailScreen(sideEffect.newsId)
            }
            is NewsContract.SideEffect.ShowToast -> {}
        }
    }
    val filterRadioButton = viewModel.setFilterRadioButtonItems()
    var selectedFilterRadioButton by remember { mutableStateOf(filterRadioButton[0]) }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .paint(
                painter = painterResource(id = R.drawable.loginscreen_bg),
                contentScale = ContentScale.Crop
            )
    ) {
        CommonTopAppBar(
            isActiveLeftBack = true,
            onLeftBack = { onBackClick() },
            isActiveCenterTextTitle = true,
            centerTextTitle = "뉴스",
            isActiveRightSearch = true,
            onRightSearch = { goSearchScreen() },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
        ) {
            Spacer((Modifier.height(12.dp)))
            //   Filter Button
            LazyRow(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 12.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterRadioButton) { item ->
                    NewsRadioButtonItem(
                        text = item.text,
                        isSelected = (item == selectedFilterRadioButton),
                        onClick = {
                            //  필터 필요함
                            selectedFilterRadioButton = item
                        }
                    )
                }
            }

            // News List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFDF2F8))
                ,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.newsItem) { newsItem ->
                    NewsCard( newsItem ){
                        viewModel.goNewsDetail(it)
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(
    newsItem: NewsDetail,
    goNewsDetail: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF),
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column (
            modifier = Modifier.clickable{
                goNewsDetail(newsItem.id)
            }
        ){

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    AsyncImage(
                        model = newsItem.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop, // 영역에 꽉 차게 잘라서 보여줌 (중요)
                        // placeholder = painterResource(R.drawable.img_placeholder), // 로딩 중에 보여줄 이미지 (선택)
                        // error = painterResource(R.drawable.img_error) // URL이 null이거나 로드 실패 시 보여줄 이미지 (선택)
                    )
                }
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    color = colorResource(R.color.color_1),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = newsItem.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = newsItem.title,
                    color = Color.Black,
                    maxLines = 1,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = newsItem.content,
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_viewer),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = newsItem.viewCount.toString(),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            painter = painterResource(R.drawable.icon_like),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = newsItem.viewCount.toString(),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = newsItem.publishedAt,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NewsRadioButtonItem(
    text: String,           // 보여줄 텍스트
    isSelected: Boolean,    // 선택 여부
    onClick: () -> Unit     // 클릭 시 실행할 동작
) {

    Box(
        modifier = Modifier
            .background(
                brush = if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF9333EA), // 왼쪽 시작 색상
                            Color(0xFFDB2777)  // 오른쪽 끝 색상
                        )
                    )
                } else {
                    SolidColor(colorResource(id = R.color.color_2))
                },
                shape = RoundedCornerShape(100.dp)
            )
            .clickable { onClick() }
    ) {
        Text(
            color = if (isSelected) Color.White else Color.Black,
            text = text,
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
fun GreetingPreview() {
    NewsScreen()
}