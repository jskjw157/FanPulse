package com.aos.fanpulse.data.mapper

// [Data 계층 DTO] (서버 통신용)
import com.aos.fanpulse.data.remote.dto.ChartDetail as DataChartDetail
import com.aos.fanpulse.data.remote.dto.ChartEntry as DataChartEntry
import com.aos.fanpulse.data.remote.dto.ChartHistoryResponse as DataChartHistoryResponse
import com.aos.fanpulse.data.remote.dto.ChartHistoryItem as DataChartHistoryItem

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.ChartDetail as DomainChartDetail
import com.aos.fanpulse.domain.model.ChartEntry as DomainChartEntry
import com.aos.fanpulse.domain.model.ChartHistoryResponse as DomainChartHistoryResponse
import com.aos.fanpulse.domain.model.ChartHistoryItem as DomainChartHistoryItem

// ==========================================
// 1. ChartDetail 매핑 세트
// ==========================================

// 1-1. 단일 순위 항목 변환 (반드시 리스트 변환보다 위에 선언!)
internal fun DataChartEntry.toDomain(): DomainChartEntry {
    return DomainChartEntry(
        id = this.id,
        rank = this.rank,
        trackId = this.trackId,
        artistId = this.artistId,
        trackTitle = this.trackTitle,
        artistName = this.artistName,
        previousRank = this.previousRank,
        peakRank = this.peakRank,
        weeksOnChart = this.weeksOnChart,
        rankChange = this.rankChange,
        isNew = this.isNew
    )
}

// 1-2. 차트 전체 정보 변환 (내부 entries 리스트 변환 포함)
internal fun DataChartDetail.toDomain(): DomainChartDetail {
    return DomainChartDetail(
        id = this.id,
        chartType = this.chartType,
        chartDate = this.chartDate,
        // 여기서 it.toDomain()은 바로 위에 있는 1-1번 함수를 참조합니다.
        entries = this.entries.map { it.toDomain() },
        createdAt = this.createdAt
    )
}


// ==========================================
// 2. ChartHistory 매핑 세트
// ==========================================

// 2-1. 단일 히스토리 항목 변환
internal fun DataChartHistoryItem.toDomain(): DomainChartHistoryItem {
    return DomainChartHistoryItem(
        id = this.id,
        chartType = this.chartType,
        chartDate = this.chartDate,
        entryCount = this.entryCount
    )
}

// 2-2. 히스토리 목록 응답 변환 (내부 content 리스트 변환 포함)
internal fun DataChartHistoryResponse.toDomain(): DomainChartHistoryResponse {
    return DomainChartHistoryResponse(
        // 여기서 it.toDomain()은 바로 위에 있는 2-1번 함수를 참조합니다.
        content = this.content.map { it.toDomain() }
    )
}