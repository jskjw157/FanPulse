package com.aos.fanpulse.presentation.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.fanpulse.R

data class SearchTag(
    val id: Int,
    val text: String,
    val isRemovable: Boolean = true
)

data class PopularSearch(
    val rank: Int,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var recentSearches by remember {
        mutableStateOf(
            listOf(
                SearchTag(1, "BTS"),
                SearchTag(2, "BLACKPINK"),
                SearchTag(3, "콘서트"),
                SearchTag(4, "NewJeans")
            )
        )
    }

    val popularSearches = remember {
        listOf(
            PopularSearch(1, "BTS 새 앨범"),
            PopularSearch(2, "BLACKPINK 투어"),
            PopularSearch(3, "SEVENTEEN"),
            PopularSearch(4, "NewJeans 뮤비")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp),
                        placeholder = {
                            Text(text = "검색어를 입력하세요", color = Color(0xFF999999), fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_search),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_close),
                                        contentDescription = "지우기",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_left_arrow),
                            contentDescription = "뒤로가기",
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        // innerPadding이 상단바 여백을 자동으로 잡아줍니다.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "최근 검색어",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "전체 삭제",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.clickable {
                        recentSearches = emptyList()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FlowRow를 사용하여 검색 태그들이 넘치면 자동으로 다음 줄로 넘어가게 함
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentSearches.forEach { tag ->
                    SearchTagChip(
                        tag = tag,
                        onRemove = {
                            recentSearches = recentSearches.filter { it.id != tag.id }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F8F8), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "인기 검색어",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    popularSearches.forEachIndexed { index, search ->
                        PopularSearchItem(search = search)
                        if (index != popularSearches.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SearchTagChip(
    tag: SearchTag,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(1.dp, Color(0xFFCCCCCC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🕐",
                    fontSize = 10.sp
                )
            }

            Text(
                text = tag.text,
                fontSize = 14.sp,
                color = Color(0xFF333333)
            )

            if (tag.isRemovable) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_close),
                    contentDescription = "Remove",
                    tint = Color(0xFF999999),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onRemove)
                )
            }
        }
    }
}

@Composable
fun PopularSearchItem(search: PopularSearch) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val rankColor = when (search.rank) {
            1 -> Color(0xFFB794F6)
            2 -> Color(0xFFEC4899)
            3 -> Color(0xFF8B5CF6)
            4 -> Color(0xFF7C3AED)
            else -> Color(0xFF999999)
        }

        Text(
            text = search.rank.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = rankColor,
            modifier = Modifier.width(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = search.text,
            fontSize = 15.sp,
            color = Color(0xFF333333)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen({})
}