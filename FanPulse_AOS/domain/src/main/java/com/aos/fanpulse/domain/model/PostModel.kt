package com.aos.fanpulse.domain.model

data class Post(
    val id: String,                 // 게시글 고유 ID
    val author: User,               // 작성자 정보
    val targetArtist: String,       // 선택한 아티스트 카테고리 (게시글 작성 화면 참고)
    val content: String,            // 게시글 본문 (최대 500자 제한)
    val imageUrls: List<String>,    // 첨부된 이미지 URL 목록 (최대 5장)
    val tags: List<String>,         // 태그 목록 (# 제외한 텍스트, 최대 5개)

    val createdAt: Long,            // 작성 시간 (Timestamp 형식. UI에서 "2시간 전"으로 변환해서 보여줌)

    // 반응 수치 (카운트)
    val likeCount: Int,             // 좋아요 수 (예: 1234)
    val commentCount: Int,          // 댓글 수 (예: 89)
    val shareCount: Int,            // 공유 수 (예: 45)

    // 현재 로그인한 사용자의 상태
    val isLikedByMe: Boolean,       // 내가 좋아요를 눌렀는지 여부 (하트 색상 변경용)
    val isBookmarkedByMe: Boolean   // 내가 북마크(저장) 했는지 여부
)

class User (
    val id: String,
    val profileImageUrl: String?,
    val nickname: String,
    val fandom: String?
)