package com.aos.fanpulse.data.mapper

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

internal fun DataLiveSearchSection.toDomain(): DomainLiveSearchSection {
    return DomainLiveSearchSection(
        items = this.items.map { it.toDomain() },
        totalCount = this.totalCount
    )
}

internal fun DataNewsSearchSection.toDomain(): DomainNewsSearchSection {
    return DomainNewsSearchSection(
        items = this.items.map { it.toDomain() },
        totalCount = this.totalCount
    )
}

internal fun DataSearchResponse.toDomain(): DomainSearchResponse {
    return DomainSearchResponse(
        live = this.live.toDomain(),
        news = this.news.toDomain()
    )
}
