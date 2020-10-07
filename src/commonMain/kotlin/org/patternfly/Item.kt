package org.patternfly

import dev.fritz2.lenses.IdProvider
import kotlin.math.max
import kotlin.math.min

typealias ItemFilter<T> = (T) -> Boolean

data class Items<T>(
    val identifier: IdProvider<T, String>,
    val all: List<T> = emptyList(),
    val items: List<T> = emptyList(),
    val pageInfo: PageInfo = PageInfo(),
    val filters: Map<String, ItemFilter<T>> = emptyMap(),
    val selected: Set<String> = emptySet(), // selected identifiers
    val sortInfo: SortInfo<T>? = null
) {
    val page: List<T>
        get() = if (items.isEmpty()) listOf() else {
            val from = inBounds(pageInfo.range.first - 1, 0, items.size - 1)
            val to = inBounds(pageInfo.range.last, 1, items.size)
            items.subList(from, to)
        }

    fun addAll(items: List<T>): Items<T> =
        copy(all = items, items = items, pageInfo = pageInfo.total(items.size))

    fun addFilter(name: String, filter: ItemFilter<T>): Items<T> {
        val newFilters = filters + (name to filter)
        val newItems = items(newFilters, sortInfo)
        val newPageInfo = pageInfo.total(newItems.size)
        return copy(items = newItems, pageInfo = newPageInfo, filters = newFilters)
    }

    fun removeFilter(name: String): Items<T> {
        val newFilters = filters - name
        val newItems = items(newFilters, sortInfo)
        val newPageInfo = pageInfo.total(newItems.size)
        return copy(items = newItems, pageInfo = newPageInfo, filters = newFilters)
    }

    fun sortWith(sortInfo: SortInfo<T>): Items<T> {
        val newItems = items(filters, sortInfo)
        return copy(items = newItems, sortInfo = sortInfo)
    }

    fun selectNone(): Items<T> = copy(selected = emptySet())

    fun selectPage(): Items<T> = copy(selected = page.map { identifier(it) }.toSet())

    fun selectAll(): Items<T> = copy(selected = items.map { identifier(it) }.toSet())

    fun select(item: T, select: Boolean): Items<T> {
        val id = identifier(item)
        val newSelection = if (select) selected + id else selected - id
        return copy(selected = newSelection)
    }

    fun toggleSelection(item: T): Items<T> {
        val id = identifier(item)
        val newSelection = if (id in selected) selected - id else selected + id
        return copy(selected = newSelection)
    }

    fun isSelected(item: T): Boolean = identifier(item) in selected

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

    private fun items(filters: Map<String, ItemFilter<T>>, sortInfo: SortInfo<T>?): List<T> =
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

data class PageInfo(
    val pageSize: Int = DEFAULT_PAGE_SIZE,
    val page: Int = 0,
    val total: Int = 0,
    private val refreshCounter: Int = 0
) {
    init {
        require(pageSize > 0) { "Page size must be greater than 0" }
        require(page >= 0) { "Page must be greater than or equal 0" }
        require(total >= 0) { "Total must be greater than or equal 0" }
    }

    val range: IntRange
        get() {
            val from = if (total == 0) 1 else page * pageSize + 1
            val to = min(total, from + pageSize - 1)
            return from..to
        }

    val pages: Int = safePages(pageSize, total)

    val firstPage: Boolean = page == 0

    val lastPage: Boolean = page == pages - 1

    fun gotoFirstPage(): PageInfo = copy(page = 0)
    fun gotoPreviousPage(): PageInfo = copy(page = inBounds(page - 1, 0, pages - 1))
    fun gotoNextPage(): PageInfo = copy(page = inBounds(page + 1, 0, pages - 1))
    fun gotoLastPage(): PageInfo = copy(page = pages - 1)
    fun gotoPage(page: Int): PageInfo = copy(page = inBounds(page, 0, pages - 1))

    fun pageSize(pageSize: Int): PageInfo {
        val pages = safePages(pageSize, total)
        val page = inBounds(page, 0, pages - 1)
        return copy(pageSize = pageSize, page = page)
    }

    fun total(total: Int): PageInfo {
        val pages = safePages(pageSize, total)
        val page = inBounds(page, 0, pages - 1)
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

// Comparator is never reversed in SortInfo!
// It's reversed in Items.items() when ascending == false
class SortInfo<T>(
    val id: String,
    val text: String,
    internal val comparator: Comparator<T>,
    val ascending: Boolean = true
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

    internal fun toggle(): SortInfo<T> = SortInfo(id, text, comparator, false)

    internal fun effectiveComparator(): Comparator<T> = if (ascending) comparator else comparator.reversed()
}

private fun inBounds(value: Int, min: Int, max: Int): Int = min(max(min, value), max)