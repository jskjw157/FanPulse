package com.fanpulse.domain.common

import kotlin.math.ceil

/**
 * Domain-level pagination request.
 * 프레임워크 독립적인 페이지네이션 요청 객체
 */
data class PageRequest(
    val page: Int,
    val size: Int,
    val sort: Sort? = null
) {
    init {
        require(page >= 0) { "Page number must be non-negative" }
        require(size > 0) { "Page size must be positive" }
    }

    /**
     * 오프셋을 계산합니다.
     * @return page * size
     */
    fun offset(): Int = page * size
}

/**
 * Domain-level sort specification.
 * 프레임워크 독립적인 정렬 명세
 */
data class Sort(
    val property: String,
    val direction: Direction
) {
    init {
        require(property.isNotBlank()) { "Sort property cannot be blank" }
    }

    enum class Direction {
        ASC, DESC
    }
}

/**
 * Domain-level page result.
 * 프레임워크 독립적인 페이지네이션 결과
 */
data class PageResult<T>(
    val content: List<T>,
    val totalElements: Long,
    val pageRequest: PageRequest
) {
    val page: Int get() = pageRequest.page
    val size: Int get() = pageRequest.size
    val sort: Sort? get() = pageRequest.sort

    /**
     * 총 페이지 수를 계산합니다.
     */
    val totalPages: Int
        get() = if (totalElements == 0L) {
            0
        } else {
            ceil(totalElements.toDouble() / size).toInt()
        }

    /**
     * 첫 페이지 여부
     */
    val isFirst: Boolean
        get() = page == 0

    /**
     * 마지막 페이지 여부
     */
    val isLast: Boolean
        get() = page >= totalPages - 1

    /**
     * 다음 페이지 존재 여부
     */
    val hasNext: Boolean
        get() = page < totalPages - 1

    /**
     * 이전 페이지 존재 여부
     */
    val hasPrevious: Boolean
        get() = page > 0
}
