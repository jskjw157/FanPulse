package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentListResponse
import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.comment.port.CommentPort
import com.fanpulse.infrastructure.common.PaginationConverter
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepository
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 도메인 포트를 통해 APPROVED 댓글을 페이징 조회한다.
 */
@Service
@Transactional(readOnly = true)
class CommentQueryServiceImpl(
    private val commentPort: CommentPort,
    private val userRepository: UserJpaRepository
) : CommentQueryService {

    override fun getComments(postId: String, pageable: Pageable): CommentListResponse {
        logger.debug { "Getting APPROVED comments for post: $postId" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = commentPort.findByPostIdAndStatus(postId, CommentStatus.APPROVED, pageRequest)
        val authorNames = userRepository.findAllByIds(pageResult.content.map { it.userId }.distinct())
            .associate { it.id to it.username }

        return CommentListResponse(
            content = pageResult.content.map { CommentResponse.from(it, authorNames[it.userId]) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }
}
