package com.aos.fanpulse.presentation.community

data class FeedInfo(
    val feedId: Long,                           // 게시글의 고유 식별자 (DB PK)

    // 작성자 정보
    val userName: String,                       // 예: "Blink_Girl"
    val userProfileImageUrl: Int,               // 프로필 이미지 URL
    val userGrade: Boolean,                     // 사용자 등급
    val userFanTag: String,                     // 예: "BLACKPINK"
    val createdDate: String,                    // 예: "5시간 전" (서버에서는 Long/Date로 받고 UI용으로 변환하는 것을 추천)

    // 게시글 내용
    val contentText: String,                    // 본문 내용
    val contentImageUrl: String? = null,        // 본문 이미지 URL (이미지가 없을 수도 있으므로 String?)

    // 인터랙션 통계 및 상태
    val likeCount: Int = 0,                     // 예: 856
    val isLikedByMe: Boolean = false,           // 현재 보고 있는 사용자가 좋아요를 눌렀는지 여부
    val commentCount: Int = 0,                  // 예: 67
    val shareCount: Int = 0,                    // 예: 34
    val isBookmarked: Boolean = false           // 북마크(저장) 여부
)