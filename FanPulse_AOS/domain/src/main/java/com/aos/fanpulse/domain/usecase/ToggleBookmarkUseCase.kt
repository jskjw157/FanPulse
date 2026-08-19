package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.BookmarkRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(postId: String, userId: String): Result<Boolean> {
        return bookmarkRepository.toggleBookmark(postId, userId)
    }
}