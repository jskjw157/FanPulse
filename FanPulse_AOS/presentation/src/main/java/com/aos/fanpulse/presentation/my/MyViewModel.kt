package com.aos.fanpulse.presentation.my

import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetCurrentUserEmailUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserIdUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserPhotoUrlUseCase
import com.aos.fanpulse.domain.usecase.GetMyProfileUseCase
import com.aos.fanpulse.domain.usecase.GetPostsUseCase
import com.aos.fanpulse.domain.usecase.ToggleBookmarkUseCase
import com.aos.fanpulse.domain.usecase.ToggleLikeUseCase
import com.aos.fanpulse.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val getUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserEmailUseCase: GetCurrentUserEmailUseCase,
    private val getUserPhotoUrlUseCase: GetCurrentUserPhotoUrlUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getPostsUseCase: GetPostsUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
): ContainerHost<MyContract.State, MyContract.SideEffect>, ViewModel() {
    override val container: Container<MyContract.State, MyContract.SideEffect> =
        container(initialState = MyContract.State()){
            initMyData()
    }

    fun initMyData() = intent {
        reduce { state.copy(isLoading = true) }
        val userId = getUserIdUseCase()
        val userNickname = getMyProfileUseCase().getOrNull()?.username
        val userCreatedAt = getMyProfileUseCase().getOrNull()?.createdAt
        val userEmail = getUserEmailUseCase()
        val userPhotoUrl = getUserPhotoUrlUseCase()

        reduce {
            state.copy(
                userId = userId,
                userNickname = userNickname,
                userCreatedAt = userCreatedAt,
                userEmail = userEmail,
                userPhotoUrl = userPhotoUrl
            )
        }
        loadPosts(artistCategory = null)
    }
    fun loadPosts(artistCategory: String?) = intent {

        val myId = getUserIdUseCase()
        if (myId == null) {
            postSideEffect(MyContract.SideEffect.ShowToast("로그인 정보가 없습니다."))
            return@intent
        }

        reduce { state.copy(isLoading = true, error = null) }

        getPostsUseCase(artistCategory,myId)
            .onSuccess { posts ->
                reduce {
                    state.copy(isLoading = false, posts = posts)
                }
            }
            .onFailure { exception ->
                reduce {
                    state.copy(isLoading = false, error = exception)
                }
                postSideEffect(
                    MyContract.SideEffect.ShowToast(
                        message = exception.message ?: "게시글을 불러오는 데 실패했습니다."
                    )
                )
            }
    }
    fun updateProfile(nickname: String) = intent {

        reduce { state.copy(isLoading = true, error = null) }

        updateProfileUseCase(nickname = nickname, bio = null)
            .onSuccess { update ->
                reduce {
                    state.copy(isLoading = false)
                }
                postSideEffect(MyContract.SideEffect.NavigateBack)
            }
            .onFailure { exception ->
                reduce {
                    state.copy(isLoading = false, error = exception)
                }
                postSideEffect(
                    MyContract.SideEffect.ShowToast(
                        message = exception.message ?: "닉네임 변경을 실패했습니다."
                    )
                )
            }
    }
    fun toggleLike(postId: String) = intent {
        val myId = state.userId ?: return@intent
        val previousPosts = state.posts
        val updatedPosts = previousPosts.map { post ->
            if (post.id == postId) {
                post.copy(
                    isLikedByMe = !post.isLikedByMe,
                    likeCount = post.likeCount + if (post.isLikedByMe) -1 else 1
                )
            } else {
                post
            }
        }

        reduce { state.copy(posts = updatedPosts) }

        toggleLikeUseCase(postId, myId).onFailure { exception ->
            reduce { state.copy(posts = previousPosts) }
            postSideEffect(
                MyContract.SideEffect.ShowToast(
                    message = exception.message ?: "좋아요 처리에 실패했습니다."
                )
            )
        }
    }

    fun toggleBookmark(postId: String) = intent {
        val myId = state.userId ?: return@intent
        val previousPosts = state.posts
        val updatedPosts = previousPosts.map { post ->
            if (post.id == postId) {
                post.copy(
                    isBookmarkedByMe = !post.isBookmarkedByMe
                )
            } else {
                post
            }
        }

        reduce { state.copy(posts = updatedPosts) }

        toggleBookmarkUseCase(postId, myId).onFailure { exception ->
            reduce { state.copy(posts = previousPosts) }
            postSideEffect(
                MyContract.SideEffect.ShowToast(
                    message = exception.message ?: "북마크 처리에 실패했습니다."
                )
            )
        }
    }
    fun goSettingScreen() = intent {
        postSideEffect(MyContract.SideEffect.NavigateSetting)
    }
}