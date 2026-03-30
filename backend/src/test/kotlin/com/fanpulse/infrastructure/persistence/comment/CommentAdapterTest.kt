package com.fanpulse.infrastructure.persistence.comment

import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.Sort
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.util.*

@DataJpaTest
@ActiveProfiles("test")
@Import(CommentAdapter::class)
@DisplayName("CommentAdapter Repository 테스트")
class CommentAdapterTest {

    @Autowired
    private lateinit var adapter: CommentAdapter

    @Autowired
    private lateinit var repository: CommentJpaRepository

    private val postId = "507f1f77bcf86cd799439011"
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Nested
    @DisplayName("save")
    inner class Save {

        @Test
        @DisplayName("댓글을 저장하고 ID로 조회할 수 있어야 한다")
        fun `should save and retrieve by id`() {
            val comment = Comment.create(postId, userId, "테스트 댓글")

            val saved = adapter.save(comment)

            val found = adapter.findById(saved.id)
            assertNotNull(found)
            assertEquals("테스트 댓글", found!!.content)
            assertEquals(CommentStatus.PENDING, found.status)
        }
    }

    @Nested
    @DisplayName("findById")
    inner class FindById {

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 null을 반환해야 한다")
        fun `should return null for non-existent id`() {
            val result = adapter.findById(UUID.randomUUID())
            assertNull(result)
        }
    }

    @Nested
    @DisplayName("findByPostIdAndStatus")
    inner class FindByPostIdAndStatus {

        @Test
        @DisplayName("APPROVED 상태 댓글만 조회되어야 한다")
        fun `should return only comments with matching status`() {
            // 3개 댓글 생성: APPROVED, BLOCKED, PENDING
            val approved = Comment.create(postId, userId, "승인 댓글").also { it.approve() }
            val blocked = Comment.create(postId, userId, "차단 댓글").also { it.block("스팸") }
            val pending = Comment.create(postId, userId, "보류 댓글")

            repository.saveAll(listOf(approved, blocked, pending))

            val pageRequest = PageRequest(page = 0, size = 10, sort = Sort("createdAt", Sort.Direction.DESC))
            val result = adapter.findByPostIdAndStatus(postId, CommentStatus.APPROVED, pageRequest)

            assertEquals(1, result.totalElements)
            assertEquals("승인 댓글", result.content[0].content)
        }

        @Test
        @DisplayName("페이지네이션이 정상 동작해야 한다")
        fun `should paginate correctly`() {
            // 5개 APPROVED 댓글 생성
            repeat(5) { i ->
                val comment = Comment.create(postId, userId, "댓글 $i").also { it.approve() }
                repository.save(comment)
            }

            val page0 = adapter.findByPostIdAndStatus(
                postId, CommentStatus.APPROVED,
                PageRequest(page = 0, size = 2, sort = Sort("createdAt", Sort.Direction.DESC))
            )

            assertEquals(5, page0.totalElements)
            assertEquals(2, page0.content.size)
            assertEquals(3, page0.totalPages)
            assertTrue(page0.hasNext)
        }
    }
}
