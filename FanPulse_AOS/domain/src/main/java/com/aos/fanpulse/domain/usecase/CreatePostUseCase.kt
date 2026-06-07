package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.PostRepository
import java.util.UUID
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        artistCategory: String,
        content: String,
        imageUrls: List<String>,
        tags: List<String>,
        authorId: String,
        authorNickname: String,
        authorProfileUrl: String,
        authorFandom: String?
    ): Result<Unit> {
        if (content.isBlank() || content.length > 500) {
            return Result.failure(IllegalArgumentException("글자 수는 1자 이상 500자 이하셔야 합니다."))
        }
        val newPostId = UUID.randomUUID().toString()
        return postRepository.createPost(newPostId ,artistCategory, content, imageUrls, tags, authorId, authorNickname, authorProfileUrl, authorFandom)
    }
}