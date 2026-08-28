package com.aos.fanpulse.domain.repository


interface ImageStorageRepository {
    suspend fun uploadImages(localFilePaths: List<String>): Result<List<String>>
}