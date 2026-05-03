package com.aos.fanpulse.data.mapper

// [Data 계층 DTO] (서버 통신용)
import com.aos.fanpulse.data.remote.dto.AuthUserInfo as DataAuthUserInfo

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.AuthUserInfo as DomainAuthUserInfo

// ==========================================
// 1. 유저 정보 매핑 (중첩 클래스 대응)
// ==========================================
internal fun DataAuthUserInfo.toDomain(): DomainAuthUserInfo {
    return DomainAuthUserInfo(
        id = this.id,
        email = this.email,
        username = this.username
    )
}