package com.notivest.portfolioservice.controller.dto

import org.springframework.data.domain.Page

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Long,
    val first: Boolean,
    val last: Boolean,
) {
    companion object {
        fun <T> fromPage(page: Page<T>): PagedResponse<T> {
            return PagedResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalPages = page.totalPages,
                totalElements = page.totalElements,
                first = page.isFirst,
                last = page.isLast,
            )
        }
    }
}
