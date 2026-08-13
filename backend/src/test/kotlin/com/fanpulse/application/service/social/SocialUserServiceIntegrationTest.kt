package com.fanpulse.application.service.social

import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.social.Notification
import com.fanpulse.domain.social.UserFavorite
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepositoryInterface
import com.fanpulse.infrastructure.persistence.social.NotificationJpaRepository
import com.fanpulse.infrastructure.persistence.social.UserFavoriteJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@Import(SocialUserServiceImpl::class)
@DisplayName("SocialUserService")
class SocialUserServiceIntegrationTest {
    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var service: SocialUserService

    @Autowired
    private lateinit var userRepository: UserJpaRepositoryInterface

    @Autowired
    private lateinit var artistRepository: ArtistJpaRepository

    @Autowired
    private lateinit var favoriteRepository: UserFavoriteJpaRepository

    @Autowired
    private lateinit var notificationRepository: NotificationJpaRepository

    @Test
    fun `returns the authenticated user's real favorite artist summaries in follow order`() {
        val owner = userRepository.save(User(email = "service-owner@example.com", username = "service-owner"))
        val other = userRepository.save(User(email = "service-other@example.com", username = "service-other"))
        val older = artistRepository.save(Artist.create("Older Artist", "Older", "Agency A", isGroup = true))
        older.updateProfileImage("https://cdn.example.com/older.jpg")
        val newer = artistRepository.save(Artist.create("Newer Artist", null, null, isGroup = false))
        val hidden = artistRepository.save(Artist.create("Hidden Artist", null, null, isGroup = true))
        favoriteRepository.save(UserFavorite.create(owner.id, older.id, LocalDateTime.parse("2026-08-11T00:00:00")))
        favoriteRepository.save(UserFavorite.create(owner.id, newer.id, LocalDateTime.parse("2026-08-12T00:00:00")))
        favoriteRepository.save(UserFavorite.create(other.id, hidden.id, LocalDateTime.parse("2026-08-13T00:00:00")))
        entityManager.flush()
        entityManager.clear()

        val favorites = service.getFavorites(owner.id)

        assertThat(favorites.map { it.id }).containsExactly(newer.id, older.id)
        val olderFavorite = favorites.last()
        assertThat(olderFavorite.name).isEqualTo("Older Artist")
        assertThat(olderFavorite.englishName).isEqualTo("Older")
        assertThat(olderFavorite.agency).isEqualTo("Agency A")
        assertThat(olderFavorite.profileImageUrl).isEqualTo("https://cdn.example.com/older.jpg")
        assertThat(olderFavorite.isGroup).isTrue()
        assertThat(olderFavorite.followedAt).isEqualTo(LocalDateTime.parse("2026-08-11T00:00:00"))
    }

    @Test
    fun `adding the same favorite twice is idempotent`() {
        val owner = userRepository.save(User(email = "idempotent-owner@example.com", username = "idempotent-owner"))
        val artist = artistRepository.save(Artist.create("Idempotent Artist", null, null, isGroup = true))

        service.addFavorite(owner.id, artist.id)
        service.addFavorite(owner.id, artist.id)

        assertThat(favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(owner.id)).hasSize(1)
    }

    @Test
    fun `a user cannot mark another user's notification read`() {
        val owner = userRepository.save(User(email = "notice-a@example.com", username = "notice-a"))
        val other = userRepository.save(User(email = "notice-b@example.com", username = "notice-b"))
        val notification = notificationRepository.save(
            Notification.create(other.id, message = "private", type = "NEWS")
        )

        org.assertj.core.api.Assertions.assertThatThrownBy {
            service.markNotificationRead(owner.id, notification.id)
        }.isInstanceOf(NoSuchElementException::class.java)
        assertThat(notificationRepository.findById(notification.id).orElseThrow().isRead).isFalse()
    }
}
