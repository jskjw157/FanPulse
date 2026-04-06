package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.ArtistChannelsRepository
import retrofit2.Response
import javax.inject.Inject

//  ArtistChannelsRepository - createArtistChannel(request) & patchArtistChannel(...)
class UpdateArtistChannelUseCase@Inject constructor(
    private val repository: ArtistChannelsRepository
){
    suspend operator fun invoke(
        channelId: String,
        newName: String?, // 수정할 이름 (수정 안 하면 null)
        newUrl: String?   // 수정할 링크 (수정 안 하면 null)
    ): Response<Unit> { // 반환 타입은 Repository와 동일하게 맞춥니다.

        // 유효성 검사 (Validation)

        // 이름 검사: 입력값이 넘어왔는데 빈칸이거나 너무 짧은 경우
        if (newName != null) {
            val safeName = newName.trim()
            if (safeName.isBlank()) {
                throw IllegalArgumentException("채널 이름은 비워둘 수 없습니다.")
            }
            if (safeName.length > 30) {
                throw IllegalArgumentException("채널 이름은 30자를 초과할 수 없습니다.")
            }
        }

        // URL 형식 검사: 정규식을 활용해 진짜 웹사이트 주소인지 확인
        if (newUrl != null) {
            val safeUrl = newUrl.trim()
            if (safeUrl.isBlank()) {
                throw IllegalArgumentException("채널 링크를 입력해주세요.")
            }
            val urlRegex = "^(https?://)?(www\\.)?([a-zA-Z0-9]+(-?[a-zA-Z0-9])*\\.)+[\\w]{2,}(/.*)?$".toRegex()
            if (!safeUrl.matches(urlRegex)) {
                throw IllegalArgumentException("유효한 URL 형식이 아닙니다.")
            }
        }

        // 2. 검사를 무사히 통과했다면 서버로 보낼 Request 객체 조립
//        val request = PatchArtistChannelRequest(
//            name = newName?.trim(),
//            url = newUrl?.trim()
//        )

        // 3. Repository를 통해 실제 API 호출!
        return repository.patchArtistChannel(channelId, //request
        )
    }
}

//data class PatchArtistChannelRequest(
//    val name: String? = null,
//    val url: String? = null
//)