package com.fanpulse.interfaces.rest.social

import com.fanpulse.application.service.social.FavoriteArtistResponse
import com.fanpulse.application.service.social.NotificationResponse
import com.fanpulse.application.service.social.SocialUserService
import com.fanpulse.interfaces.rest.common.ApiResponse
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
        @RequestParam(defaultValue = "false") unreadOnly: Boolean
    ) = ResponseEntity.ok(ApiResponse.success(service.getNotifications(userId, unreadOnly)))

    @PatchMapping("/notifications/{notificationId}/read")
    fun readNotification(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable notificationId: UUID
    ): ResponseEntity<ApiResponse<NotificationResponse>> =
        ResponseEntity.ok(ApiResponse.success(service.markNotificationRead(userId, notificationId)))

    @PatchMapping("/notifications/read-all")
    fun readAll(@RequestAttribute("userId") userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(mapOf("updated" to service.markAllNotificationsRead(userId))))
}
