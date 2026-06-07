package com.aos.fanpulse.domain.repository

interface LikeRepository {
    /**
     * 좋아요 상태를 토글(On/Off)합니다.
     * @return 최종적으로 좋아요 상태가 되었는지 여부 (true: 좋아요 됨, false: 취소됨)
     */
    suspend fun toggleLike(postId: String, userId: String): Result<Boolean>
}