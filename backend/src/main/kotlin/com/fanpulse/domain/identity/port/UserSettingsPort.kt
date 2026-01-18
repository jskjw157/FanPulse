package com.fanpulse.domain.identity.port

import com.fanpulse.domain.identity.UserSettings
import java.util.UUID

/**
 * UserSettings Repository Port (출력 포트)
 *
 * 사용자 설정 저장소 인터페이스
 */
interface UserSettingsPort {
    /**
     * 사용자 설정 저장
     */
    fun save(settings: UserSettings): UserSettings

    /**
     * 사용자 ID로 설정 조회
     */
    fun findByUserId(userId: UUID): UserSettings?
}
