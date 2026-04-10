package com.aos.fanpulse.presentation.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aos.fanpulse.R

@Composable
fun CommunityBottomSheetScreen(
    viewModel: CommunityScreenViewModel = hiltViewModel(),
    setShowModal: (Boolean) -> Unit
) {

    val artistList by viewModel.artists.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchArtists()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedArtist by remember { mutableStateOf<String?>(null) }

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
                //  AppBar
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
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    IconButton(onClick = {
                        setShowModal(false)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.icon_close),
                            contentDescription = null,
                            tint = Color.Black
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
                    shape = RoundedCornerShape(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF3F4F6),
                        focusedBorderColor = Color(0xFF9C27B0),
                        focusedContainerColor = Color(0xFFF3F4F6),      // 커서가 깜빡일 때(포커스) 배경색
                        unfocusedContainerColor = Color(0xFFF3F4F6)     // 평상시(언포커스) 배경색
                    ),
                    singleLine = true
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artistList.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }) { artist ->
                        ArtistCard(
                            artist = artist,
                            isSelected = selectedArtist == artist.name,
                            onClick = {
                                selectedArtist = artist.name
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist: CommunityScreenViewModel.Artist,
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

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

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

                Text(
                    text = artist.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun CommunityBottomSheetPreview() {
    CommunityBottomSheetScreen(setShowModal = {})
}