package com.aos.fanpulse.data.mapper

// [Data 계층 DTO] (서버 통신용)
import com.aos.fanpulse.data.remote.dto.CommentListResponse as DataCommentListResponse
import com.aos.fanpulse.data.remote.dto.Comment as DataComment
import com.aos.fanpulse.data.remote.dto.CommentRequest as DataCommentRequest

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.CommentListResponse as DomainCommentListResponse
import com.aos.fanpulse.domain.model.Comment as DomainComment
import com.aos.fanpulse.domain.model.CommentRequest as DomainCommentRequest


// ==========================================
// 1. Data(서버 응답) -> Domain(앱 모델) 방향
// ==========================================

// 1-1. 단일 댓글 변환 (리스트 변환보다 위에 선언해야 it.toDomain()이 작동합니다)
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

// 1-2. 댓글 목록 응답 변환 (페이징 정보 포함)
internal fun DataCommentListResponse.toDomain(): DomainCommentListResponse {
    return DomainCommentListResponse(
        // 여기서 it은 DataComment 타입이므로 위의 1-1번 함수가 호출됩니다.
        content = this.content.map { it.toDomain() },
        totalElements = this.totalElements,
        page = this.page,
        size = this.size,
        totalPages = this.totalPages
    )
}


// ==========================================
// 2. Domain(앱 데이터) -> Data(서버 요청) 방향
// ==========================================

// 2-1. 댓글 작성 요청 변환 (UI에서 넘어온 데이터를 서버 포맷으로 변환)
internal fun DomainCommentRequest.toData(): DataCommentRequest {
    return DataCommentRequest(
        postId = this.postId,
        content = this.content,
        parentCommentId = this.parentCommentId
    )
}

