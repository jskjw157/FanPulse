package com.fanpulse.application.dto.comment

import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema(description = "댓글 생성 요청 (Controller)")
data class CreateCommentRequest(
    @Schema(description = "게시글 ID (MongoDB ObjectId)")
    val postId: String,

    @Schema(description = "작성자 ID")
    val userId: UUID,

    @Schema(description = "댓글 내용")
    val content: String,

    @Schema(description = "부모 댓글 ID (대댓글인 경우)")
    val parentCommentId: UUID? = null
)

@Schema(description = "댓글 목록 응답")
data class CommentListResponse(
    @Schema(description = "댓글 목록")
    val content: List<CommentResponse>,

    @Schema(description = "전체 요소 수")
    val totalElements: Long,

    @Schema(description = "현재 페이지")
    val page: Int,

    @Schema(description = "페이지 크기")
    val size: Int,

    @Schema(description = "전체 페이지 수")
    val totalPages: Int
)

@Schema(description = "댓글 생성 커맨드 (Service)")
data class CreateCommentCommand(
    @Schema(description = "게시글 ID (MongoDB ObjectId)")
    val postId: String,

    @Schema(description = "작성자 ID")
    val userId: UUID,

    @Schema(description = "댓글 내용")
    val content: String,

    @Schema(description = "부모 댓글 ID (대댓글인 경우)")
    val parentCommentId: UUID? = null
)

@Schema(description = "댓글 응답")
data class CommentResponse(
    @Schema(description = "댓글 ID")
    val id: UUID,

    @Schema(description = "게시글 ID")
    val postId: String,

    @Schema(description = "작성자 ID")
    val userId: UUID,

    @Schema(description = "댓글 내용")
    val content: String,

    @Schema(description = "댓글 상태")
    val status: CommentStatus,

    @Schema(description = "부모 댓글 ID")
    val parentCommentId: UUID?,

    @Schema(description = "작성 일시")
    val createdAt: Instant
) {
    companion object {
        fun from(comment: Comment): CommentResponse = CommentResponse(
            id = comment.id,
            postId = comment.postId,
            userId = comment.userId,
            content = comment.content,
            status = comment.status,
            parentCommentId = comment.parentCommentId,
            createdAt = comment.createdAt
        )
    }
}
