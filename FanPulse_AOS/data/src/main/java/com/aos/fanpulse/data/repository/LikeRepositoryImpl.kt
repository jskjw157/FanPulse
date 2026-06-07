package com.aos.fanpulse.data.repository

import com.aos.fanpulse.domain.repository.LikeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LikeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LikeRepository {

    override suspend fun toggleLike(postId: String, userId: String): Result<Boolean> {
        return try {
            val isLikedNow = firestore.runTransaction { transaction ->
                val postRef = firestore.collection("posts").document(postId)
                val likeRef = firestore.collection("likes").document("${userId}_${postId}")

                val likeSnapshot = transaction.get(likeRef)

                if (likeSnapshot.exists()) {
                    transaction.delete(likeRef)
                    transaction.update(postRef, "likeCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    false
                } else {
                    val likeData = hashMapOf(
                        "userId" to userId,
                        "postId" to postId,
                        "createdAt" to System.currentTimeMillis()
                    )
                    transaction.set(likeRef, likeData)
                    transaction.update(postRef, "likeCount", com.google.firebase.firestore.FieldValue.increment(1))
                    true
                }
            }.await()

            Result.success(isLikedNow)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}