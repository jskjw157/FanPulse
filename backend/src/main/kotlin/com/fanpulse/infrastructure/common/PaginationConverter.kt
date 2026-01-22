package com.fanpulse.infrastructure.common

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.common.Sort
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.data.domain.Sort as SpringSort

/**
 * Spring Pagination과 Domain Pagination 간 변환 유틸리티
 */
object PaginationConverter {

    /**
     * Domain PageRequest를 Spring Pageable로 변환
     */
    fun toSpringPageable(pageRequest: PageRequest): SpringPageRequest {
        val sort = pageRequest.sort?.let { domainSort ->
            val direction = when (domainSort.direction) {
                Sort.Direction.ASC -> SpringSort.Direction.ASC
                Sort.Direction.DESC -> SpringSort.Direction.DESC
            }
            SpringSort.by(direction, domainSort.property)
        } ?: SpringSort.unsorted()

        return SpringPageRequest.of(pageRequest.page, pageRequest.size, sort)
    }

    /**
     * Spring Pageable을 Domain PageRequest로 변환
     */
    fun toDomainPageRequest(pageable: org.springframework.data.domain.Pageable): PageRequest {
        val sort = if (pageable.sort.isSorted) {
            val order = pageable.sort.iterator().next()
            val direction = when (order.direction) {
                SpringSort.Direction.ASC -> Sort.Direction.ASC
                SpringSort.Direction.DESC -> Sort.Direction.DESC
                else -> Sort.Direction.ASC
            }
            Sort(property = order.property, direction = direction)
        } else {
            null
        }

        return PageRequest(
            page = pageable.pageNumber,
            size = pageable.pageSize,
            sort = sort
        )
    }

    /**
     * Spring Page를 Domain PageResult로 변환
     */
    fun <T> toDomainPageResult(springPage: Page<T>, pageRequest: PageRequest): PageResult<T> {
        return PageResult(
            content = springPage.content,
            totalElements = springPage.totalElements,
            pageRequest = pageRequest
        )
    }
}
