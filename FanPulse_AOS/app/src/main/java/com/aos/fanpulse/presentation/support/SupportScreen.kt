@file:OptIn(ExperimentalMaterial3Api::class)

package com.aos.fanpulse.presentation.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Data Classes
data class SupportStats(
    val averageResponseTime: String,
    val satisfactionRate: String
)

data class FAQItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: FAQCategory
)

enum class FAQCategory(val displayName: String, val icon: ImageVector) {
    ALL("전체", Icons.Default.Person),
    BOOKING("예매", Icons.Default.Person),
    PAYMENT("결제", Icons.Default.Person),
    ACCOUNT("계정", Icons.Default.Person)
}

data class InquiryItem(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val status: InquiryStatus
)

enum class InquiryStatus(val displayName: String, val color: Color) {
    ANSWERED("답변완료", Color(0xFF4CAF50)),
    PROCESSING("처리중", Color(0xFF2196F3)),
    PENDING("대기중", Color(0xFFFF9800))
}

data class NoticeItem(
    val id: String,
    val title: String,
    val date: String,
    val isNew: Boolean = false
)

enum class SupportTab {
    FAQ, INQUIRY, NOTICE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(SupportTab.FAQ) }
    var showInquiryForm by remember { mutableStateOf(false) }

    val stats = SupportStats(
        averageResponseTime = "2시간",
        satisfactionRate = "98%"
    )

    Dialog(
        onDismissRequest = onBackClick,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFAFAFA)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "고객센터",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                }

                // Stats Card
                SupportStatsCard(stats)

                // Tab Row
                SupportTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                // Content based on selected tab
                when (selectedTab) {
                    SupportTab.FAQ -> FAQContent()
                    SupportTab.INQUIRY -> InquiryContent(
                        onNewInquiryClick = { showInquiryForm = true }
                    )
                    SupportTab.NOTICE -> NoticeContent()
                }

                // Floating Action Button for Customer Service
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = { },
                        modifier = Modifier.padding(16.dp),
                        containerColor = Color(0xFFE91E63),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Customer Service",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Inquiry Form Dialog
            if (showInquiryForm) {
                InquiryFormDialog(
                    onDismiss = { showInquiryForm = false },
                    onSubmit = { showInquiryForm = false }
                )
            }
        }
    }
}

@Composable
fun SupportStatsCard(stats: SupportStats) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF9C27B0),
                        Color(0xFFE91E63)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "평균 응답 시간",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stats.averageResponseTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "문의 해결률",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stats.satisfactionRate,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SupportTabRow(
    selectedTab: SupportTab,
    onTabSelected: (SupportTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            SupportTabButton(
                text = "FAQ",
                isSelected = selectedTab == SupportTab.FAQ,
                onClick = { onTabSelected(SupportTab.FAQ) },
                modifier = Modifier.weight(1f)
            )
            SupportTabButton(
                text = "1:1 문의",
                isSelected = selectedTab == SupportTab.INQUIRY,
                onClick = { onTabSelected(SupportTab.INQUIRY) },
                modifier = Modifier.weight(1f)
            )
            SupportTabButton(
                text = "공지사항",
                isSelected = selectedTab == SupportTab.NOTICE,
                onClick = { onTabSelected(SupportTab.NOTICE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SupportTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF9C27B0) else Color.Gray,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFF9C27B0))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )
        }
    }
}

