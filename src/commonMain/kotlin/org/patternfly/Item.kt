package org.patternfly

import dev.fritz2.lenses.IdProvider
import kotlin.math.max
import kotlin.math.min

typealias ItemFilter<T> = (T) -> Boolean

data class Items<T>(
    val identifier: IdProvider<T, String>,
    val allItems: List<T> = emptyList(),
    val items: List<T> = emptyList(),
    val selected: Set<String> = emptySet(),
    val pageInfo: PageInfo = PageInfo()
) {
    val page: List<T>
        get() = if (items.isEmpty()) listOf() else {
            val from = safeBounds(pageInfo.range.first - 1, 0, items.size - 1)
            val to = safeBounds(pageInfo.range.last, 1, items.size)
            items.subList(from, to)
        }

    fun isSelected(item: T): Boolean = identifier(item) in selected

    override fun toString(): String =
        "Items(allItems=${allItems.size},items=${items.size},selected=${selected.size},pageInfo=$pageInfo)"
}

data class PageInfo(
    val pageSize: Int = DEFAULT_PAGE_SIZE,
    val page: Int = 0,
    val total: Int = 0,
    private val refreshCounter: Int = 0
) {
    val range: IntRange
        get() {
            val from = if (total == 0) 1 else page * pageSize + 1
            val to = min(total, from + pageSize - 1)
            return from..to
        }

    val pages: Int = safePages(pageSize, total)

    val firstPage: Boolean = page == 0

    val lastPagePage: Boolean = page == pages - 1

    internal fun gotoFirstPage(): PageInfo = copy(page = 0)
    internal fun gotoPreviousPage(): PageInfo = copy(page = safeBounds(page - 1, 0, pages - 1))
    internal fun gotoNextPage(): PageInfo = copy(page = safeBounds(page + 1, 0, pages - 1))
    internal fun gotoLastPage(): PageInfo = copy(page = pages - 1)
    internal fun gotoPage(page: Int): PageInfo = copy(page = safeBounds(page, 0, pages - 1))

    internal fun pageSize(pageSize: Int): PageInfo {
        val pages = safePages(pageSize, total)
        val page = safeBounds(page, 0, pages - 1)
        return copy(pageSize = pageSize, page = page)
    }

    internal fun total(total: Int): PageInfo {
        val pages = safePages(pageSize, total)
        val page = safeBounds(page, 0, pages - 1)
        return copy(page = page, total = total)
    }

    internal fun refresh(): PageInfo =
        copy(refreshCounter = if (refreshCounter == Int.MAX_VALUE) 0 else refreshCounter + 1)

    override fun toString(): String = "PageInfo(range=$range,page=($page/$pages),pageSize=$pageSize,total=$total)"

    private fun safePages(pageSize: Int, total: Int): Int {
        var pages = total / pageSize
        if (total % pageSize != 0) {
            pages++
        }
        return max(1, pages)
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        val DEFAULT_PAGE_SIZES: Array<Int> = arrayOf(10, 20, 50, 100)
    }
}

data class SortInfo<T>(val name: String, val comparator: Comparator<T>)

private fun safeBounds(value: Int, min: Int, max: Int): Int = min(max(min, value), max)