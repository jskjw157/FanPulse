package com.aos.fanpulse.data.mapper

// [Data 계층 DTO] (서버 통신용)
import com.aos.fanpulse.data.remote.dto.BaseResponse as DataBaseResponse
import com.aos.fanpulse.data.remote.dto.NewsListResponse as DataNewsListResponse
import com.aos.fanpulse.data.remote.dto.NewsItem as DataNewsItem
import com.aos.fanpulse.data.remote.dto.NewsDetail as DataNewsDetail

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.BaseResponse as DomainBaseResponse
import com.aos.fanpulse.domain.model.NewsListResponse as DomainNewsListResponse
import com.aos.fanpulse.domain.model.NewsItem as DomainNewsItem
import com.aos.fanpulse.domain.model.NewsDetail as DomainNewsDetail

// ==========================================
// 1. 뉴스 단일 항목 및 상세 정보 매핑
// ==========================================

// 1-1. 단일 뉴스 아이템 (리스트 변환을 위해 반드시 가장 위에 선언!)
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

// 1-2. 뉴스 상세 정보
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

// ==========================================
// 2. 뉴스 리스트 응답 매핑
// ==========================================
internal fun DataNewsListResponse.toDomain(): DomainNewsListResponse {
    return DomainNewsListResponse(
        // 여기서 it.toDomain()은 위의 1-1 함수를 정상적으로 참조합니다.
        content = this.content.map { it.toDomain() },
        totalElements = this.totalElements,
        page = this.page,
        size = this.size,
        totalPages = this.totalPages
    )
}

// ==========================================
// 3. 제네릭 공통 응답 (BaseResponse) 매핑
// ==========================================
/**
 * 제네릭 타입 T를 R로 변환하기 위해, 내부 데이터를 어떻게 바꿀지(mapData)를 함수 형태로 받습니다.
 */
internal fun <T, R> DataBaseResponse<T>.toDomain(mapData: (T) -> R): DomainBaseResponse<R> {
    return DomainBaseResponse(
        success = this.success,
        data = mapData(this.data) // 넘겨받은 변환 함수를 실행
    )
}