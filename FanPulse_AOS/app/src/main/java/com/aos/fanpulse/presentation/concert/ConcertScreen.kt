package com.aos.fanpulse.presentation.concert

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Concert(
    val title: String,
    val artist: String,
    val date: String,
    val location: String,
    val priceRange: String,
    val imageRes: Int
)

@Composable
fun ConcertScreen() {
    val concerts = listOf(
        Concert(
            title = "BTS World Tour Seoul",
            artist = "BTS",
            date = "Dec 20, 2024 at 19:00 KST",
            location = "Seoul, Korea",
            priceRange = "₩150,000 - ₩300,000",
            imageRes = 0 // R.drawable.bts_concert
        ),
        Concert(
            title = "BLACKPINK World Tour",
            artist = "BLACKPINK",
            date = "Dec 25, 2024 at 18:00 KST",
            location = "Seoul, Korea",
            priceRange = "₩180,000 - ₩350,000",
            imageRes = 0 // R.drawable.blackpink_concert
        ),
        Concert(
            title = "SEVENTEEN Be The Sun",
            artist = "SEVENTEEN",
            date = "Jan 5, 2025 at 19:00 KST",
            location = "Seoul, Korea",
            priceRange = "₩140,000 - ₩280,000",
            imageRes = 0 // R.drawable.seventeen_concert
        ),
        Concert(
            title = "NewJeans Fan Meeting",
            artist = "NewJeans",
            date = "Jan 10, 2025 at 17:00 KST",
            location = "Seoul, Korea",
            priceRange = "₩120,000 - ₩250,000",
            imageRes = 0 // R.drawable.newjeans_concert
        ),
        Concert(
            title = "TWICE Encore Concert",
            artist = "TWICE",
            date = "Jan 18, 2025 at 19:00 KST",
            location = "Seoul, Korea",
            priceRange = "₩160,000 - ₩320,000",
            imageRes = 0 // R.drawable.twice_concert
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(concerts) { concert ->
            ConcertCard(concert)
        }
    }
}

@Composable
fun ConcertCard(concert: Concert) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6B2E8E),
                                Color(0xFFD946A6)
                            )
                        )
                    )
            ) {
                // 여기에 실제 이미지를 넣을 수 있습니다
                // Image(
                //     painter = painterResource(id = concert.imageRes),
                //     contentDescription = concert.title,
                //     modifier = Modifier.fillMaxSize(),
                //     contentScale = ContentScale.Crop
                // )
            }

            // Concert Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = concert.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Artist
                Text(
                    text = concert.artist,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_today),
                        contentDescription = "Date",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = concert.date,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_dialog_map),
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = concert.location,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Price Range
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                        contentDescription = "Price",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = concert.priceRange,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Get Tickets Button
                Button(
                    onClick = { /* Handle ticket purchase */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Get Tickets",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ConcertScreen()
}