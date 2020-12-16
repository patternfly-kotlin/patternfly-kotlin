package org.patternfly

import dev.fritz2.lenses.IdProvider
import kotlin.math.max
import kotlin.math.min

/**
 * Type alias for a [filter](https://www.patternfly.org/v4/guidelines/filters) used in [Items].
 */
public typealias ItemFilter<T> = (T) -> Boolean

/**
 * Immutable container for items used in [ItemStore]. Items can be paged, filtered, selected and sorted. Every modification to an instance of this class leads to a new instance with changed properties. Each item has to be uniquely identifiable using the specified [idProvider].
 *
 * The wrapped data can be obtained using different collections:
 *
 * - [all]: All items given when this instance was created.
 * - [items]: Sorted items after all filters have been applied.
 * - [page]: Paged version of [items].
 * - [selection]: Selected items.
 *
 * **Paging**
 *
 * Paging information is kept in an instance of [PageInfo]. Use [page] to get the items of the current page. Use a call to `copy(pageInfo = ...)` to change the paging.
 *
 * **Filter**
 *
 * Each filter is identified by an unique name. They are applied to all items. Filters can be added and removed using a call to `copy(filters = mapOf(...))`.
 *
 * **Select**
 *
 * Items can be selected using of of the following methods
 *
 * - [select]
 * - [selectOnly]
 * - [selectNone]
 * - [selectPage]
 * - [selectAll]
 * - [toggleSelection]
 *
 * Selected items are stored in the [selection] property.
 *
 * **Sort**
 *
 * To sort the items use an instance of [SortInfo]. Only one sort info at a time is supported.
 *
 * @param idProvider used to uniquely identify each item
 * @param all all items managed by this instance
 * @param items sorted items after all filters have been applied
 * @param pageInfo used to divide [items] into pages
 * @param filters predicates applied to [all] items
 * @param selected contains the selected **IDs**
 * @param sortInfo [Comparator]s for sorting [all] items
 *
 * @param T the type of the payload
 *
 * @sample org.patternfly.sample.ItemsSample.page
 * @sample org.patternfly.sample.ItemsSample.filter
 * @sample org.patternfly.sample.ItemsSample.select
 * @sample org.patternfly.sample.ItemsSample.sort
 */
