package com.aos.fanpulse.data.repository

import com.aos.fanpulse.domain.repository.BookmarkRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookmarkRepository {
    override suspend fun toggleBookmark(postId: String, userId: String): Result<Boolean> {
        return try {
            val isBookmarkedNow = firestore.runTransaction { transaction ->
                val bookmarkRef = firestore.collection("bookmarks").document("${userId}_${postId}")
                val bookmarkSnapshot = transaction.get(bookmarkRef)

                if (bookmarkSnapshot.exists()) {
                    transaction.delete(bookmarkRef)
                    false
                } else {
                    val bookmarkData = hashMapOf(
                        "userId" to userId,
                        "postId" to postId,
                        "createdAt" to System.currentTimeMillis()
                    )
                    transaction.set(bookmarkRef, bookmarkData)
                    true
                }
            }.await()

            Result.success(isBookmarkedNow)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}