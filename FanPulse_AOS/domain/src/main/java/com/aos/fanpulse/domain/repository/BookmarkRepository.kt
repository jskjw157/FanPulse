package com.aos.fanpulse.domain.repository

interface BookmarkRepository {
    suspend fun toggleBookmark(postId: String, userId: String): Result<Boolean>
}