package com.aos.fanpulse.presentation.community

import com.aos.fanpulse.domain.model.Comment
import com.aos.fanpulse.domain.model.Post

object CommunityDetailContract {

    data class State (
        val isLoading: Boolean = false,
        val userId: String? = null,
        val userEmail: String? = null,
        val userPhotoUrl: String? = null,
        val post: Post? = null,
        val comments: List<Comment> = emptyList(),
        val commentInput: String = "",
        val error: Throwable? = null,
        val isMenuExpanded: Boolean = false
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        object NavigateBack : SideEffect
        data class NavigateToEdit(val postId: String) : SideEffect
    }
}