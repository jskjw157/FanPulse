package com.fanpulse.infrastructure.persistence.community

import com.fanpulse.domain.community.CommunityLike
import com.fanpulse.domain.community.CommunityPost
import com.fanpulse.domain.community.CommunityPostStatus
import com.fanpulse.domain.community.CommunitySavedPost
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CommunityPostJpaRepository : JpaRepository<CommunityPost, UUID> {
    fun findByStatusOrderByCreatedAtDesc(status: CommunityPostStatus, pageable: Pageable): Page<CommunityPost>

    @Query(
        value = "SELECT * FROM community_posts WHERE id = :id AND status = 'PUBLISHED' FOR UPDATE",
        nativeQuery = true
    )
    fun findPublishedByIdForUpdate(@Param("id") id: UUID): CommunityPost?

    @Query(
        value = "SELECT * FROM community_posts WHERE id = :id FOR UPDATE",
        nativeQuery = true
    )
    fun findByIdForUpdate(@Param("id") id: UUID): CommunityPost?

    @Query(
        value = """
            SELECT p.*
            FROM community_posts p
            LEFT JOIN (
                SELECT target_id, COUNT(*) AS like_count
                FROM likes
                WHERE target_type = 'POST'
                GROUP BY target_id
            ) l ON l.target_id = p.id
            LEFT JOIN (
                SELECT post_id, COUNT(*) AS comment_count
                FROM comments
                WHERE status = 'APPROVED'
                GROUP BY post_id
            ) c ON c.post_id = CAST(p.id AS VARCHAR)
            WHERE p.status = 'PUBLISHED'
            ORDER BY COALESCE(l.like_count, 0) DESC,
                     COALESCE(c.comment_count, 0) DESC,
                     p.created_at DESC,
                     p.id DESC
        """,
        countQuery = "SELECT COUNT(*) FROM community_posts WHERE status = 'PUBLISHED'",
        nativeQuery = true
    )
    fun findPopular(pageable: Pageable): Page<CommunityPost>
}

interface CommunityLikeJpaRepository : JpaRepository<CommunityLike, UUID> {
    fun existsByUserIdAndTargetTypeAndTargetId(userId: UUID, targetType: String, targetId: UUID): Boolean
    fun countByTargetTypeAndTargetId(targetType: String, targetId: UUID): Long

    @Query(
        """
        SELECT l.targetId AS targetId, COUNT(l.id) AS total
        FROM CommunityLike l
        WHERE l.targetType = :targetType AND l.targetId IN :targetIds
        GROUP BY l.targetId
        """
    )
    fun countGroupedByTargetId(
        @Param("targetType") targetType: String,
        @Param("targetIds") targetIds: Collection<UUID>
    ): List<CommunityTargetCount>

    fun deleteByUserIdAndTargetTypeAndTargetId(userId: UUID, targetType: String, targetId: UUID): Long
}

interface CommunityTargetCount {
    fun getTargetId(): UUID
    fun getTotal(): Long
}

interface CommunitySavedPostJpaRepository : JpaRepository<CommunitySavedPost, UUID> {
    fun existsByUserIdAndPostId(userId: UUID, postId: UUID): Boolean
    fun deleteByUserIdAndPostId(userId: UUID, postId: UUID): Long
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<CommunitySavedPost>
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<CommunitySavedPost>

    @Query(
        value = """
            SELECT saved
            FROM CommunitySavedPost saved
            WHERE saved.userId = :userId
              AND EXISTS (
                  SELECT post.id
                  FROM CommunityPost post
                  WHERE post.id = saved.postId
                    AND post.status = :status
              )
            ORDER BY saved.createdAt DESC
        """,
        countQuery = """
            SELECT COUNT(saved)
            FROM CommunitySavedPost saved
            WHERE saved.userId = :userId
              AND EXISTS (
                  SELECT post.id
                  FROM CommunityPost post
                  WHERE post.id = saved.postId
                    AND post.status = :status
              )
        """
    )
    fun findPublishedByUserId(
        @Param("userId") userId: UUID,
        @Param("status") status: CommunityPostStatus,
        pageable: Pageable
    ): Page<CommunitySavedPost>
}
