package com.aos.fanpulse.data.mapper

import com.aos.fanpulse.data.remote.dto.CommentListResponse as DataCommentListResponse
import com.aos.fanpulse.data.remote.dto.Comment as DataComment
import com.aos.fanpulse.data.remote.dto.CommentRequest as DataCommentRequest

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.CommentListResponse as DomainCommentListResponse
import com.aos.fanpulse.domain.model.Comment as DomainComment
import com.aos.fanpulse.domain.model.CommentRequest as DomainCommentRequest

internal fun DataComment.toDomain(): DomainComment {
    return DomainComment(
        id = this.id,
        postId = this.postId,
        userId = this.userId,
        content = this.content,
        status = this.status,
        parentCommentId = this.parentCommentId,
        createdAt = this.createdAt
    )
}

internal fun DataCommentListResponse.toDomain(): DomainCommentListResponse {
    return DomainCommentListResponse(
        content = this.content.map { it.toDomain() },
        totalElements = this.totalElements,
        page = this.page,
        size = this.size,
        totalPages = this.totalPages
    )
}

internal fun DomainCommentRequest.toData(): DataCommentRequest {
    return DataCommentRequest(
        postId = this.postId,
        content = this.content,
        parentCommentId = this.parentCommentId
    )
}

