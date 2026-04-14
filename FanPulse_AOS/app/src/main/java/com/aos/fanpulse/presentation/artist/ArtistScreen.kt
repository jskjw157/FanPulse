package com.aos.fanpulse.presentation.artist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.R
import com.aos.fanpulse.data.remote.apiservice.Artist
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ArtistScreen(
    viewModel: ArtistViewModel = hiltViewModel(),
    goSearchScreen: () -> Unit = {},
    goNotificationScreen: () -> Unit = {},
    goArtistDetail: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val filterRadioButton = viewModel.setFilterRadioButtonItems()
    var selectedFilterRadioButton by remember { mutableStateOf(filterRadioButton[0]) }

    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            // 예: 뷰모델에서 postSideEffect(SideEffect.NavigateToDetail(id)) 를 호출했을 때
            is ArtistContract.SideEffect.NavigateArtistDetail -> {
                goArtistDetail(sideEffect.artistId)
            }
            is ArtistContract.SideEffect.NavigateHome -> {

            }
            is ArtistContract.SideEffect.ShowToast -> {

            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.color_12))
    ) {
        CommonTopAppBar(
            isActiveLeftBack = true,
            onLeftBack = { onBackClick() },
            isActiveLeftImage = true,
            isActiveRightSearch = true,
            onRightSearch = { goSearchScreen() },
            isActiveRightNotification = true,
            onRightNotification = { goNotificationScreen() },
        )

        // Title Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.color_12))
                .padding(16.dp)
        ) {
            Text(
                text = "아티스트",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "좋아하는 아티스트를 팔로우하세요",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        //   Filter Button
        LazyRow(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterRadioButton) { item ->
                ArtistRadioButtonItem(
                    text = item.text,
                    filterImage = item.image,
                    isSelected = (item == selectedFilterRadioButton),
                    onClick = {
                        //  필터 필요함
                        selectedFilterRadioButton = item
                    }
                )
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            // Artist Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.color_12)),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    state.artists
                ) { artist ->
                    ArtistItem(artist = artist){
                        viewModel.goArtistDetailScreen(it)
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistRadioButtonItem(
    text: String,           // 보여줄 텍스트
    filterImage: Int?,
    isSelected: Boolean,    // 선택 여부
    onClick: () -> Unit     // 클릭 시 실행할 동작
) {

    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) colorResource(id = R.color.color_1) else colorResource(id = R.color.color_2),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable { onClick() }
    ) {
        Row (
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            if (filterImage != null){
                Image(
                    painter = painterResource(id = filterImage),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(if (isSelected) Color.White else colorResource(R.color.color_3))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                color = if (isSelected) Color.White else colorResource(R.color.color_3),
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ArtistItem(
    artist: Artist,
    goArtistDetail:(String) -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { goArtistDetail(artist.id) }
        ) {
            // Artist Image
            AsyncImage(
                model = artist.profileImageUrl,
                contentDescription = artist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Ranking Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(
                        color = colorResource(id = R.color.color_1),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                //  이상함
                Text(
                    text = "#${artist.name//ranking
                    }",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Artist Name
        Text(
            text = artist.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Artist Description   이상함
        Text(
            text = artist.name//description
            ,
            fontSize = 12.sp,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Followers and Like    이상함
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_person),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatFollowers(100000//followers
                    ),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { isFavorite = !isFavorite },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (isFavorite) colorResource(id = R.color.color_1) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun formatFollowers(followers: Int): String {
    return when {
        followers >= 1000000 -> "${followers / 100000 / 10.0}M"
        followers >= 1000 -> "${followers / 1000}K"
        else -> followers.toString()
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ArtistScreenPreview() {
    ArtistScreen(){}
}