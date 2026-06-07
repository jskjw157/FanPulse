package com.aos.fanpulse.data.mapper

import com.aos.fanpulse.data.remote.dto.PostDto
import com.aos.fanpulse.data.remote.dto.UserDto
import com.aos.fanpulse.domain.model.Post
import com.aos.fanpulse.domain.model.User

fun PostDto.toDomain(isLiked: Boolean, isBookmarked: Boolean): Post {
    return Post(
        id = this.id,
        author = User(
            id = authorId,
            nickname = authorNickname.ifEmpty { "익명" },
            profileImageUrl = authorProfileImageUrl.ifEmpty { null },
            fandom = authorFandom?.ifEmpty { null }
        ),
        targetArtist = this.targetArtist,
        content = this.content,
        imageUrls = this.imageUrls,
        tags = this.tags,
        createdAt = this.createdAt,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        shareCount = this.shareCount,
        isLikedByMe = isLiked,
        isBookmarkedByMe = isBookmarked
    )
}

fun UserDto.toDomain(): User = User(id, profileImageUrl, nickname, fandom)