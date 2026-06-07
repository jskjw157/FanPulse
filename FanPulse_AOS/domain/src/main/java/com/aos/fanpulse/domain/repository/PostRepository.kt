package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.domain.model.Post

interface PostRepository {
    suspend fun fetchPosts(artistCategory: String?, currentUserId: String): Result<List<Post>>
    suspend fun fetchPostDetail(postId: String, currentUserId: String): Result<Post>
    suspend fun createPost(postId: String, artistCategory: String, content: String, imageUrls: List<String>, tags: List<String>, authorId: String, authorNickname: String, authorProfileUrl: String, authorFandom: String?): Result<Unit>
    suspend fun deletePost(postId: String, currentUserId: String): Result<Unit>
}