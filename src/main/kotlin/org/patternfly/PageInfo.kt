package org.patternfly

import kotlin.math.max
import kotlin.math.min

/**
 * Immutable class for paging over [Items]. Every modification to an instance of this class leads to a new instance with changed properties.
 *
 * @param pageSize the size of one page
 * @param page the current page
 * @param total total number of items
 */
public data class PageInfo(
    val pageSize: Int = DEFAULT_PAGE_SIZE,
    val page: Int = 0,
    val total: Int = 0,
    private val signalUpdate: Int = 0
) {
    init {
        require(pageSize > 0) { "Page size must be greater than 0" }
        require(page >= 0) { "Page must be greater than or equal 0" }
        require(total >= 0) { "Total must be greater than or equal 0" }
    }

    /**
     * Range for the current page
     */
    val range: IntRange
        get() {
            val from = if (total == 0) 1 else page * pageSize + 1
            val to = min(total, from + pageSize - 1)
            return from..to
        }

    /**
     * Number of pages
     */
    val pages: Int = safePages(pageSize, total)

    /**
     * Whether the current [page] is the first one.
     */
    val firstPage: Boolean = page == 0

    /**
     * Whether the current [page] is the last one.
     */
    val lastPage: Boolean = page == pages - 1

    /**
     * Goes to the first page and returns a new instance.
     */
    public fun gotoFirstPage(): PageInfo = copy(page = 0)

    /**
     * Goes to the previous page (if any) and returns a new instance.
     */
    public fun gotoPreviousPage(): PageInfo = copy(page = inBounds(page - 1, 0, pages - 1))

    /**
     * Goes to the next page (if any) and returns a new instance.
     */
    public fun gotoNextPage(): PageInfo = copy(page = inBounds(page + 1, 0, pages - 1))

    /**
     * Goes to the last page and returns a new instance.
     */
    public fun gotoLastPage(): PageInfo = copy(page = pages - 1)

    /**
     * Goes to the specified page and returns a new instance.
     */
    public fun gotoPage(page: Int): PageInfo = copy(page = inBounds(page, 0, pages - 1))

    /**
     * Sets a new page size and returns a new instance.
     */
    public fun pageSize(pageSize: Int): PageInfo {
        val pages = safePages(pageSize, total)
        val page = inBounds(page, 0, pages - 1)
        return copy(pageSize = pageSize, page = page)
    }

    /**
     * Sets a new total number of items and returns a new instance.
     */
    public fun total(total: Int): PageInfo {
        val pages = safePages(pageSize, total)
        val page = inBounds(page, 0, pages - 1)
        return copy(page = page, total = total)
    }

    internal fun refresh(): PageInfo =
        copy(signalUpdate = if (signalUpdate == Int.MAX_VALUE) 0 else signalUpdate + 1)

    override fun toString(): String = "PageInfo(range=$range,page=($page/$pages),pageSize=$pageSize,total=$total)"

    private fun safePages(pageSize: Int, total: Int): Int {
        var pages = total / pageSize
        if (total % pageSize != 0) {
            pages++
        }
        return max(1, pages)
    }

    public companion object {
        public const val DEFAULT_PAGE_SIZE: Int = 10
        public val DEFAULT_PAGE_SIZES: IntArray = intArrayOf(10, 20, 50, 100)
    }
}

internal fun inBounds(value: Int, min: Int, max: Int): Int = min(max(min, value), max)
