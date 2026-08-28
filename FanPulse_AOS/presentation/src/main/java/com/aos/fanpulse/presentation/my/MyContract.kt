package com.aos.fanpulse.presentation.my

import com.aos.fanpulse.domain.model.Post

object MyContract {

    data class State(
        val isLoading: Boolean = false,
        val userId: String? = null,
        val userNickname: String? = null,
        val userCreatedAt: String? = null,
        val userEmail: String? = null,
        val userPhotoUrl: String? = null,
        val posts: List<Post> = emptyList(),
        val error: Throwable? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        object NavigateSetting : SideEffect
        object NavigateBack : SideEffect
    }
}