package com.aos.fanpulse.domain.model

// 댓글 목록 조회 응답 (페이징)
data class CommentListResponse(
    val content: List<Comment>,
    val totalElements: Int,
    val page: Int,
    val size: Int,
    val totalPages: Int
)

// 개별 댓글 정보 (조회 및 작성 응답 공통)
data class Comment(
    val id: String,                 // 댓글 ID (UUID)
    val postId: String,             // 게시글 ID (New - N, Post - P, VOD - V)
    val userId: String,             // 작성자 ID
    val content: String,            // 댓글 내용
    val status: String,             // 댓글 상태 (예: "APPROVED", "HIDDEN")
    val parentCommentId: String?,   // 부모 댓글 ID (대댓글인 경우)
    val createdAt: String           // 생성 일시
)

// 댓글 작성 요청 바디 (Request Body)
data class CommentRequest(
    val postId: String,
    val content: String,
    val parentCommentId: String? = null
)