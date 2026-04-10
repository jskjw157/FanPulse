package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.CommentListResponse
import com.aos.fanpulse.domain.repository.CommentsRepository
import retrofit2.Response
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val repository: CommentsRepository
)  {
    /**
     * @param postId 게시글 ID (필수)
     * @param page 현재 페이지 번호 (기본값 0)
     * @param size 한 번에 가져올 댓글 개수 (기본값 20)
     */
    suspend operator fun invoke(
        postId: String,
        page: Int = 0,
        size: Int = 20
    ): Response<CommentListResponse> {

        // 유효성 검사: 게시글 ID가 비어있으면 서버 요청을 하지 않음
        if (postId.isBlank()) {
            throw IllegalArgumentException("유효하지 않은 게시글 접근입니다. (Post ID 누락)")
        }

        // 파라미터 방어 로직: 페이지 번호나 사이즈가 음수일 경우를 대비
        val safePage = if (page < 0) 0 else page
        val safeSize = if (size <= 0) 20 else size

        // Repository 호출
        val response = repository.getComments(
            postId = postId,
            page = safePage,
            size = safeSize
        )

        // (선택 사항) 데이터 가공 로직
        // 만약 서버에서 온 데이터 중 '삭제된 댓글'을 클라이언트에서 숨겨야 하거나,
        // 특정 유저의 댓글을 필터링해야 한다면 여기서 response.body()를 수정할 수 있습니다.

        return response
    }
}