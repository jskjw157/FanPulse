package com.aos.fanpulse.data.repository

import android.net.Uri
import com.aos.fanpulse.domain.repository.ImageStorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import androidx.core.net.toUri

class ImageStorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : ImageStorageRepository {

    override suspend fun uploadImages(localFilePaths: List<String>): Result<List<String>> {
        return try {
            val uploadedUrls = mutableListOf<String>()

            for ((index, filePath) in localFilePaths.withIndex()) {

                val uri = filePath.toUri()
                val fileName = "${UUID.randomUUID()}_${System.currentTimeMillis()}.png"
                val storageRef = storage.reference
                    .child("posts_images")
                    .child(fileName)

                storageRef.putFile(uri).await()

                val downloadUrl = storageRef.downloadUrl.await().toString()

                uploadedUrls.add(downloadUrl)
            }

            Result.success(uploadedUrls)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}