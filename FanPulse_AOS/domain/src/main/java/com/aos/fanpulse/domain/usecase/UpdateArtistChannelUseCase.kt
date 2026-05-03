package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.ArtistChannelsRepository
import javax.inject.Inject

//  ArtistChannelsRepository - createArtistChannel(request) & patchArtistChannel(...)
class UpdateArtistChannelUseCase@Inject constructor(
    private val repository: ArtistChannelsRepository
){
    suspend operator fun invoke(
        channelId: String,
        newName: String?,
        newUrl: String?
    ): Result<Unit> {

        return runCatching {
            if (newName != null) {
                val safeName = newName.trim()
                if (safeName.isBlank()) {
                    throw IllegalArgumentException("채널 이름은 비워둘 수 없습니다.")
                }
                if (safeName.length > 30) {
                    throw IllegalArgumentException("채널 이름은 30자를 초과할 수 없습니다.")
                }
            }

            if (newUrl != null) {
                val safeUrl = newUrl.trim()
                if (safeUrl.isBlank()) {
                    throw IllegalArgumentException("채널 링크를 입력해주세요.")
                }
                val urlRegex =
                    "^(https?://)?(www\\.)?([a-zA-Z0-9]+(-?[a-zA-Z0-9])*\\.)+[\\w]{2,}(/.*)?$".toRegex()
                if (!safeUrl.matches(urlRegex)) {
                    throw IllegalArgumentException("유효한 URL 형식이 아닙니다.")
                }
            }

            repository.patchArtistChannel(id = channelId,)
        }
    }
}
