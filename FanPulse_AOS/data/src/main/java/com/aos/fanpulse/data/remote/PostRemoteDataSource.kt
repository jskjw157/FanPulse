package com.aos.fanpulse.data.remote

import com.aos.fanpulse.data.remote.dto.PostDto
import com.aos.fanpulse.data.remote.dto.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // 1. 특정 아티스트의 게시글 목록 조회
    suspend fun getPosts(category: String?): List<PostDto> {
        var query: Query = firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        if (category != null) {
            query = query.whereEqualTo("targetArtist", category)
        }
        val snapshots = query.get().await()
        return snapshots.documents.mapNotNull { doc ->
            doc.toObject(PostDto::class.java)?.copy(id = doc.id)
        }
    }

    // 2. 단일 게시글 상세 조회
    suspend fun getPostById(postId: String): PostDto? {
        val snapshot = firestore.collection("posts").document(postId).get().await()
        return snapshot.toObject(PostDto::class.java)?.copy(id = snapshot.id)
    }

    // 3. 단일 유저 정보 조회
    suspend fun getUserById(userId: String): UserDto? {
        val snapshot = firestore.collection("users").document(userId).get().await()
        return snapshot.toObject(UserDto::class.java)?.copy(id = snapshot.id)
    }

    // 4. 특정 유저가 특정 게시물에 좋아요를 눌렀는지 확인
    suspend fun checkIsLiked(userId: String, postId: String): Boolean {
        val snapshot = firestore.collection("likes").document("${userId}_${postId}").get().await()
        return snapshot.exists()
    }

    // 5. 특정 유저가 특정 게시물을 북마크했는지 확인
    suspend fun checkIsBookmarked(userId: String, postId: String): Boolean {
        val snapshot = firestore.collection("bookmarks").document("${userId}_${postId}").get().await()
        return snapshot.exists()
    }

    // 6. 새 게시글 생성
    suspend fun createPost(postData: Map<String, Any?>) {
        // 문서 ID를 자동 생성하면서 데이터를 저장합니다.
        firestore.collection("posts").document().set(postData).await()
    }

    // 7. 특정 게시글 문서 삭제
    suspend fun deletePost(postId: String) {
        firestore.collection("posts").document(postId).delete().await()
    }

    // 8. 특정 유저의 해당 게시글 좋아요 문서 삭제
    suspend fun deleteLike(userId: String, postId: String) {
        firestore.collection("likes").document("${userId}_${postId}").delete().await()
    }

    // 9. 특정 유저의 해당 게시글 북마크 문서 삭제
    suspend fun deleteBookmark(userId: String, postId: String) {
        firestore.collection("bookmarks").document("${userId}_${postId}").delete().await()
    }
}