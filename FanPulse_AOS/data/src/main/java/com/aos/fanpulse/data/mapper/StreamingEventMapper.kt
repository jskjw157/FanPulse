package com.aos.fanpulse.data.mapper

import com.aos.fanpulse.data.remote.dto.StreamingBaseResponse as DataStreamingBaseResponse
import com.aos.fanpulse.data.remote.dto.StreamingEventCursorData as DataStreamingEventCursorData
import com.aos.fanpulse.data.remote.dto.StreamingEventItem as DataStreamingEventItem
import com.aos.fanpulse.data.remote.dto.StreamingEventDetail as DataStreamingEventDetail
import com.aos.fanpulse.data.remote.dto.StreamingPageResponse as DataStreamingPageResponse
import com.aos.fanpulse.data.remote.dto.StreamingEventSimpleItem as DataStreamingEventSimpleItem

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.StreamingBaseResponse as DomainStreamingBaseResponse
import com.aos.fanpulse.domain.model.StreamingEventCursorData as DomainStreamingEventCursorData
import com.aos.fanpulse.domain.model.StreamingEventItem as DomainStreamingEventItem
import com.aos.fanpulse.domain.model.StreamingEventDetail as DomainStreamingEventDetail
import com.aos.fanpulse.domain.model.StreamingPageResponse as DomainStreamingPageResponse
import com.aos.fanpulse.domain.model.StreamingEventSimpleItem as DomainStreamingEventSimpleItem

internal fun DataStreamingEventItem.toDomain(): DomainStreamingEventItem {
    return DomainStreamingEventItem(
        id = this.id,
        title = this.title,
        artistId = this.artistId,
        artistName = this.artistName,
        thumbnailUrl = this.thumbnailUrl,
        status = this.status,
        scheduledAt = this.scheduledAt,
        startedAt = this.startedAt,
        viewerCount = this.viewerCount
    )
}

internal fun DataStreamingEventDetail.toDomain(): DomainStreamingEventDetail {
    return DomainStreamingEventDetail(
        id = this.id,
        title = this.title,
        description = this.description,
        artistId = this.artistId,
        artistName = this.artistName,
        thumbnailUrl = this.thumbnailUrl,
        streamUrl = this.streamUrl,
        status = this.status,
        scheduledAt = this.scheduledAt,
        startedAt = this.startedAt,
        endedAt = this.endedAt,
        viewerCount = this.viewerCount,
        createdAt = this.createdAt
    )
}

internal fun DataStreamingEventSimpleItem.toDomain(): DomainStreamingEventSimpleItem {
    return DomainStreamingEventSimpleItem(
        id = this.id,
        title = this.title,
        thumbnailUrl = this.thumbnailUrl,
        artistId = this.artistId,
        scheduledAt = this.scheduledAt,
        status = this.status,
        viewerCount = this.viewerCount,
        platform = this.platform
    )
}

internal fun DataStreamingEventCursorData.toDomain(): DomainStreamingEventCursorData {
    return DomainStreamingEventCursorData(
        items = this.items.map { it.toDomain() },
        nextCursor = this.nextCursor,
        hasMore = this.hasMore
    )
}

internal fun <T, R> DataStreamingBaseResponse<T>.toDomain(mapData: (T) -> R): DomainStreamingBaseResponse<R> {
    return DomainStreamingBaseResponse(
        success = this.success,
        data = mapData(this.data)
    )
}

internal fun <T, R> DataStreamingPageResponse<T>.toDomain(mapItem: (T) -> R): DomainStreamingPageResponse<R> {
    return DomainStreamingPageResponse(
        content = this.content.map { mapItem(it) },
        totalElements = this.totalElements,
        page = this.page,
        size = this.size,
        totalPages = this.totalPages
    )
}