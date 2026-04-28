package com.aos.fanpulse.data.mapper


// [Data 계층 DTO] (서버 통신용)
import com.aos.fanpulse.data.remote.dto.ArtistListResponse as DataArtistListResponse
import com.aos.fanpulse.data.remote.dto.Artist as DataArtist
import com.aos.fanpulse.data.remote.dto.ArtistDetail as DataArtistDetail

// [Domain 계층 Model] (앱 내부 비즈니스 로직용)
import com.aos.fanpulse.domain.model.ArtistListResponse as DomainArtistListResponse
import com.aos.fanpulse.domain.model.Artist as DomainArtist
import com.aos.fanpulse.domain.model.ArtistDetail as DomainArtistDetail

// ==========================================
// 1. 단일 아티스트 매핑 (가장 먼저 선언 필수!)
// ==========================================
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

// ==========================================
// 2. 아티스트 리스트 매핑 (위의 단일 매핑 함수 사용)
// ==========================================
internal fun DataArtistListResponse.toDomain(): DomainArtistListResponse {
    return DomainArtistListResponse(
        // 여기서 it.toDomain()은 1번에서 선언한 단일 아티스트 매핑 함수를 참조합니다.
        content = this.content.map { it.toDomain() },
        totalElements = this.totalElements,
        page = this.page,
        size = this.size,
        totalPages = this.totalPages
    )
}

// ==========================================
// 3. 아티스트 상세 정보 매핑
// ==========================================
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