@Composable
fun FAQContent() {
    var selectedCategory by remember { mutableStateOf(FAQCategory.ALL) }
    var expandedFAQId by remember { mutableStateOf<String?>(null) }

    val faqItems = listOf(
        FAQItem(
            id = "1",
            question = "티켓 예매는 어떻게 하나요?",
            answer = "콘서트 페이지에서 원하는 공연을 선택한 후, 좌석과 날짜를 선택하여 예매할 수 있습니다. 결제는 카드, 계좌이체, 포인트 사용이 가능합니다.",
            category = FAQCategory.BOOKING
        ),
        FAQItem(
            id = "2",
            question = "예매 취소 및 환불은 어떻게 하나요?",
            answer = "예매 내역에서 취소를 원하시는 티켓을 선택하고 취소 요청을 하시면 됩니다.",
            category = FAQCategory.BOOKING
        ),
        FAQItem(
            id = "3",
            question = "결제 수단은 무엇이 있나요?",
            answer = "신용카드, 체크카드, 계좌이체, 포인트 결제가 가능합니다.",
            category = FAQCategory.PAYMENT
        ),
        FAQItem(
            id = "4",
            question = "결제 오류가 발생했어요",
            answer = "결제 오류가 발생한 경우 고객센터로 문의해주시기 바랍니다.",
            category = FAQCategory.PAYMENT
        ),
        FAQItem(
            id = "5",
            question = "회원가입은 어떻게 하나요?",
            answer = "앱 메인 화면에서 회원가입 버튼을 클릭하여 진행하실 수 있습니다.",
            category = FAQCategory.ACCOUNT
        ),
        FAQItem(
            id = "6",
            question = "비밀번호를 잊어버렸어요",
            answer = "로그인 화면에서 비밀번호 찾기를 클릭하여 재설정할 수 있습니다.",
            category = FAQCategory.ACCOUNT
        ),
        FAQItem(
            id = "7",
            question = "포인트는 어떻게 적립하나요?",
            answer = "예매, 리뷰 작성, 이벤트 참여 등을 통해 포인트를 적립할 수 있습니다.",
            category = FAQCategory.ALL
        ),
        FAQItem(
            id = "8",
            question = "포인트 유효기간이 있나요?",
            answer = "포인트는 적립일로부터 1년간 유효합니다.",
            category = FAQCategory.ALL
        )
    )

    val filteredFAQs = if (selectedCategory == FAQCategory.ALL) {
        faqItems
    } else {
        faqItems.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Category Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FAQCategory.values()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = category.displayName,
                                fontSize = 13.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF9C27B0),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color.Gray
                    )
                )
            }
        }

        // FAQ List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 80.dp
            )
        ) {
            items(filteredFAQs) { faq ->
                FAQItemCard(
                    faq = faq,
                    isExpanded = expandedFAQId == faq.id,
                    onClick = {
                        expandedFAQId = if (expandedFAQId == faq.id) null else faq.id
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "원하는 답변을 찾지 못하셨나요?",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "1:1 문의하기",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FAQItemCard(
    faq: FAQItem,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = Color(0xFFE1BEE7),
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Q",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = faq.question,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.Person else Icons.Default.Person,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.Gray
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = Color(0xFFE1BEE7),
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "A",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun InquiryContent(
    onNewInquiryClick: () -> Unit
) {
    val inquiries = listOf(
        InquiryItem(
            id = "1",
            title = "티켓 좌석 변경 문의",
            category = "예매",
            date = "2024.12.15",
            status = InquiryStatus.ANSWERED
        ),
        InquiryItem(
            id = "2",
            title = "결제 오류 문의",
            category = "결제",
            date = "2024.12.14",
            status = InquiryStatus.PROCESSING
        ),
        InquiryItem(
            id = "3",
            title = "포인트 적립 문의",
            category = "포인트",
            date = "2024.12.10",
            status = InquiryStatus.ANSWERED
        )
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // New Inquiry Button
        Button(
            onClick = onNewInquiryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0)
            ),
            shape = RoundedCornerShape(26.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "New Inquiry",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "새 문의 작성하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "문의 내역",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Inquiry List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(inquiries) { inquiry ->
                InquiryItemCard(inquiry)
            }
        }
    }
}

@Composable
fun InquiryItemCard(inquiry: InquiryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = inquiry.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Category",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = inquiry.category,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = inquiry.date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Surface(
                color = inquiry.status.color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = inquiry.status.displayName,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    color = inquiry.status.color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NoticeContent() {
    val notices = listOf(
        NoticeItem(
            id = "1",
            title = "2024년 설날 연휴 고객센터 운영 안내",
            date = "2024.12.20",
            isNew = true
        ),
        NoticeItem(
            id = "2",
            title = "앱 업데이트 안내 (v2.5.0)",
            date = "2024.12.18",
            isNew = true
        ),
        NoticeItem(
            id = "3",
            title = "개인정보 처리방침 변경 안내",
            date = "2024.12.15",
            isNew = false
        ),
        NoticeItem(
            id = "4",
            title = "서비스 점검 안내 (12/25)",
            date = "2024.12.10",
            isNew = false
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notices) { notice ->
            NoticeItemCard(notice)
        }
    }
}

@Composable
fun NoticeItemCard(notice: NoticeItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (notice.isNew) {
                    Surface(
                        color = Color(0xFFE53935),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "NEW",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = notice.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notice.date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "View",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun InquiryFormDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("예매 관련") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val categories = listOf("예매 관련", "결제 관련", "포인트 관련", "계정 관련", "기타")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1:1 문의하기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // Category Dropdown
                    Text(
                        text = "문의 유형",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.Person else Icons.Default.Person,
                                    contentDescription = "Dropdown"
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF9C27B0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title Field
                    Text(
                        text = "제목",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("문의 제목을 입력해주세요", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9C27B0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Content Field
                    Text(
                        text = "문의 내용",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { if (it.length <= 500) content = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { Text("문의 내용을 상세히 입력해주세요", fontSize = 14.sp) },
                        maxLines = 10,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9C27B0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                    Text(
                        text = "최대 500자",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Field
                    Text(
                        text = "이메일",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("답변 받으실 이메일", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9C27B0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Submit Button
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text(
                            text = "문의 제출하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SupportScreenPreview() {
    SupportScreen()
}