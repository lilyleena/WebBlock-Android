package com.kshrd.features.remote.dto

import kotlinx.serialization.Serializable

/** Every v2 list: `{items, page (zero-based), size, totalItems}`. */
@Serializable
data class PageDto<T>(
    val items: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalItems: Long = 0,
)

/** Reads every page of a list until it has `totalItems` (or a page comes back empty). */
suspend fun <T> allPages(fetch: suspend (page: Int) -> PageDto<T>): List<T> {
    val items = mutableListOf<T>()
    var page = 0
    while (true) {
        val result = fetch(page)
        items += result.items
        if (result.items.isEmpty() || items.size >= result.totalItems) return items
        page++
    }
}
