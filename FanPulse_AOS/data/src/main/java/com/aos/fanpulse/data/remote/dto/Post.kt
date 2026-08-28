package com.aos.fanpulse.data.remote.dto

data class PostDto(
    val id: String = "",

    val authorId: String = "",
    val authorNickname: String = "",
    val authorProfileImageUrl: String = "",
    val authorFandom: String? = null,

    val targetArtist: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = 0L,

    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0
)

data class UserDto(
    val id: String = "",
    val profileImageUrl: String? = null,
    val nickname: String = "",
    val fandom: String? = null
)