package com.fanpulse.infrastructure.persistence.social

import com.fanpulse.domain.identity.User
import com.fanpulse.domain.social.Notification
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
@DisplayName("NotificationJpaRepository")
class NotificationJpaRepositoryTest {
    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var repository: NotificationJpaRepository

    @Test
    fun `lists only an owner's unread notifications newest first`() {
        val owner = entityManager.persist(User(email = "notice-owner@example.com", username = "notice-owner"))
        val other = entityManager.persist(User(email = "notice-other@example.com", username = "notice-other"))
        val oldUnread = repository.save(notification(owner, "old", false, "2026-08-11T00:00:00"))
        val newUnread = repository.save(notification(owner, "new", false, "2026-08-13T00:00:00"))
        repository.save(notification(owner, "read", true, "2026-08-12T00:00:00"))
        repository.save(notification(other, "hidden", false, "2026-08-14T00:00:00"))
        entityManager.flush()
        entityManager.clear()

        val notifications = repository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(owner.id)

        assertThat(notifications.map { it.id }).containsExactly(newUnread.id, oldUnread.id)
    }

    private fun notification(user: User, title: String, read: Boolean, createdAt: String): Notification =
        Notification.create(
            userId = user.id,
            type = "NEWS",
            message = "$title message",
            isRead = read,
            createdAt = LocalDateTime.parse(createdAt)
        )
}
