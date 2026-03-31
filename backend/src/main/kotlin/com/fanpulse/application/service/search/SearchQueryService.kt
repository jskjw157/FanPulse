package com.fanpulse.application.service.search

import com.fanpulse.application.dto.search.SearchResponse

/**
 * 통합 검색 쿼리 서비스.
 * 라이브 스트리밍, 뉴스 등 여러 도메인을 통합 검색한다.
 */
interface SearchQueryService {

    /**
     * 코루틴 병렬 처리로 LIVE/SCHEDULED/ENDED 스트리밍과 뉴스를 동시 검색한다.
     *
     * @param query 검색 키워드
     * @param limit 카테고리별 최대 결과 수
     */
    fun search(query: String, limit: Int): SearchResponse
}
