package com.fanpulse.application.service.social

import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.identity.User
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepositoryInterface
import com.fanpulse.infrastructure.persistence.social.UserFavoriteJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "SPRING_TEST_POSTGRES", matches = "true")
@DisplayName("PostgreSQL favorite upsert concurrency")
class UserFavoriteUpsertConcurrencyIntegrationTest {
    @Autowired private lateinit var service: SocialUserService
    @Autowired private lateinit var favoriteRepository: UserFavoriteJpaRepository
    @Autowired private lateinit var userRepository: UserJpaRepositoryInterface
    @Autowired private lateinit var artistRepository: ArtistJpaRepository

    private val executor = Executors.newFixedThreadPool(2)

    @BeforeEach
    fun setUp() {
        favoriteRepository.deleteAll()
        userRepository.deleteAll()
        artistRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        executor.shutdownNow()
        executor.awaitTermination(5, TimeUnit.SECONDS)
    }

    @Test
    fun `concurrent duplicate requests create one row and one created response`() {
        val user = userRepository.saveAndFlush(
            User(email = "favorite-race@example.com", username = "favorite-race")
        )
        val artist = artistRepository.saveAndFlush(
            Artist.create("Concurrent Artist", null, null, isGroup = true)
        )
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)

        val futures = (1..2).map {
            executor.submit<FavoriteAddResult> {
                ready.countDown()
                check(start.await(10, TimeUnit.SECONDS))
                service.addFavorite(user.id, artist.id)
            }
        }
        check(ready.await(10, TimeUnit.SECONDS))
        start.countDown()
        val results = futures.map { it.get(20, TimeUnit.SECONDS) }

        assertThat(results.count { it.created }).isEqualTo(1)
        assertThat(results.map { it.favorite.id }).containsOnly(artist.id)
        assertThat(favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(user.id)).hasSize(1)
    }
}
