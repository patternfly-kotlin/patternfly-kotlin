package org.patternfly

import dev.fritz2.lenses.IdProvider
import kotlin.math.max
import kotlin.math.min

/** Immutable holder for items used in data components like [DataList]. */
data class Items<T>(
    private val identifier: IdProvider<T, String>,
    val allItems: List<T> = listOf(),
    val pageInfo: PageInfo = PageInfo(total = allItems.size),
    val selection: SelectionInfo<T> = SelectionInfo(identifier, mapOf()),
    val sortInfo: SortInfo<T>? = null
) {

    val visibleItems: List<T>
        get() {
            val sorted = if (sortInfo != null) {
                allItems.sortedWith(sortInfo.comparator)
            } else {
                allItems
            }
            return sorted.subList(max(0, pageInfo.range.first - 1), pageInfo.range.last)
        }

    internal fun clear(): Items<T> = Items(identifier, emptyList())
    internal fun addAll(list: List<T>): Items<T> = Items(identifier, list)

    override fun toString(): String = buildString {
        append("Items(pageInfo=$pageInfo")
        sortInfo?.let {
            append(",sortedBy=${it.name}")
        }
        append(",selected=${selection.items.size})")
    }
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

    private fun safeBounds(value: Int, min: Int, max: Int): Int = min(max(min, value), max)

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        val DEFAULT_PAGE_SIZES: Array<Int> = arrayOf(10, 20, 50, 100)
    }
}

data class SortInfo<T>(val name: String, val comparator: Comparator<T>)

// contains only selected items
data class SelectionInfo<T>(private val identifier: IdProvider<T, String>, private val selectionMap: Map<String, T>) {

    val items: List<T>
        get() = selectionMap.values.toList()

    internal fun select(item: T, select: Boolean): SelectionInfo<T> = copy(
        selectionMap = if (select) selectionMap + (identifier(item) to item) else selectionMap - identifier(item)
    )

    internal fun toggle(item: T): SelectionInfo<T> = copy(
        selectionMap = if (selectionMap.containsKey(identifier(item))) {
            selectionMap - identifier(item)
        } else {
            selectionMap + (identifier(item) to item)
        }
    )
}
