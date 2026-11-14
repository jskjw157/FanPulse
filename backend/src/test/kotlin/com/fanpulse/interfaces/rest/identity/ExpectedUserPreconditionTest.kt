package com.fanpulse.interfaces.rest.identity

import com.fanpulse.application.dto.identity.*
import com.fanpulse.application.service.identity.UserService
import com.fanpulse.application.service.social.SocialUserService
import com.fanpulse.infrastructure.security.JwtAuthenticationFilter
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.interfaces.rest.GlobalExceptionHandler
import com.fanpulse.interfaces.rest.social.SocialUserController
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.time.Instant
import java.util.UUID

/** Real JWT filter/principal + MVC binding/advice, without a database or Spring application. */
class ExpectedUserPreconditionTest {
    private val alice = UUID.fromString("11111111-1111-4111-8111-111111111111")
    private val bob = UUID.fromString("22222222-2222-4222-8222-222222222222")
    private val tokens = JwtTokenProvider("test-only-expected-user-precondition-key-318", 3_600_000L, 604_800_000L)
    private lateinit var users: UserService
    private lateinit var social: SocialUserService
    private lateinit var mvc: MockMvc

    @BeforeEach
    fun setup() {
        users = mockk()
        social = mockk()
        mvc = MockMvcBuilders.standaloneSetup(MeController(users), SocialUserController(social))
            .setControllerAdvice(GlobalExceptionHandler())
            .addFilters<StandaloneMockMvcBuilder>(JwtAuthenticationFilter(tokens))
            .build()
    }

    @AfterEach
    fun clearPrincipal() = SecurityContextHolder.clearContext()

    @Test
    fun `stale Alice tab with Bob cookie fails before any mutation service is called`() {
        for (path in listOf("/me", "/me/settings")) {
            for (expected in listOf(alice.toString(), "", "not-a-uuid")) {
                mvc.perform(patch("/api/v1$path")
                    .cookie(cookie(bob))
                    .header("X-Expected-User-Id", expected)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body(path)))
                    .andExpect(status().isPreconditionFailed())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.errorCode").value("EXPECTED_USER_MISMATCH"))
                    .andExpect(jsonPath("$.userId").doesNotExist())
            }
        }
        verify { users wasNot Called; social wasNot Called }
    }

    @Test
    fun `duplicate assertions cannot hide a conflicting tab identity`() {
        mvc.perform(patch("/api/v1/me").cookie(cookie(bob))
            .header("X-Expected-User-Id", bob.toString(), alice.toString())
            .contentType(MediaType.APPLICATION_JSON).content(body("/me")))
            .andExpect(status().isPreconditionFailed())
        verify { users wasNot Called }
    }

    @Test
    fun `cookie writes without the assertion fail closed instead of silently using Bob`() {
        for (path in listOf("/me", "/me/settings")) {
            mvc.perform(patch("/api/v1$path").cookie(cookie(bob))
                .contentType(MediaType.APPLICATION_JSON).content(body(path)))
                .andExpect(status().`is`(428))
                .andExpect(jsonPath("$.errorCode").value("EXPECTED_USER_REQUIRED"))
        }
        verify { users wasNot Called }
    }

    @Test
    fun `matching cookie assertions reach the original mutation services once for the principal`() {
        stubWrites(bob)
        for (path in listOf("/me", "/me/settings")) {
            mvc.perform(patch("/api/v1$path").cookie(cookie(bob))
                .header("X-Expected-User-Id", bob.toString())
                .contentType(MediaType.APPLICATION_JSON).content(body(path)))
                .andExpect(status().isOk())
        }
        verify(exactly = 1) { users.updateProfile(bob, UpdateProfileRequest("Changed_318")) }
        verify(exactly = 1) { users.updateSettings(bob, UpdateSettingsRequest(language = "en")) }
        verify(exactly = 0) { users.updateProfile(alice, any()); users.updateSettings(alice, any()) }
    }

    @Test
    fun `legacy bearer writes remain compatible and bearer identity takes precedence over cookies`() {
        stubWrites(alice)
        for (path in listOf("/me", "/me/settings")) {
            mvc.perform(patch("/api/v1$path").cookie(cookie(bob))
                .header("Authorization", "Bearer ${tokens.generateAccessToken(alice)}")
                .contentType(MediaType.APPLICATION_JSON).content(body(path)))
                .andExpect(status().isOk())
        }
        verify(exactly = 1) { users.updateProfile(alice, any()); users.updateSettings(alice, any()) }
    }

    @Test
    fun `a supplied bearer assertion is enforced too`() {
        mvc.perform(patch("/api/v1/me").header("Authorization", "Bearer ${tokens.generateAccessToken(bob)}")
            .header("X-Expected-User-Id", alice.toString())
            .contentType(MediaType.APPLICATION_JSON).content(body("/me")))
            .andExpect(status().isPreconditionFailed())
        verify { users wasNot Called }
    }

    @Test
    fun `private reads cannot fetch Bob settings profile notifications or count into Alice scope`() {
        for (path in listOf("/me", "/me/settings", "/users/me/notifications", "/users/me/notifications/page", "/users/me/notifications/unread-count")) {
            mvc.perform(get("/api/v1$path").cookie(cookie(bob)).header("X-Expected-User-Id", alice.toString()))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.errorCode").value("EXPECTED_USER_MISMATCH"))
                .andExpect(jsonPath("$.data").doesNotExist())
        }
        verify { users wasNot Called; social wasNot Called }
    }

    @Test
    fun `notification write assertions reject stale mark-all and individual reads before service entry`() {
        for (path in listOf("read-all", "33333333-3333-4333-8333-333333333333/read")) {
            mvc.perform(patch("/api/v1/users/me/notifications/$path").cookie(cookie(bob))
                .header("X-Expected-User-Id", alice.toString()))
                .andExpect(status().isPreconditionFailed())
        }
        verify { social wasNot Called }
    }

    @Test
    fun `legacy reads still work without assertions`() {
        every { users.getSettings(bob) } returns UserSettingsResponse("light", "en", true, Instant.EPOCH)
        every { social.getUnreadNotificationCount(bob) } returns 3L
        mvc.perform(get("/api/v1/me/settings").cookie(cookie(bob))).andExpect(status().isOk())
        mvc.perform(get("/api/v1/users/me/notifications/unread-count").cookie(cookie(bob)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.unreadCount").value(3))
    }

    private fun stubWrites(id: UUID) {
        every { users.updateProfile(id, any()) } returns UserResponse(id, "test@example.com", "Changed_318", false, Instant.EPOCH)
        every { users.updateSettings(id, any()) } returns UserSettingsResponse("light", "en", true, Instant.EPOCH)
    }

    private fun cookie(id: UUID) = Cookie("fanpulse_access_token", tokens.generateAccessToken(id))
    private fun body(path: String) = if (path == "/me") """{"username":"Changed_318"}""" else """{"language":"en"}"""
}
