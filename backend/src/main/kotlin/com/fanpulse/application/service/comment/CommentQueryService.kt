package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentListResponse
import org.springframework.data.domain.Pageable

interface CommentQueryService {

    fun getComments(postId: String, pageable: Pageable): CommentListResponse
}
