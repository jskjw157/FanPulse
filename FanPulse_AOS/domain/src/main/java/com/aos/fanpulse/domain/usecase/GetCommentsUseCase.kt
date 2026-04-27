package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.CommentListResponse
import com.aos.fanpulse.domain.repository.CommentsRepository
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
    ): Result<CommentListResponse> {

        return runCatching {
            // 1. 유효성 검사: 게시글 ID 확인
            if (postId.isBlank()) {
                throw IllegalArgumentException("유효하지 않은 게시글 접근입니다. (Post ID 누락)")
            }

            // 2. 파라미터 방어 로직 (정제)
            val safePage = if (page < 0) 0 else page
            val safeSize = if (size <= 0) 20 else size

            // 3. Repository 호출
            // 리포지토리는 이제 Response<T>가 아닌 순수 CommentListResponse를 반환합니다.
            val commentList = repository.getComments(
                postId = postId,
                page = safePage,
                size = safeSize
            )

            // 4. (선택 사항) 데이터 가공 로직
            // 예: 삭제된 댓글은 리스트에서 미리 제외하고 넘겨줌
            // val filteredComments = commentList.comments.filter { !it.isDeleted }

            commentList
        }
    }
}