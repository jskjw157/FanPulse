package com.aos.fanpulse.presentation.chart

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aos.fanpulse.domain.model.ChartTrack
import com.aos.fanpulse.presentation.common.CommonTopAppBar
import org.orbitmvi.orbit.compose.collectAsState
import androidx.core.net.toUri

@Composable
fun ChartScreen(
    viewModel: ChartViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {

    val state by viewModel.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {

        CommonTopAppBar(
            isActiveLeftBack = true,
            onLeftBack = { onBackClick() },
            isActiveLeftTextTitle = true,
            leftTextTitle = "차트 순위",
        )

        // Songs List
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(state.chartTracks) { index, track ->
                ChartSongItem(index + 1,track)
            }
        }
    }
}

@Composable
fun ChartSongItem(
    rank: Int,
    song: ChartTrack,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable{
                if (song.trackUrl.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, song.trackUrl.toUri())
                        context.startActivity(intent)
                    } catch (e: Exception) {
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${rank}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) Color(0xFFFF9800) else Color.Gray,
                modifier = Modifier.width(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = "앨범 커버 이미지",
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artistName,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

        }
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ChartScreenPreview() {
    ChartScreen()
}