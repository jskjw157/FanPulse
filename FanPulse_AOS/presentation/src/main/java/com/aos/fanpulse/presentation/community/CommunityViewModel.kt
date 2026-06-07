package com.aos.fanpulse.presentation.community

import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.model.Artist
import com.aos.fanpulse.domain.usecase.GetArtistUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserEmailUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserIdUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserPhotoUrlUseCase
import com.aos.fanpulse.domain.usecase.GetPostsUseCase
import com.aos.fanpulse.domain.usecase.ToggleBookmarkUseCase
import com.aos.fanpulse.domain.usecase.ToggleLikeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserEmailUseCase: GetCurrentUserEmailUseCase,
    private val getUserPhotoUrlUseCase: GetCurrentUserPhotoUrlUseCase,
    private val getArtistUseCase: GetArtistUseCase,
    private val getPostsUseCase: GetPostsUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
): ContainerHost<CommunityContract.State, CommunityContract.SideEffect>, ViewModel() {
    override val container: Container<CommunityContract.State, CommunityContract.SideEffect> =
        container(initialState = CommunityContract.State()) {
            initData()
        }

    fun initData() = intent {
        reduce { state.copy(isLoading = true) }
        val userId = getUserIdUseCase()
        val userEmail = getUserEmailUseCase()
        val userPhotoUrl = getUserPhotoUrlUseCase()

        reduce {
            state.copy(
                userId = userId,
                userEmail = userEmail,
                userPhotoUrl = userPhotoUrl
            )
        }
        fetchArtists()
        loadPosts(artistCategory = null)
    }

    fun fetchArtists() = intent {
        val response = getArtistUseCase(
            activeOnly = true,
            page = 0,
            size = 20,
            sortBy = "name",
            sortDir = "asc"
        )

        if (response.isSuccess) {
            val originalList = response.getOrNull()?.data?.content ?: emptyList()
            val artistList = originalList.map {
                Artist(
                    id = it.id,
                    name = it.name,
                    englishName = it.englishName,
                    agency = it.agency,
                    profileImageUrl = it.profileImageUrl,
                    isGroup = it.isGroup,
                )
            }

            val allArtist = Artist(
                id = 0.toString(),
                name = "ALL",
                englishName = "ALL",
                agency = "",
                profileImageUrl = "",
                isGroup = false
            )

            val finalArtistList = listOf(allArtist) + artistList

            reduce {
                state.copy(artists = finalArtistList)
            }
        }
    }

    fun updateSelectedArtist(artist: Artist) = intent {
        reduce { state.copy(selectedArtist = artist) }
    }

    fun loadPosts(artistCategory: String?) = intent {

        val myId = getUserIdUseCase()
        if (myId == null) {
            postSideEffect(CommunityContract.SideEffect.ShowToast("로그인 정보가 없습니다."))
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
                    CommunityContract.SideEffect.ShowToast(
                        message = exception.message ?: "게시글을 불러오는 데 실패했습니다."
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
                CommunityContract.SideEffect.ShowToast(
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
                CommunityContract.SideEffect.ShowToast(
                    message = exception.message ?: "북마크 처리에 실패했습니다."
                )
            )
        }
    }
}