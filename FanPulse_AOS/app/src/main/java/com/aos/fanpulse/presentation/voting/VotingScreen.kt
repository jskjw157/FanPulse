package com.aos.fanpulse.presentation.voting

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data Classes
data class VotingPower(
    val total: Int,
    val daily: Int,
    val bonus: Int,
    val used: Int
)

data class VoteCategory(
    val id: String,
    val title: String,
    val totalVotes: String,
    val endDate: String,
    val items: List<VoteItem>,
    val isActive: Boolean = true
)

data class VoteItem(
    val name: String,
    val votes: String,
    val percentage: Int,
    val imageRes: Int,
    val hasGoldenBadge: Boolean = false
)

enum class VotingTab {
    ALL, ARTIST, SONG, MV
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VotingScreen() {
    var selectedTab by remember { mutableStateOf(VotingTab.ALL) }

    val votingPower = VotingPower(
        total = 12,
        daily = 10,
        bonus = 5,
        used = 3
    )

    val categories = listOf(
        VoteCategory(
            id = "male_group",
            title = "Best Male Group 2024",
            totalVotes = "2.5M votes",
            endDate = "Ends 2024-12-20",
            items = listOf(
                VoteItem("BTS", "1,250,000 votes", 50, 0, hasGoldenBadge = true),
                VoteItem("SEVENTEEN", "750,000 votes", 30, 0),
                VoteItem("Stray Kids", "500,000 votes", 20, 0)
            )
        ),
        VoteCategory(
            id = "female_group",
            title = "Best Female Group 2024",
            totalVotes = "2.1M votes",
            endDate = "Ends 2024-12-20",
            items = listOf(
                VoteItem("BLACKPINK", "1,050,000 votes", 50, 0, hasGoldenBadge = true),
                VoteItem("NewJeans", "630,000 votes", 30, 0),
                VoteItem("aespa", "420,000 votes", 20, 0)
            )
        ),
        VoteCategory(
            id = "song",
            title = "Song of the Year",
            totalVotes = "1.8M votes",
            endDate = "Ends 2024-12-25",
            items = listOf(
                VoteItem("Super Shy - NewJ", "900,000 votes", 50, 0, hasGoldenBadge = true),
                VoteItem("Seven - Jungkook", "540,000 votes", 30, 0),
                VoteItem("Spicy - aespa", "360,000 votes", 20, 0)
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Voting",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Voting Power Card
            item {
                VotingPowerCard(votingPower)
            }

            // Tab Row
            item {
                VotingTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // Vote Categories
            items(categories) { category ->
                VoteCategoryCard(category)
            }

            // Get More Votes Banner
            item {
                GetMoreVotesBanner()
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun VotingPowerCard(votingPower: VotingPower) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF9C27B0),
                        Color(0xFFE91E63)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Voting Power",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${votingPower.total}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Daily: ${votingPower.daily}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "Bonus: +${votingPower.bonus}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "Used: ${votingPower.used}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun VotingTabRow(
    selectedTab: VotingTab,
    onTabSelected: (VotingTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VotingTabButton(
            text = "All",
            icon = Icons.Default.Favorite,
            isSelected = selectedTab == VotingTab.ALL,
            onClick = { onTabSelected(VotingTab.ALL) }
        )
        VotingTabButton(
            text = "Artist",
            icon = Icons.Default.Person,
            isSelected = selectedTab == VotingTab.ARTIST,
            onClick = { onTabSelected(VotingTab.ARTIST) }
        )
        VotingTabButton(
            text = "Song",
            icon = Icons.Default.Person,
            isSelected = selectedTab == VotingTab.SONG,
            onClick = { onTabSelected(VotingTab.SONG) }
        )
        VotingTabButton(
            text = "MV",
            icon = Icons.Default.Person,
            isSelected = selectedTab == VotingTab.MV,
            onClick = { onTabSelected(VotingTab.MV) }
        )
    }
}

@Composable
fun VotingTabButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF9C27B0) else Color(0xFFF5F5F5),
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VoteCategoryCard(category: VoteCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Surface(
                    color = Color(0xFFE1BEE7),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Active",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF9C27B0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = category.totalVotes,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = category.endDate,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vote Items
            category.items.forEach { item ->
                VoteItemRow(item)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // View Full Rankings Button
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF9C27B0)
                ),
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFF9C27B0)))
                )
            ) {
                Text(
                    text = "View Full Rankings",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun VoteItemRow(item: VoteItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Artist Image with Badge
            Box {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF9C27B0),
                                    Color(0xFFE91E63)
                                )
                            )
                        )
                ) {
                    // 실제 이미지로 교체 가능
                }

                if (item.hasGoldenBadge) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "First Place",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.votes,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "${item.percentage}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )

            Spacer(modifier = Modifier.width(12.dp))
        }

        // Vote Button
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0)
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = "Vote",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun GetMoreVotesBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFA726),
                        Color(0xFFFF9800)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Get More Votes!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Watch ads or join VIP",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Earn Now",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun VotingScreenPreview() {
    VotingScreen()
}