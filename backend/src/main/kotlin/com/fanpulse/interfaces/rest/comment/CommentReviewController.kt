package com.fanpulse.interfaces.rest.comment

import com.fanpulse.application.dto.comment.CommentReviewDecision
import com.fanpulse.application.service.comment.CommentReviewService
import jakarta.validation.Valid
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

/** Private responses are never included in public comment endpoints or shared caches. */
@RestController
class CommentReviewController(private val reviews: CommentReviewService) {
    @GetMapping("/api/v1/comments/mine")
    fun mine(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam postId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ) = privateResponse(reviews.mine(userId, postId, page, size))

    @GetMapping("/api/v1/admin/comment-reviews")
    fun pending(@RequestParam(defaultValue = "0") page: Int, @RequestParam(defaultValue = "20") size: Int) =
        privateResponse(reviews.pending(page, size))

    @GetMapping("/api/v1/admin/comment-reviews/{id}/history")
    fun history(@PathVariable id: UUID) = privateResponse(reviews.history(id))

    @PostMapping("/api/v1/admin/comment-reviews/{id}/decision")
    fun decide(
        @PathVariable id: UUID,
        @RequestAttribute("userId") userId: UUID,
        @Valid @RequestBody request: CommentReviewDecision
    ) = privateResponse(reviews.decide(id, userId, request))

    @ExceptionHandler(ResponseStatusException::class)
    fun statusError(error: ResponseStatusException) = ResponseEntity.status(error.statusCode)
        .cacheControl(CacheControl.noStore()).body(mapOf("detail" to (error.reason ?: "Review failed")))

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun conflict() = ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CacheControl.noStore())
        .body(mapOf("detail" to "Comment changed; refresh before deciding"))

    private fun <T : Any> privateResponse(body: T): ResponseEntity<T> = ResponseEntity.ok()
        .cacheControl(CacheControl.noStore()).body(body)
}
