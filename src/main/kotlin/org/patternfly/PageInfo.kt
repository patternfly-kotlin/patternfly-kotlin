package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import kotlin.math.max
import kotlin.math.min

/**
 * Common interface meant to be implemented by stores which have a [PageInfo] instance.
 */
public interface PageInfoHandler {

    /**
     * Handler to go to the first page.
     */
    public val gotoFirstPage: Handler<Unit>

    /**
     * Handler to go to the previous (if any) page.
     */
    public val gotoPreviousPage: Handler<Unit>

    /**
     * Handler to go to the next (if any) page.
     */
    public val gotoNextPage: Handler<Unit>

    /**
     * Handler to go to the last page.
     */
    public val gotoLastPage: Handler<Unit>

    /**
     * Handler to go to the specified page.
     */
    public val gotoPage: Handler<Int>

    /**
     * Handler to set a new page size.
     */
    public val pageSize: Handler<Int>

    /**
     * Handler to set a new number of total items.
     */
    public val total: Handler<Int>

    /**
     * Handler to refresh the [PageInfo] instance.
     */
    public val refresh: Handler<Unit>
}

/**
 * Store holding a [PageInfo] instance.
 */
public class PageInfoStore(pageInfo: PageInfo) : RootStore<PageInfo>(pageInfo), PageInfoHandler {

    /**
     * Handler to go to the first page.
     */
    override val gotoFirstPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoFirstPage() }

    /**
     * Handler to go to the previous (if any) page.
     */
    override val gotoPreviousPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoPreviousPage() }

    /**
     * Handler to go to the next (if any) page.
     */
    override val gotoNextPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoNextPage() }

    /**
     * Handler to go to the last page.
     */
    override val gotoLastPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoLastPage() }

    /**
     * Handler to go to the specified page.
     */
    override val gotoPage: Handler<Int> = handle { pageInfo, page -> pageInfo.gotoPage(page) }

    /**
     * Handler to set a new page size.
     */
    override val pageSize: Handler<Int> = handle { pageInfo, pageSize -> pageInfo.pageSize(pageSize) }

    /**
     * Handler to set a new number of total items.
     */
    override val total: Handler<Int> = handle { pageInfo, total -> pageInfo.total(total) }

    /**
     * Handler to refresh the [PageInfo] instance.
     */
    override val refresh: Handler<Unit> = handle { pageInfo -> pageInfo.refresh() }
}

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
            val from = if (total == 0) 0 else page * pageSize + 1
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

    /**
     * Contains global constants common to all [PageInfo] instances.
     */
    public companion object {

        /**
         * Default page size.
         */
        public const val DEFAULT_PAGE_SIZE: Int = 10

        /**
         * Default page sizes.
         */
        public val DEFAULT_PAGE_SIZES: IntArray = intArrayOf(10, 20, 50, 100)
    }
}

internal fun inBounds(value: Int, min: Int, max: Int): Int = min(max(min, value), max)
