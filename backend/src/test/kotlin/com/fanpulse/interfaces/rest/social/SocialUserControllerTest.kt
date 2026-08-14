package com.fanpulse.interfaces.rest.social

import com.fanpulse.application.service.social.FavoriteArtistResponse
import com.fanpulse.application.service.social.FavoriteAddResult
import com.fanpulse.application.service.social.NotificationResponse
import com.fanpulse.application.service.social.SocialUserService
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.fanpulse.interfaces.rest.GlobalExceptionHandler
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(SocialUserController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
@DisplayName("SocialUserController")
class SocialUserControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @MockkBean lateinit var service: SocialUserService
    @MockkBean lateinit var jwtTokenProvider: JwtTokenProvider

    private val userId = UUID.randomUUID()
    private val artistId = UUID.randomUUID()
    private val notificationId = UUID.randomUUID()

    @Test
    @WithMockUser
    fun `lists only the request attribute user's favorites`() {
        every { service.getFavorites(userId) } returns listOf(favorite())

        mockMvc.get("/api/v1/users/me/favorites") { requestAttr("userId", userId) }
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[0].id") { value(artistId.toString()) }
                jsonPath("$.data[0].name") { value("API Artist") }
            }
    }

    @Test
    @WithMockUser
    fun `returns 201 for a newly added favorite and removes it`() {
        every { service.addFavorite(userId, artistId) } returns FavoriteAddResult(favorite(), created = true)
        every { service.removeFavorite(userId, artistId) } returns Unit

        mockMvc.post("/api/v1/users/me/favorites/$artistId") { requestAttr("userId", userId) }
            .andExpect { status { isCreated() } }
        mockMvc.delete("/api/v1/users/me/favorites/$artistId") { requestAttr("userId", userId) }
            .andExpect { status { isNoContent() } }
    }

    @Test
    @WithMockUser
    fun `returns 200 for an already existing favorite`() {
        every { service.addFavorite(userId, artistId) } returns FavoriteAddResult(favorite(), created = false)

        mockMvc.post("/api/v1/users/me/favorites/$artistId") { requestAttr("userId", userId) }
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(artistId.toString()) }
            }
    }

    @Test
    @WithMockUser
    fun `marks only the authenticated user's notification as read`() {
        every { service.markNotificationRead(userId, notificationId) } returns notification(read = true)

        mockMvc.patch("/api/v1/users/me/notifications/$notificationId/read") {
            requestAttr("userId", userId)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.isRead") { value(true) }
        }
        verify(exactly = 1) { service.markNotificationRead(userId, notificationId) }
    }

    @Test
    @WithMockUser
    fun `marks all of the authenticated user's notifications read`() {
        every { service.markAllNotificationsRead(userId) } returns 3

        mockMvc.patch("/api/v1/users/me/notifications/read-all") { requestAttr("userId", userId) }
            .andExpect {
                status { isOk() }
                jsonPath("$.data.updated") { value(3) }
            }
    }

    @Test
    fun `rejects unauthenticated access`() {
        mockMvc.get("/api/v1/users/me/favorites")
            .andExpect { status { isForbidden() } }
    }

    private fun favorite() = FavoriteArtistResponse(
        id = artistId,
        name = "API Artist",
        englishName = null,
        agency = null,
        profileImageUrl = null,
        isGroup = true,
        followedAt = LocalDateTime.parse("2026-08-13T12:00:00")
    )

    private fun notification(read: Boolean) = NotificationResponse(
        id = notificationId,
        type = "NEWS",
        message = "실제 알림",
        isRead = read,
        createdAt = LocalDateTime.parse("2026-08-13T12:00:00")
    )
}
