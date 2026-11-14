package com.fanpulse.interfaces.rest.social

import com.fanpulse.application.service.social.FavoriteArtistResponse
import com.fanpulse.application.service.social.NotificationPageResponse
import com.fanpulse.application.service.social.NotificationResponse
import com.fanpulse.application.service.social.SocialUserService
import com.fanpulse.interfaces.rest.common.ApiResponse
import com.fanpulse.interfaces.rest.identity.requireExpectedUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users/me")
class SocialUserController(private val service: SocialUserService) {

    @GetMapping("/favorites")
    fun favorites(@RequestAttribute("userId") userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(service.getFavorites(userId)))

    @PostMapping("/favorites/{artistId}")
    fun addFavorite(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable artistId: UUID
    ): ResponseEntity<ApiResponse<FavoriteArtistResponse>> {
        val result = service.addFavorite(userId, artistId)
        val status = if (result.created) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity.status(status).body(ApiResponse.success(result.favorite))
    }

    @DeleteMapping("/favorites/{artistId}")
    fun removeFavorite(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable artistId: UUID
    ): ResponseEntity<Void> {
        service.removeFavorite(userId, artistId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/notifications")
    fun notifications(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "false") unreadOnly: Boolean,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<List<NotificationResponse>>> {
        requireExpectedUser(request, userId)
        return ResponseEntity.ok(ApiResponse.success(service.getNotifications(userId, unreadOnly)))
    }

    // Keep the legacy array endpoint intact for existing web/mobile clients.
    @GetMapping("/notifications/page")
    fun notificationPage(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "false") unreadOnly: Boolean,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<NotificationPageResponse>> {
        requireExpectedUser(request, userId)
        return ResponseEntity.ok(ApiResponse.success(service.getNotificationPage(userId, unreadOnly, page, size)))
    }

    @GetMapping("/notifications/unread-count")
    fun unreadCount(@RequestAttribute("userId") userId: UUID, request: HttpServletRequest): ResponseEntity<ApiResponse<Map<String, Long>>> {
        requireExpectedUser(request, userId)
        return ResponseEntity.ok(ApiResponse.success(mapOf("unreadCount" to service.getUnreadNotificationCount(userId))))
    }

    @PatchMapping("/notifications/{notificationId}/read")
    fun readNotification(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable notificationId: UUID,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<NotificationResponse>> {
        requireExpectedUser(request, userId)
        return ResponseEntity.ok(ApiResponse.success(service.markNotificationRead(userId, notificationId)))
    }

    @PatchMapping("/notifications/read-all")
    fun readAll(@RequestAttribute("userId") userId: UUID, request: HttpServletRequest): ResponseEntity<ApiResponse<Map<String, Int>>> {
        requireExpectedUser(request, userId)
        return ResponseEntity.ok(ApiResponse.success(mapOf("updated" to service.markAllNotificationsRead(userId))))
    }
}
