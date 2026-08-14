package com.fanpulse.application.service.community

import com.fanpulse.domain.ai.port.ContentModerationPort
import com.fanpulse.domain.community.CommunityPost
import com.fanpulse.domain.community.CommunityPostStatus
import com.fanpulse.domain.community.CommunityLike
import com.fanpulse.domain.community.CommunitySavedPost
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.infrastructure.persistence.comment.CommentJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunityLikeJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunitySavedPostJpaRepository
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepositoryInterface
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class CommunityServiceImpl(
    private val users: UserJpaRepositoryInterface,
    private val artists: ArtistJpaRepository,
    private val posts: CommunityPostJpaRepository,
    private val likes: CommunityLikeJpaRepository,
    private val savedPosts: CommunitySavedPostJpaRepository,
    private val comments: CommentJpaRepository,
    private val moderation: ContentModerationPort
) : CommunityService {

    @Transactional
    override fun createPost(userId: UUID, request: CreateCommunityPostRequest): CommunityPostResponse {
        users.findById(userId).orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
        val artist = request.artistId?.let { artistId ->
            artists.findById(artistId)
                .filter { it.active }
                .orElseThrow { NoSuchElementException("활성 아티스트를 찾을 수 없습니다") }
        }
        val content = request.content.trim()
        require(content.isNotEmpty()) { "게시글 내용은 비어 있을 수 없습니다" }

        val moderationResult = try {
            moderation.checkContent(content)
        } catch (exception: Exception) {
            throw CommunityModerationUnavailableException(
                "모더레이션 서비스를 사용할 수 없어 게시글을 저장하지 않았습니다",
                exception
            )
        }
        val moderationModel = moderationResult.modelUsed.trim().lowercase()
        if (
            moderationResult.error != null ||
            moderationModel == "fallback" ||
            moderationModel == "noop"
        ) {
            throw CommunityModerationUnavailableException(
                "모더레이션 서비스를 사용할 수 없어 게시글을 저장하지 않았습니다"
            )
        }
        require(!moderationResult.isFlagged && !moderationResult.action.equals("block", ignoreCase = true)) {
            "허용되지 않는 게시글 내용입니다"
        }

        val post = posts.save(CommunityPost.create(userId, artist?.id, content))
        return toResponse(post)
    }

    override fun getPosts(page: Int, size: Int, sort: CommunitySort): CommunityPostPageResponse {
        val pageable = PageRequest.of(validPage(page), validSize(size))
        val result = when (sort) {
            CommunitySort.LATEST -> posts.findByStatusOrderByCreatedAtDesc(CommunityPostStatus.PUBLISHED, pageable)
            CommunitySort.POPULAR -> posts.findPopular(pageable)
        }
        return toPageResponse(result)
    }

    override fun getPost(postId: UUID): CommunityPostResponse =
        toResponse(findPublishedPost(postId))

    @Transactional
    override fun like(userId: UUID, postId: UUID): CommunityPostState {
        requireUser(userId)
        lockPublishedPost(postId)
        if (!likes.existsByUserIdAndTargetTypeAndTargetId(userId, POST_TARGET_TYPE, postId)) {
            likes.save(CommunityLike(userId = userId, targetType = POST_TARGET_TYPE, targetId = postId))
        }
        return getState(userId, postId)
    }

    @Transactional
    override fun unlike(userId: UUID, postId: UUID): CommunityPostState {
        requireUser(userId)
        lockPublishedPost(postId)
        likes.deleteByUserIdAndTargetTypeAndTargetId(userId, POST_TARGET_TYPE, postId)
        return getState(userId, postId)
    }

    @Transactional
    override fun save(userId: UUID, postId: UUID): CommunityPostState {
        requireUser(userId)
        lockPublishedPost(postId)
        if (!savedPosts.existsByUserIdAndPostId(userId, postId)) {
            savedPosts.save(CommunitySavedPost(userId = userId, postId = postId))
        }
        return getState(userId, postId)
    }

    @Transactional
    override fun unsave(userId: UUID, postId: UUID): CommunityPostState {
        requireUser(userId)
        lockPublishedPost(postId)
        savedPosts.deleteByUserIdAndPostId(userId, postId)
        return getState(userId, postId)
    }

    override fun getState(userId: UUID, postId: UUID): CommunityPostState {
        requireUserAndPost(userId, postId)
        return CommunityPostState(
            liked = likes.existsByUserIdAndTargetTypeAndTargetId(userId, POST_TARGET_TYPE, postId),
            saved = savedPosts.existsByUserIdAndPostId(userId, postId)
        )
    }

    override fun getSavedPosts(userId: UUID, page: Int, size: Int): CommunityPostPageResponse {
        users.findById(userId).orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
        val savedPage = savedPosts.findPublishedByUserId(
            userId,
            CommunityPostStatus.PUBLISHED,
            PageRequest.of(validPage(page), validSize(size))
        )
        val postById = posts.findAllById(savedPage.content.map { it.postId }).associateBy { it.id }
        val visiblePosts = savedPage.content.mapNotNull { saved -> postById[saved.postId] }
        return CommunityPostPageResponse(
            content = toResponses(visiblePosts),
            page = savedPage.number,
            size = savedPage.size,
            totalElements = savedPage.totalElements,
            totalPages = savedPage.totalPages,
            last = savedPage.isLast
        )
    }

    private fun toPageResponse(page: Page<CommunityPost>) = CommunityPostPageResponse(
        content = toResponses(page.content),
        page = page.number,
        size = page.size,
        totalElements = page.totalElements,
        totalPages = page.totalPages,
        last = page.isLast
    )

    private fun toResponse(post: CommunityPost): CommunityPostResponse = toResponses(listOf(post)).single()

    private fun toResponses(postList: List<CommunityPost>): List<CommunityPostResponse> {
        if (postList.isEmpty()) return emptyList()

        val postIds = postList.map { it.id }
        val authorById = users.findAllById(postList.map { it.userId }.distinct()).associateBy { it.id }
        val artistById = artists.findAllById(postList.mapNotNull { it.artistId }.distinct()).associateBy { it.id }
        val likeCountByPostId = likes.countGroupedByTargetId(POST_TARGET_TYPE, postIds)
            .associate { it.getTargetId() to it.getTotal() }
        val commentCountByPostId = comments.countGroupedByPostIdAndStatus(
            postIds.map(UUID::toString),
            CommentStatus.APPROVED
        ).associate { it.getPostId() to it.getTotal() }

        return postList.map { post ->
            val author = authorById[post.userId]
                ?: throw IllegalStateException("게시글 작성자를 찾을 수 없습니다")
            val artist = post.artistId?.let(artistById::get)
            CommunityPostResponse(
                id = post.id,
                authorId = author.id,
                authorName = author.username,
                artistId = artist?.id,
                artistName = artist?.name,
                artistProfileImageUrl = artist?.profileImageUrl,
                content = post.content,
                imageUrl = post.imageUrl,
                likeCount = likeCountByPostId[post.id] ?: 0,
                commentCount = commentCountByPostId[post.id.toString()] ?: 0,
                createdAt = post.createdAt
            )
        }
    }

    private fun requireUserAndPost(userId: UUID, postId: UUID) {
        requireUser(userId)
        findPublishedPost(postId)
    }

    private fun requireUser(userId: UUID) {
        users.findById(userId).orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
    }

    private fun lockPublishedPost(postId: UUID): CommunityPost =
        posts.findPublishedByIdForUpdate(postId)
            ?: throw NoSuchElementException("게시글을 찾을 수 없습니다")

    private fun findPublishedPost(postId: UUID): CommunityPost = posts.findById(postId)
        .filter { it.status == CommunityPostStatus.PUBLISHED }
        .orElseThrow { NoSuchElementException("게시글을 찾을 수 없습니다") }


    private fun validPage(page: Int): Int {
        require(page >= 0) { "페이지 번호는 0 이상이어야 합니다" }
        return page
    }

    private fun validSize(size: Int): Int = size.coerceIn(1, 100)

    companion object {
        private const val POST_TARGET_TYPE = "POST"
    }
}
