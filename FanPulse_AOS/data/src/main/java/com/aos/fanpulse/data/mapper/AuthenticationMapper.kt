package com.aos.fanpulse.data.mapper

// [Data 계층 DTO] (서버 통신용)
import com.aos.fanpulse.data.remote.dto.GoogleLoginRequest as DataGoogleLoginRequest
import com.aos.fanpulse.data.remote.dto.RefreshRequest as DataRefreshRequest
import com.aos.fanpulse.data.remote.dto.TokenResponse as DataTokenResponse
import com.aos.fanpulse.data.remote.dto.AuthStatusResponse as DataAuthStatusResponse
import com.aos.fanpulse.data.remote.dto.AuthUserInfo as DataAuthUserInfo

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.GoogleLoginRequest as DomainGoogleLoginRequest
import com.aos.fanpulse.domain.model.RefreshRequest as DomainRefreshRequest
import com.aos.fanpulse.domain.model.TokenResponse as DomainTokenResponse
import com.aos.fanpulse.domain.model.AuthStatusResponse as DomainAuthStatusResponse
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

// ==========================================
// 2. Data -> Domain (서버 응답 처리)
// ==========================================
internal fun DataTokenResponse.toDomain(): DomainTokenResponse {
    return DomainTokenResponse(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken
    )
}

internal fun DataAuthStatusResponse.toDomain(): DomainAuthStatusResponse {
    return DomainAuthStatusResponse(
        authenticated = this.authenticated,
        // user가 null인 경우를 대비해 safely mapping 처리
        user = this.user?.toDomain()
    )
}

// ==========================================
// 3. Domain -> Data (서버 요청 전송)
// ==========================================
internal fun DomainGoogleLoginRequest.toData(): DataGoogleLoginRequest {
    return DataGoogleLoginRequest(
        idToken = this.idToken
    )
}

internal fun DomainRefreshRequest.toData(): DataRefreshRequest {
    return DataRefreshRequest(
        refreshToken = this.refreshToken
    )
}