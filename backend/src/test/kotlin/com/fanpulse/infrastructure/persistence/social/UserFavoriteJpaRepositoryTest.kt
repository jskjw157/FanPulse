package com.fanpulse.infrastructure.persistence.social

import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.social.UserFavorite
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserFavoriteJpaRepository")
class UserFavoriteJpaRepositoryTest {
    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var repository: UserFavoriteJpaRepository

    @Test
    fun `finds only the requested user's follows in newest-first order`() {
        val owner = entityManager.persist(User(email = "owner@example.com", username = "owner"))
        val other = entityManager.persist(User(email = "other@example.com", username = "other"))
        val olderArtist = entityManager.persist(Artist.create("Older", null, null, isGroup = true))
        val newerArtist = entityManager.persist(Artist.create("Newer", null, null, isGroup = false))
        val hiddenArtist = entityManager.persist(Artist.create("Hidden", null, null, isGroup = true))

        repository.save(UserFavorite.create(owner.id, olderArtist.id, LocalDateTime.parse("2026-08-11T00:00:00")))
        repository.save(UserFavorite.create(owner.id, newerArtist.id, LocalDateTime.parse("2026-08-12T00:00:00")))
        repository.save(UserFavorite.create(other.id, hiddenArtist.id, LocalDateTime.parse("2026-08-13T00:00:00")))
        entityManager.flush()
        entityManager.clear()

        val favorites = repository.findAllByUserIdOrderByCreatedAtDesc(owner.id)

        assertThat(favorites.map { it.artistId }).containsExactly(newerArtist.id, olderArtist.id)
    }
}
