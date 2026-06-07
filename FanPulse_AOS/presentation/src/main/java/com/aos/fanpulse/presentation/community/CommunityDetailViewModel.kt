package com.aos.fanpulse.presentation.community

import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.CreateCommentUseCase
import com.aos.fanpulse.domain.usecase.DeletePostUseCase
import com.aos.fanpulse.domain.usecase.GetCommentsUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserEmailUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserIdUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserPhotoUrlUseCase
import com.aos.fanpulse.domain.usecase.GetPostDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    private val getUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserEmailUseCase: GetCurrentUserEmailUseCase,
    private val getUserPhotoUrlUseCase: GetCurrentUserPhotoUrlUseCase,
    private val getPostDetailUseCase: GetPostDetailUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val deletePostUseCase: DeletePostUseCase
): ContainerHost<CommunityDetailContract.State, CommunityDetailContract.SideEffect>, ViewModel(){
    override val container : Container<CommunityDetailContract.State, CommunityDetailContract.SideEffect> =
        container(initialState = CommunityDetailContract.State()){
            loadUserInfo()
        }

    fun loadData(postId: String) = intent {
        reduce { state.copy(isLoading = true) }

        getPostDetailUseCase(postId, state.userId.toString())
            .onSuccess { post ->
                reduce { state.copy(post = post) }
            }
            .onFailure {
                postSideEffect(CommunityDetailContract.SideEffect.ShowToast("게시글을 불러오지 못했습니다."))
            }

        getCommentsUseCase(postId)
            .onSuccess { commentListResponse ->
                reduce { state.copy(comments = commentListResponse.content) }
            }

        reduce { state.copy(isLoading = false) }
    }
    private fun loadUserInfo() = intent {
        reduce {
            state.copy(
                userId = getUserIdUseCase(),
                userEmail = getUserEmailUseCase(),
                userPhotoUrl = getUserPhotoUrlUseCase()
            )
        }
    }
    fun openMenu() = intent { reduce { state.copy(isMenuExpanded = true) } }
    fun closeMenu() = intent { reduce { state.copy(isMenuExpanded = false) } }
    fun onEditClicked(postId: String) = intent {
        reduce { state.copy(isMenuExpanded = false) }
        postSideEffect(CommunityDetailContract.SideEffect.NavigateToEdit(postId))
    }
    fun onDeleteClicked(postId: String) = intent {
        reduce { state.copy(isMenuExpanded = false, isLoading = true) }

        deletePostUseCase(postId, state.userId.toString())
            .onSuccess {
                reduce { state.copy(isLoading = false) }
                postSideEffect(CommunityDetailContract.SideEffect.ShowToast("게시글이 삭제되었습니다."))
                postSideEffect(CommunityDetailContract.SideEffect.NavigateBack)
            }
            .onFailure { exception ->
                reduce { state.copy(isLoading = false) }
                postSideEffect(CommunityDetailContract.SideEffect.ShowToast("삭제 실패: ${exception.message}"))
            }
    }
    fun updateCommentInput(text: String) = intent {
        reduce { state.copy(commentInput = text) }
    }

    fun submitComment(postId: String) = intent {
        if (state.commentInput.isBlank()) return@intent

        reduce { state.copy(isLoading = true) }


        createCommentUseCase(
            postId = postId, content = state.commentInput
        ).onSuccess {
            reduce { state.copy(commentInput = "") }
            postSideEffect(CommunityDetailContract.SideEffect.ShowToast("댓글이 등록되었습니다."))
        }.onFailure { exception ->
            reduce { state.copy(isLoading = false) }
            postSideEffect(CommunityDetailContract.SideEffect.ShowToast("댓글 등록 실패: ${exception.message}"))
        }
        reduce { state.copy(commentInput = "", isLoading = false) }
    }

}