public data class Items<T>(
    val idProvider: IdProvider<T, String>,
    val all: List<T> = emptyList(),
    val items: List<T> = emptyList(),
    val pageInfo: PageInfo = PageInfo(),
    val filters: Map<String, ItemFilter<T>> = emptyMap(),
    val selected: Set<String> = emptySet(), // selected identifiers
    val sortInfo: SortInfo<T>? = null
) {

    /**
     * The items of the current page. Filters and sorting (in that order) are applied to this list.
     */
    val page: List<T>
        get() = if (items.isEmpty()) listOf() else {
            val from = inBounds(pageInfo.range.first - 1, 0, items.size - 1)
            val to = inBounds(pageInfo.range.last, 1, items.size)
            items.subList(from, to)
        }

    /**
     * The selected items. No filters or sorting is applied to this list.
     */
    public val selection: List<T>
        get() = selected.mapNotNull { selectedId ->
            all.find { idProvider(it) == selectedId }
        }

    /**
     * Adds all items to the current items and returns a new instance.
     */
    public fun addAll(items: List<T>): Items<T> =
        copy(all = items, items = items, pageInfo = pageInfo.total(items.size))

    /**
     * Adds a filter and returns a new instance.
     */
    public fun addFilter(name: String, filter: ItemFilter<T>): Items<T> {
        val newFilters = filters + (name to filter)
        val newItems = applyFiltersAndSorting(newFilters, sortInfo)
        val newPageInfo = pageInfo.total(newItems.size)
        return copy(items = newItems, pageInfo = newPageInfo, filters = newFilters)
    }

    /**
     * Removes the specified filter and returns a new instance.
     */
    public fun removeFilter(name: String): Items<T> {
        val newFilters = filters - name
        val newItems = applyFiltersAndSorting(newFilters, sortInfo)
        val newPageInfo = pageInfo.total(newItems.size)
        return copy(items = newItems, pageInfo = newPageInfo, filters = newFilters)
    }

    /**
     * Applies the specified sort info and returns a new instance.
     */
    public fun sortWith(sortInfo: SortInfo<T>): Items<T> {
        val newItems = applyFiltersAndSorting(filters, sortInfo)
        return copy(items = newItems, sortInfo = sortInfo)
    }

    /**
     * Clears the selection and returns a new instance.
     */
    public fun selectNone(): Items<T> = copy(selected = emptySet())

    /**
     * Selects all items of the current [page] and returns a new instance.
     */
    public fun selectPage(): Items<T> = copy(selected = page.map { idProvider(it) }.toSet())

    /**
     * Selects [all] items and returns a new instance.
     */
    public fun selectAll(): Items<T> = copy(selected = items.map { idProvider(it) }.toSet())

    /**
     * (De)selects the specified item and returns a new instance.
     */
    public fun select(item: T, select: Boolean): Items<T> {
        val id = idProvider(item)
        val newSelection = if (select) selected + id else selected - id
        return copy(selected = newSelection)
    }

    /**
     * Selects only the specified item, unselects any other item and returns a new instance.
     */
    public fun selectOnly(item: T): Items<T> {
        return copy(selected = setOf(idProvider(item)))
    }

    /**
     * Toggles the selection of the specified item and returns a new instance.
     */
    public fun toggleSelection(item: T): Items<T> {
        val id = idProvider(item)
        val newSelection = if (id in selected) selected - id else selected + id
        return copy(selected = newSelection)
    }

    /**
     * Returns `true` if the specified item is selcted, `false` otherwise.
     */
    public fun isSelected(item: T): Boolean = idProvider(item) in selected

    override fun toString(): String = buildString {
        append("Items(all(").append(all.size).append(")")
        append(",items(").append(items.size).append(")")
        append(",pageInfo=").append(pageInfo)
        if (filters.isNotEmpty()) {
            append(",filters=").append(filters.keys)
        }
        append(",selected(").append(selected.size).append(")")
        append(")")
    }

    private fun applyFiltersAndSorting(filters: Map<String, ItemFilter<T>>, sortInfo: SortInfo<T>?): List<T> =
        if (filters.isEmpty()) {
            if (sortInfo != null) {
                all.sortedWith(sortInfo.effectiveComparator())
            } else all
        } else {
            var sequence = all.asSequence()
            filters.values.forEach { sequence = sequence.filter(it) }
            sortInfo?.let {
                sequence = sequence.sortedWith(it.effectiveComparator())
            }
            sequence.toList()
        }
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
        public val DEFAULT_PAGE_SIZES: Array<Int> = arrayOf(10, 20, 50, 100)
    }
}

/**
 * Simple class to hold information when sorting [Items]. Please note that the comparator is never reversed! It's only reversed 'in-place' when [ascending] == `false`
 *
 * @param id unique identifier
 * @param text text used in the UI
 * @param comparator comparator for this sort info
 * @param ascending whether the comparator is ascending or descending
 */
public class SortInfo<T>(
    public val id: String,
    public val text: String,
    internal val comparator: Comparator<T>,
    public val ascending: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SortInfo<*>
        if (id != other.id) return false
        if (ascending != other.ascending) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + ascending.hashCode()
        return result
    }

    override fun toString(): String {
        return "SortInfo(id=$id, ascending=$ascending)"
    }

    internal fun toggle(): SortInfo<T> = SortInfo(id, text, comparator, !ascending)

    internal fun effectiveComparator(): Comparator<T> = if (ascending) comparator else comparator.reversed()
}

private fun inBounds(value: Int, min: Int, max: Int): Int = min(max(min, value), max)
