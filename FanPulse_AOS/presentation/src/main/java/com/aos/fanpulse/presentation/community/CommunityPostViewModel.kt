package com.aos.fanpulse.presentation.community

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.model.Artist
import com.aos.fanpulse.domain.usecase.CreatePostUseCase
import com.aos.fanpulse.domain.usecase.GetArtistUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserIdUseCase
import com.aos.fanpulse.domain.usecase.GetCurrentUserPhotoUrlUseCase
import com.aos.fanpulse.domain.usecase.GetMyProfileUseCase
import com.aos.fanpulse.domain.usecase.UploadImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class CommunityPostViewModel @Inject constructor(
    private val getUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserPhotoUrlUseCase: GetCurrentUserPhotoUrlUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val createPostUseCase: CreatePostUseCase,
    private val uploadImagesUseCase: UploadImagesUseCase,
    private val getArtistUseCase: GetArtistUseCase,
): ContainerHost<CommunityPostContract.State, CommunityPostContract.SideEffect>, ViewModel() {

    override val container: Container<CommunityPostContract.State, CommunityPostContract.SideEffect> =
        container(initialState = CommunityPostContract.State()){
            fetchArtists()
        }

    fun updateSelectedArtist(artist: Artist) = intent {
        reduce { state.copy(selectedArtist = artist) }
    }

    fun updateContent(text: String) = intent {
        if (text.length <= 500) {
            reduce { state.copy(content = text) }
        }
    }

    fun addTag(tag: String) = intent {
        val cleanTag = tag.trim()
        if (cleanTag.isBlank()) return@intent

        if (state.tags.size >= 5) {
            postSideEffect(CommunityPostContract.SideEffect.ShowToast("태그는 최대 5개까지만 가능합니다."))
            return@intent
        }

        if (state.tags.contains(cleanTag)) {
            postSideEffect(CommunityPostContract.SideEffect.ShowToast("이미 추가된 태그입니다."))
            return@intent
        }

        reduce { state.copy(tags = state.tags + cleanTag) }
    }

    fun removeTag(tag: String) = intent {
        reduce { state.copy(tags = state.tags - tag) }
    }

    fun createPost(artistCategory: String) = intent {
        val myId = getUserIdUseCase()
        if (myId == null) {
            postSideEffect(CommunityPostContract.SideEffect.ShowToast("로그인 정보가 없습니다."))
            return@intent
        }

        val myProfileUrl = getUserPhotoUrlUseCase()
        if (myProfileUrl == null) {
            postSideEffect(CommunityPostContract.SideEffect.ShowToast("유저 정보가 없습니다."))
            return@intent
        }

        val myNickname = getMyProfileUseCase().getOrNull()?.username
        if (myNickname == null) {
            postSideEffect(CommunityPostContract.SideEffect.ShowToast("닉네임 정보가 없습니다."))
            return@intent
        }

        if (state.content.isBlank()) {
            postSideEffect(CommunityPostContract.SideEffect.ShowToast("내용을 입력해주세요."))
            return@intent
        }

        reduce { state.copy(isLoading = true) }

        val uploadedUrls = if (state.selectedImages.isNotEmpty()) {
            val localImagePaths = state.selectedImages.map { it.toString() }

            val uploadResult = uploadImagesUseCase(localImagePaths)

            if (uploadResult.isFailure) {
                reduce { state.copy(isLoading = false) }
                postSideEffect(CommunityPostContract.SideEffect.ShowToast("이미지 업로드에 실패했습니다."))
                return@intent
            }
            uploadResult.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        createPostUseCase(
            artistCategory = artistCategory,
            content = state.content,
            imageUrls = uploadedUrls,
            tags = state.tags,
            authorId = myId,
            authorNickname = myNickname,
            authorFandom = null,                    //  TODO 팬덤은 빼기 저장하는 데이터가 없음
            authorProfileUrl = myProfileUrl         //  TODO 프로필 수정이 안됨 저장하는 데이터가 없음
        ).onSuccess {
            reduce { state.copy(isLoading = false) }
            postSideEffect(CommunityPostContract.SideEffect.ShowToast("게시글이 작성되었습니다."))
            postSideEffect(CommunityPostContract.SideEffect.NavigateBack)
        }.onFailure { exception ->
            reduce { state.copy(isLoading = false) }
            postSideEffect(CommunityPostContract.SideEffect.ShowToast(exception.message ?: "게시글 작성에 실패했습니다."))
        }
    }

    fun addImages(newImages: List<Uri>) = intent {
        val combinedImages = (state.selectedImages + newImages).take(5)
        reduce { state.copy(selectedImages = combinedImages) }
    }

    fun removeImage(uri: Uri) = intent {
        reduce { state.copy(selectedImages = state.selectedImages - uri) }
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
            reduce {
                state.copy(artists = artistList)
            }
        }
    }
}