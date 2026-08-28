package com.aos.fanpulse.data.mapper

import com.aos.fanpulse.data.remote.dto.BaseResponse as DataBaseResponse
import com.aos.fanpulse.data.remote.dto.NewsListResponse as DataNewsListResponse
import com.aos.fanpulse.data.remote.dto.NewsItem as DataNewsItem
import com.aos.fanpulse.data.remote.dto.NewsDetail as DataNewsDetail

import com.aos.fanpulse.domain.model.BaseResponse as DomainBaseResponse
import com.aos.fanpulse.domain.model.NewsListResponse as DomainNewsListResponse
import com.aos.fanpulse.domain.model.NewsItem as DomainNewsItem
import com.aos.fanpulse.domain.model.NewsDetail as DomainNewsDetail

internal fun DataNewsItem.toDomain(): DomainNewsItem {
    return DomainNewsItem(
        id = this.id,
        artistId = this.artistId,
        title = this.title,
        thumbnailUrl = this.thumbnailUrl,
        sourceName = this.sourceName,
        category = this.category,
        publishedAt = this.publishedAt
    )
}

internal fun DataNewsDetail.toDomain(): DomainNewsDetail {
    return DomainNewsDetail(
        id = this.id,
        artistId = this.artistId,
        title = this.title,
        content = this.content,
        sourceUrl = this.sourceUrl,
        sourceName = this.sourceName,
        thumbnailUrl = this.thumbnailUrl,
        category = this.category,
        viewCount = this.viewCount,
        publishedAt = this.publishedAt,
        createdAt = this.createdAt
    )
}

internal fun DataNewsListResponse.toDomain(): DomainNewsListResponse {
    return DomainNewsListResponse(

        content = this.content.map { it.toDomain() },
        totalElements = this.totalElements,
        page = this.page,
        size = this.size,
        totalPages = this.totalPages
    )
}

internal fun <T, R> DataBaseResponse<T>.toDomain(mapData: (T) -> R): DomainBaseResponse<R> {
    return DomainBaseResponse(
        success = this.success,
        data = mapData(this.data)
    )
}