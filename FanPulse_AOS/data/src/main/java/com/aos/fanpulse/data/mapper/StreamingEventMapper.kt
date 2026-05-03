package com.aos.fanpulse.data.mapper

// [Data 계층 DTO] (서버 통신용)
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

// ==========================================
// 1. 단일 아이템 매핑 (항상 가장 위에 위치해야 함!)
// ==========================================

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

// ==========================================
// 2. 내부 리스트 포함 데이터 매핑 (커서 기반)
// ==========================================

internal fun DataStreamingEventCursorData.toDomain(): DomainStreamingEventCursorData {
    return DomainStreamingEventCursorData(
        // 위에서 정의한 DataStreamingEventItem.toDomain()을 사용합니다.
        items = this.items.map { it.toDomain() },
        nextCursor = this.nextCursor,
        hasMore = this.hasMore
    )
}

// ==========================================
// 3. 제네릭 공통 응답 래퍼 매핑 (Base & Page)
// ==========================================

/**
 * 단일 데이터 제네릭 매퍼 (StreamingBaseResponse)
 * 예: BaseResponse<StreamingEventDetail> 변환 시 사용
 */
internal fun <T, R> DataStreamingBaseResponse<T>.toDomain(mapData: (T) -> R): DomainStreamingBaseResponse<R> {
    return DomainStreamingBaseResponse(
        success = this.success,
        data = mapData(this.data)
    )
}

/**
 * 리스트 데이터 제네릭 매퍼 (StreamingPageResponse)
 * 예: StreamingPageResponse<StreamingEventSimpleItem> 변환 시 사용
 */
internal fun <T, R> DataStreamingPageResponse<T>.toDomain(mapItem: (T) -> R): DomainStreamingPageResponse<R> {
    return DomainStreamingPageResponse(
        // 리스트 안의 각 아이템(T)을 mapItem 함수를 통해 R로 변환합니다.
        content = this.content.map { mapItem(it) },
        totalElements = this.totalElements,
        page = this.page,
        size = this.size,
        totalPages = this.totalPages
    )
}