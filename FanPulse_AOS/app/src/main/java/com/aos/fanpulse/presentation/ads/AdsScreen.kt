package com.aos.fanpulse.presentation.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data Classes
data class PointsInfo(
    val totalPoints: Int
)

data class EarnTask(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val icon: ImageVector,
    val color: Color,
    val remaining: Int
)

data class RecentEarning(
    val title: String,
    val timeAgo: String,
    val points: Int
)

data class RewardItem(
    val title: String,
    val points: Int,
    val imageRes: Int, // 실제로는 drawable 리소스
    val isLimited: Boolean = false,
    val inStock: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdsScreen() {
    val pointsInfo = PointsInfo(totalPoints = 2450)

    val earnTasks = listOf(
        EarnTask(
            id = "video",
            title = "Watch 30s Video Ad",
            description = "Earn points by watching short videos",
            points = 10,
            icon = Icons.Default.PlayArrow,
            color = Color(0xFF2196F3),
            remaining = 5
        ),
        EarnTask(
            id = "survey",
            title = "Complete Survey",
            description = "Share your opinion and earn more",
            points = 50,
            icon = Icons.Default.Person,
            color = Color(0xFF4CAF50),
            remaining = 3
        ),
        EarnTask(
            id = "app",
            title = "Install Partner App",
            description = "Download and try new apps",
            points = 100,
            icon = Icons.Default.Person,
            color = Color(0xFF9C27B0),
            remaining = 2
        ),
        EarnTask(
            id = "checkin",
            title = "Daily Check-in",
            description = "Login daily to earn bonus points",
            points = 20,
            icon = Icons.Default.Person,
            color = Color(0xFFFF9800),
            remaining = 1
        )
    )

    val recentEarnings = listOf(
        RecentEarning("Watched Video Ad", "5 min ago", 10),
        RecentEarning("Daily Check-in", "2 hours ago", 20),
        RecentEarning("Completed Survey", "1 day ago", 50)
    )

    val rewards = listOf(
        RewardItem("BTS Official Lightstick", 5000, 0, isLimited = true),
        RewardItem("BLACKPINK Photo Card Set", 2000, 0, inStock = true),
        RewardItem("NewJeans Album (Signed)", 8000, 0, isLimited = true),
        RewardItem("Concert Ticket Discount 20%", 1500, 0, inStock = true),
        RewardItem("Official T-Shirt", 3000, 0, inStock = true),
        RewardItem("VIP Membership 1 Month", 1000, 0, inStock = true)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ads & Rewards",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "History"
                        )
                    }
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
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // My Points Card
            item {
                MyPointsCard(pointsInfo)
            }

            // Earn Points Section
            item {
                Text(
                    text = "Earn Points",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            item {
                EarnTasksGrid(earnTasks)
            }

            // Recent Earnings Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Earnings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            items(recentEarnings) { earning ->
                RecentEarningItem(earning)
            }

            // Redeem Rewards Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Redeem Rewards",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    TextButton(onClick = { }) {
                        Text(
                            text = "View All",
                            fontSize = 14.sp,
                            color = Color(0xFF9C27B0),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                RewardsGrid(rewards)
            }
        }
    }
}

@Composable
fun MyPointsCard(pointsInfo: PointsInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF9800),
                        Color(0xFFFFB74D)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Points",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${pointsInfo.totalPoints.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Points",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun EarnTasksGrid(tasks: List<EarnTask>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tasks.take(2).forEach { task ->
                EarnTaskCard(
                    task = task,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tasks.drop(2).forEach { task ->
                EarnTaskCard(
                    task = task,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EarnTaskCard(
    task: EarnTask,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = task.color
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Remaining badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${task.remaining} left",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon
                Icon(
                    imageVector = task.icon,
                    contentDescription = task.title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    // Title
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description
                    Text(
                        text = task.description,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Points and Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+${task.points}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "Start",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentEarningItem(earning: RecentEarning) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = earning.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = earning.timeAgo,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Text(
            text = "+${earning.points}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
    }
}

@Composable
fun RewardsGrid(rewards: List<RewardItem>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rewards.chunked(2).forEach { rowRewards ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowRewards.forEach { reward ->
                    RewardCard(
                        reward = reward,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number
                if (rowRewards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun RewardCard(
    reward: RewardItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Image Container with Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                // Placeholder for image
                // Image would go here

                // Status Badge
                if (reward.isLimited) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = Color(0xFFE53935),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Limited",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (reward.inStock) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "In Stock",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = reward.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Points and Redeem Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Points",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${reward.points.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }

                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "Redeem",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AdsRewardsScreenPreview() {
    AdsScreen()
}