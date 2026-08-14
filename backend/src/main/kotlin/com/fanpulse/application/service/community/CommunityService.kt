package com.fanpulse.application.service.community

import java.time.Instant
import java.util.UUID

class CommunityModerationUnavailableException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

enum class CommunitySort {
    LATEST,
    POPULAR
}

data class CreateCommunityPostRequest(
    val artistId: UUID?,
    val content: String
)

data class CommunityPostResponse(
    val id: UUID,
    val authorId: UUID,
    val authorName: String,
    val artistId: UUID?,
    val artistName: String?,
    val artistProfileImageUrl: String?,
    val content: String,
    val imageUrl: String?,
    val likeCount: Long,
    val commentCount: Long,
    val createdAt: Instant
)

data class CommunityPostPageResponse(
    val content: List<CommunityPostResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

data class CommunityPostState(
    val liked: Boolean,
    val saved: Boolean
)

interface CommunityService {
    fun createPost(userId: UUID, request: CreateCommunityPostRequest): CommunityPostResponse
    fun getPosts(page: Int, size: Int, sort: CommunitySort): CommunityPostPageResponse
    fun getPost(postId: UUID): CommunityPostResponse
    fun like(userId: UUID, postId: UUID): CommunityPostState
    fun unlike(userId: UUID, postId: UUID): CommunityPostState
    fun save(userId: UUID, postId: UUID): CommunityPostState
    fun unsave(userId: UUID, postId: UUID): CommunityPostState
    fun getState(userId: UUID, postId: UUID): CommunityPostState
    fun getSavedPosts(userId: UUID, page: Int, size: Int): CommunityPostPageResponse
}
