package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.ImageStorageRepository

import javax.inject.Inject

class UploadImagesUseCase @Inject constructor(
    private val imageStorageRepository: ImageStorageRepository
) {
    suspend operator fun invoke(localFilePaths: List<String>): Result<List<String>> {
        if (localFilePaths.isEmpty()) return Result.success(emptyList())
        return imageStorageRepository.uploadImages(localFilePaths)
    }
}