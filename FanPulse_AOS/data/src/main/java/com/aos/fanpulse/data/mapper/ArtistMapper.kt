package com.aos.fanpulse.data.mapper

import com.aos.fanpulse.data.remote.dto.ArtistListResponse as DataArtistListResponse
import com.aos.fanpulse.data.remote.dto.Artist as DataArtist
import com.aos.fanpulse.data.remote.dto.ArtistDetail as DataArtistDetail

// [Domain 계층 Model] (앱 내부 비즈니스 로직용)
import com.aos.fanpulse.domain.model.ArtistListResponse as DomainArtistListResponse
import com.aos.fanpulse.domain.model.Artist as DomainArtist
import com.aos.fanpulse.domain.model.ArtistDetail as DomainArtistDetail

internal fun DataArtist.toDomain(): DomainArtist {
    return DomainArtist(
        id = this.id,
        name = this.name,
        englishName = this.englishName,
        agency = this.agency,
        profileImageUrl = this.profileImageUrl,
        isGroup = this.isGroup
    )
}

internal fun DataArtistListResponse.toDomain(): DomainArtistListResponse {
    return DomainArtistListResponse(
        content = this.content.map { it.toDomain() },
        totalElements = this.totalElements,
        page = this.page,
        size = this.size,
        totalPages = this.totalPages
    )
}

internal fun DataArtistDetail.toDomain(): DomainArtistDetail {
    return DomainArtistDetail(
        id = this.id,
        name = this.name,
        englishName = this.englishName,
        agency = this.agency,
        description = this.description,
        profileImageUrl = this.profileImageUrl,
        isGroup = this.isGroup,
        members = this.members,
        active = this.active,
        debutDate = this.debutDate,
        createdAt = this.createdAt
    )
}