package com.fanpulse.interfaces.rest.comment

import com.fanpulse.application.dto.comment.CommentListResponse
import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.application.dto.comment.CreateCommentRequest
import com.fanpulse.application.service.comment.CommentCommandService
import com.fanpulse.application.service.comment.CommentQueryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comments", description = "댓글 생성 및 조회 (AI 필터링 적용)")
class CommentController(
    private val commandService: CommentCommandService,
    private val queryService: CommentQueryService
) {

    @PostMapping
    @Operation(
        summary = "댓글 생성",
        description = "댓글을 생성하고 AI 필터링을 적용합니다. 결과에 따라 APPROVED/BLOCKED/PENDING 상태가 결정됩니다."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "댓글 생성 성공",
            content = [Content(schema = Schema(implementation = CommentResponse::class))]
        ),
        ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 내용 등)"),
        ApiResponse(responseCode = "401", description = "인증 필요")
    )
    fun createComment(
        @RequestBody request: CreateCommentRequest
    ): ResponseEntity<CommentResponse> {
        logger.debug { "POST /api/v1/comments for post=${request.postId}" }
        val response = commandService.createComment(
            postId = request.postId,
            userId = request.userId,
            content = request.content,
            parentCommentId = request.parentCommentId
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "댓글 목록 조회",
        description = "게시글의 APPROVED 상태 댓글을 페이지네이션으로 조회합니다."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "댓글 목록 조회 성공",
            content = [Content(schema = Schema(implementation = CommentListResponse::class))]
        ),
        ApiResponse(responseCode = "400", description = "postId 누락")
    )
    fun getComments(
        @Parameter(description = "게시글 ID (필수)")
        @RequestParam postId: String,

        @Parameter(description = "페이지 번호 (0-based)")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "페이지 크기")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<CommentListResponse> {
        logger.debug { "GET /api/v1/comments?postId=$postId&page=$page&size=$size" }
        val pageable = PageRequest.of(page, size.coerceIn(1, 100), Sort.by(Sort.Direction.DESC, "createdAt"))
        val response = queryService.getComments(postId, pageable)
        return ResponseEntity.ok(response)
    }
}
