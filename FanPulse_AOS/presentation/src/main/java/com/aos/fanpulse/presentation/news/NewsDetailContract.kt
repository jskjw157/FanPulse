package com.aos.fanpulse.presentation.news

import com.aos.fanpulse.domain.model.Comment
import com.aos.fanpulse.domain.model.NewsDetail
import com.aos.fanpulse.domain.model.NewsItem

object NewsDetailContract {

    val dummyComments = listOf(

        Comment(
            id = "comment_01",
            postId = "N-1004",
            userId = "user_abc123",
            content = "대상 정말 축하합니다!! 올해 무대 진짜 역대급이었어요.",
            status = "APPROVED",
            parentCommentId = null,
            createdAt = "2026-05-17T10:00:00Z"
        ),

        Comment(
            id = "comment_02",
            postId = "N-1004",
            userId = "user_xyz789",
            content = "완전 인정합니다. 라이브 폼 미쳤음 ㅠㅠ",
            status = "APPROVED",
            parentCommentId = "comment_01",
            createdAt = "2026-05-17T10:05:30Z"
        ),

        Comment(
            id = "comment_03",
            postId = "N-1004",
            userId = "user_def456",
            content = "엔하이픈, 블랙핑크 모두 고생 많으셨습니다~",
            status = "APPROVED",
            parentCommentId = "comment_01",
            createdAt = "2026-05-17T10:12:00Z"
        ),

        Comment(
            id = "comment_04",
            postId = "N-1004",
            userId = "user_qwerty",
            content = "MC 레이 진행 너무 깔끔하게 잘하더라.",
            status = "APPROVED",
            parentCommentId = null,
            createdAt = "2026-05-17T11:20:00Z"
        ),

        Comment(
            id = "comment_05",
            postId = "N-1004",
            userId = "user_troll",
            content = "비속어나 욕설이 포함된 악플...",
            status = "HIDDEN",
            parentCommentId = null,
            createdAt = "2026-05-17T11:45:00Z"
        ),

        Comment(
            id = "comment_06",
            postId = "N-1004",
            userId = "user_mania",
            content = "다음 컴백은 언제쯤 하려나 기대되네요!",
            status = "APPROVED",
            parentCommentId = null,
            createdAt = "2026-05-17T12:00:00Z"
        )
    )

    data class NewsDetailState(
        val newsDetail: NewsDetail? = null,
        val relatedNewsItem: List<NewsItem> = emptyList(),
        val commentsItem: List<Comment> = dummyComments,
        val commentInput: String = "",

        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}