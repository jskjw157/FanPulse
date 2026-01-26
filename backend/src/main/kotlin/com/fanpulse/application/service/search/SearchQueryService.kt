package com.fanpulse.application.service.search

import com.fanpulse.application.dto.search.SearchResponse

interface SearchQueryService {
    fun search(query: String, limit: Int): SearchResponse
}
