package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.UpdateSettingsRequest
import com.aos.fanpulse.data.remote.apiservice.UserSettings
import com.aos.fanpulse.domain.repository.UserProfileRepository
import retrofit2.Response
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    /**
     * @param theme 테마 모드 (LIGHT, DARK, SYSTEM)
     * @param language 언어 설정 (ko, en 등)
     * @param pushEnabled 푸시 알림 여부
     */
    suspend operator fun invoke(
        theme: String,
        language: String,
        pushEnabled: Boolean
    ): Response<UserSettings> {

        // 테마 값 검증 및 정제
        val safeTheme = theme.trim().uppercase()
        val allowedThemes = listOf("LIGHT", "DARK", "SYSTEM")
        if (!allowedThemes.contains(safeTheme)) {
            throw IllegalArgumentException("유효하지 않은 테마 설정입니다. (LIGHT, DARK, SYSTEM 중 선택)")
        }

        // 언어 값 검증 (ISO 국가 코드 형식 등 앱 정책에 따라)
        val safeLanguage = language.trim().lowercase()
        val allowedLanguages = listOf("ko", "en", "ja") // 앱에서 지원하는 언어 목록
        if (!allowedLanguages.contains(safeLanguage)) {
            throw IllegalArgumentException("지원하지 않는 언어 형식입니다.")
        }

        // 3. Request 객체 조립 (데이터 클래스 구조에 맞게)
        val request = UpdateSettingsRequest(
            theme = safeTheme,
            language = safeLanguage,
            pushEnabled = pushEnabled
        )

        // 4. Repository 호출
        return repository.updateSettings(request)
    }
}