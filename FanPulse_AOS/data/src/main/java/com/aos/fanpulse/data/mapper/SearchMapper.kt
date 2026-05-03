package com.aos.fanpulse.data.mapper

// [Data 계층 DTO] (서버 통신용)
import com.aos.fanpulse.data.remote.dto.SearchResponse as DataSearchResponse
import com.aos.fanpulse.data.remote.dto.LiveSearchSection as DataLiveSearchSection
import com.aos.fanpulse.data.remote.dto.NewsSearchSection as DataNewsSearchSection
import com.aos.fanpulse.data.remote.dto.SearchLiveItem as DataSearchLiveItem
import com.aos.fanpulse.data.remote.dto.SearchNewsItem as DataSearchNewsItem

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.SearchResponse as DomainSearchResponse
import com.aos.fanpulse.domain.model.LiveSearchSection as DomainLiveSearchSection
import com.aos.fanpulse.domain.model.NewsSearchSection as DomainNewsSearchSection
import com.aos.fanpulse.domain.model.SearchLiveItem as DomainSearchLiveItem
import com.aos.fanpulse.domain.model.SearchNewsItem as DomainSearchNewsItem

// ==========================================
// 1. 개별 아이템 매핑 (가장 먼저 선언)
// ==========================================

internal fun DataSearchLiveItem.toDomain(): DomainSearchLiveItem {
    return DomainSearchLiveItem(
        id = this.id,
        title = this.title,
        artistId = this.artistId,
        artistName = this.artistName,
        thumbnailUrl = this.thumbnailUrl,
        status = this.status,
        scheduledAt = this.scheduledAt
    )
}

internal fun DataSearchNewsItem.toDomain(): DomainSearchNewsItem {
    return DomainSearchNewsItem(
        id = this.id,
        title = this.title,
        summary = this.summary,
        sourceName = this.sourceName,
        publishedAt = this.publishedAt
    )
}


// ==========================================
// 2. 섹션 리스트 매핑 (위의 아이템 매핑 함수 사용)
// ==========================================

internal fun DataLiveSearchSection.toDomain(): DomainLiveSearchSection {
    return DomainLiveSearchSection(
        // 여기서 it은 DataSearchLiveItem 이므로 위의 함수를 참조합니다.
        items = this.items.map { it.toDomain() },
        totalCount = this.totalCount
    )
}

internal fun DataNewsSearchSection.toDomain(): DomainNewsSearchSection {
    return DomainNewsSearchSection(
        // 여기서 it은 DataSearchNewsItem 이므로 위의 함수를 참조합니다.
        items = this.items.map { it.toDomain() },
        totalCount = this.totalCount
    )
}


// ==========================================
// 3. 전체 검색 응답 매핑 (최종 조립)
// ==========================================

internal fun DataSearchResponse.toDomain(): DomainSearchResponse {
    return DomainSearchResponse(
        // 각 섹션 변수를 도메인 객체로 변환합니다. (2번에서 만든 함수들 호출)
        live = this.live.toDomain(),
        news = this.news.toDomain()
    )
}
