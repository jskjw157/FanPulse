package com.aos.fanpulse.data.mapper

import com.aos.fanpulse.data.remote.dto.AuthUserInfo as DataAuthUserInfo

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.AuthUserInfo as DomainAuthUserInfo

internal fun DataAuthUserInfo.toDomain(): DomainAuthUserInfo {
    return DomainAuthUserInfo(
        id = this.id,
        email = this.email,
        username = this.username
    )
}