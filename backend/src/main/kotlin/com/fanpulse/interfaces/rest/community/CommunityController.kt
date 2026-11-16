package com.fanpulse.interfaces.rest.community

import com.fanpulse.application.dto.comment.CommentListResponse
import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.application.service.comment.CommentCommandService
import com.fanpulse.application.service.comment.CommentQueryService
import com.fanpulse.application.service.community.CommunityPostPageResponse
import com.fanpulse.application.service.community.CommunityPostResponse
import com.fanpulse.application.service.community.CommunityPostState
import com.fanpulse.application.service.community.CommunityService
import com.fanpulse.application.service.community.CommunitySort
import com.fanpulse.application.service.community.CreateCommunityPostRequest
import com.fanpulse.application.service.community.UpdateCommunityPostRequest
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.interfaces.rest.common.ApiResponse
import com.fanpulse.interfaces.rest.identity.requireExpectedUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Locale
import java.util.UUID

@RestController
@RequestMapping("/api/v1/community")
class CommunityController(
    private val service: CommunityService,
    private val commentCommandService: CommentCommandService,
    private val commentQueryService: CommentQueryService
) {
    @GetMapping("/posts")
    fun getPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "LATEST") sort: String
    ): ApiResponse<CommunityPostPageResponse> = ApiResponse.success(
        service.getPosts(page, size, parseSort(sort))
    )

    @GetMapping("/posts/{postId}")
    fun getPost(@PathVariable postId: UUID): ApiResponse<CommunityPostResponse> =
        ApiResponse.success(service.getPost(postId))

    @GetMapping("/posts/{postId}/comments")
    fun getComments(
        @PathVariable postId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<CommentListResponse> {
        service.getPost(postId)
        val pageable = PageRequest.of(
            page,
            size.coerceIn(1, 100),
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return ApiResponse.success(commentQueryService.getComments(postId.toString(), pageable))
    }

    @PostMapping("/posts")
    fun createPost(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: CreateCommunityPostRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<CommunityPostResponse>> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(service.createPost(userId, request)))
    }

    @PutMapping("/posts/{postId}")
    fun updatePost(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        @RequestBody request: UpdateCommunityPostRequest,
        servletRequest: HttpServletRequest
    ): ApiResponse<CommunityPostResponse> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        return ApiResponse.success(service.updatePost(userId, postId, request))
    }

    @DeleteMapping("/posts/{postId}")
    fun deletePost(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        service.deletePost(userId, postId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/posts/{postId}/comments/{commentId}")
    fun updateComment(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        @PathVariable commentId: UUID,
        @RequestBody request: UpdateCommunityCommentRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<CommentResponse>> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        val response = commentCommandService.updateComment(commentId, userId, request.content, postId.toString())
        val status = if (response.status == CommentStatus.PENDING) HttpStatus.ACCEPTED else HttpStatus.OK
        return ResponseEntity.status(status).body(ApiResponse.success(response))
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    fun deleteComment(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        @PathVariable commentId: UUID,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        commentCommandService.deleteComment(commentId, userId, postId.toString())
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/posts/{postId}/comments")
    fun createComment(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        @RequestBody request: CreateCommunityCommentRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<CommentResponse>> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        service.getPost(postId)
        val response = commentCommandService.createComment(
            postId = postId.toString(),
            userId = userId,
            content = request.content,
            parentCommentId = request.parentCommentId
        )
        val status = if (response.status == CommentStatus.PENDING) HttpStatus.ACCEPTED else HttpStatus.CREATED
        return ResponseEntity.status(status).body(ApiResponse.success(response))
    }

    @PostMapping("/posts/{postId}/likes")
    fun like(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        servletRequest: HttpServletRequest
    ): ApiResponse<CommunityPostState> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        return ApiResponse.success(service.like(userId, postId))
    }

    @DeleteMapping("/posts/{postId}/likes")
    fun unlike(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        servletRequest: HttpServletRequest
    ): ApiResponse<CommunityPostState> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        return ApiResponse.success(service.unlike(userId, postId))
    }

    @PostMapping("/posts/{postId}/saved")
    fun save(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        servletRequest: HttpServletRequest
    ): ApiResponse<CommunityPostState> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        return ApiResponse.success(service.save(userId, postId))
    }

    @DeleteMapping("/posts/{postId}/saved")
    fun unsave(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        servletRequest: HttpServletRequest
    ): ApiResponse<CommunityPostState> {
        requireExpectedUser(servletRequest, userId, cookieWrite = true)
        return ApiResponse.success(service.unsave(userId, postId))
    }

    @GetMapping("/me/posts/{postId}/state")
    fun getState(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable postId: UUID,
        servletRequest: HttpServletRequest
    ): ApiResponse<CommunityPostState> {
        requireExpectedUser(servletRequest, userId)
        return ApiResponse.success(service.getState(userId, postId))
    }

    @GetMapping("/me/saved")
    fun getSavedPosts(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        servletRequest: HttpServletRequest
    ): ApiResponse<CommunityPostPageResponse> {
        requireExpectedUser(servletRequest, userId)
        return ApiResponse.success(service.getSavedPosts(userId, page, size))
    }

    private fun parseSort(value: String): CommunitySort = runCatching {
        CommunitySort.valueOf(value.uppercase(Locale.ROOT))
    }.getOrElse {
        throw IllegalArgumentException("지원하지 않는 커뮤니티 정렬입니다")
    }
}

data class CreateCommunityCommentRequest(
    val content: String,
    val parentCommentId: UUID? = null
)

data class UpdateCommunityCommentRequest(val content: String)
