package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentListResponse
import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.comment.port.CommentPort
import com.fanpulse.infrastructure.common.PaginationConverter
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class CommentQueryServiceImpl(
    private val commentPort: CommentPort
) : CommentQueryService {

    override fun getComments(postId: String, pageable: Pageable): CommentListResponse {
        logger.debug { "Getting APPROVED comments for post: $postId" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = commentPort.findByPostIdAndStatus(postId, CommentStatus.APPROVED, pageRequest)

        return CommentListResponse(
            content = pageResult.content.map { CommentResponse.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }
}
