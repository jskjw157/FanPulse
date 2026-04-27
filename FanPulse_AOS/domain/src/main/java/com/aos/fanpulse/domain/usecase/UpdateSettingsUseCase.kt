package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.UpdateSettingsRequest
import com.aos.fanpulse.domain.model.UserSettings
import com.aos.fanpulse.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    /**
     * @param theme 테마 모드 (LIGHT, DARK, SYSTEM)
     * @param language 언어 설정 (ko, en 등)
     * @param pushEnabled 푸시 알림 여부
     */
    suspend operator fun invoke(theme: String, language: String, pushEnabled: Boolean): Result<UserSettings> = runCatching {
        val safeTheme = theme.trim().uppercase()
        if (safeTheme !in listOf("LIGHT", "DARK", "SYSTEM")) {
            throw IllegalArgumentException("유효하지 않은 테마 설정입니다.")
        }

        val safeLanguage = language.trim().lowercase()
        if (safeLanguage !in listOf("ko", "en", "ja")) {
            throw IllegalArgumentException("지원하지 않는 언어입니다.")
        }

        repository.updateSettings(UpdateSettingsRequest(safeTheme, safeLanguage, pushEnabled))
    }
}