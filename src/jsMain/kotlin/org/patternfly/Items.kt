package org.patternfly

import dev.fritz2.lenses.IdProvider
import kotlin.math.max
import kotlin.math.min

const val DEFAULT_PAGE_SIZE = 50

/** Immutable holder for items used in [ItemStore]. */
data class Items<T>(
    private val identifier: IdProvider<T, String>,
    val allItems: List<T> = listOf(),
    val pageInfo: PageInfo = PageInfo(DEFAULT_PAGE_SIZE, 0, allItems.size),
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
            val paged = sorted.subList(max(0, pageInfo.from - 1), pageInfo.to)
            return paged
        }

    internal fun clear(): Items<T> = Items(identifier, emptyList())

    internal fun addAll(list: List<T>): Items<T> = Items(identifier, list)

    internal fun gotoPage(page: Int): Items<T> = Items(
        identifier,
        allItems,
        pageInfo.gotoPage(page),
        selection,
        sortInfo
    )

    internal fun pageSize(pageSize: Int): Items<T> = Items(
        identifier,
        allItems,
        pageInfo.pageSize(pageSize),
        selection,
        sortInfo
    )

    internal fun selectNone(): Items<T> = Items(
        identifier,
        allItems,
        pageInfo,
        SelectionInfo(identifier, mapOf()),
        sortInfo
    )

    internal fun select(item: T, select: Boolean): Items<T> = Items(
        identifier,
        allItems,
        pageInfo,
        selection.select(item, select),
        sortInfo
    )

    internal fun toggleSelection(item: T): Items<T> = Items(
        identifier,
        allItems,
        pageInfo,
        selection.toggle(item),
        sortInfo
    )

    internal fun selectVisible(): Items<T> = Items(
        identifier,
        allItems,
        pageInfo,
        SelectionInfo(identifier, visibleItems.associateBy(identifier)),
        sortInfo
    )

    internal fun selectAll(): Items<T> = Items(
        identifier,
        allItems,
        pageInfo,
        SelectionInfo(identifier, allItems.associateBy(identifier)),
        sortInfo
    )

    internal fun sortBy(name: String, comparator: Comparator<T>): Items<T> = Items(
        identifier,
        allItems,
        pageInfo,
        selection,
        SortInfo(name, comparator)
    )

    override fun toString(): String = buildString {
        append("Items [${pageInfo.from}, ${pageInfo.to}) of ${pageInfo.total} items, page ${pageInfo.page} of ${pageInfo.pages} with size ${pageInfo.page}")
        sortInfo?.let {
            append(", sorted by ${it.name}")
        }
        append(", ${selection.items.size} item(s) selected")
    }
}

data class PageInfo(val pageSize: Int, val page: Int, val total: Int) {

    val from: Int
        get() = if (total == 0) 0 else page * pageSize + 1

    val to: Int
        get() {
            return min(total, from + pageSize - 1)
        }

    val pages: Int
        get() {
            var pages = total / pageSize
            if (total % pageSize != 0) {
                pages++
            }
            return max(1, pages)
        }

    internal fun gotoPage(page: Int): PageInfo = PageInfo(pageSize, page, total)

    internal fun pageSize(pageSize: Int): PageInfo = PageInfo(pageSize, page, total)
}

data class SortInfo<T>(val name: String, val comparator: Comparator<T>)

// contains only selected items
data class SelectionInfo<T>(private val identifier: IdProvider<T, String>, private val selectionMap: Map<String, T>) {

    val items: List<T>
        get() = selectionMap.values.toList()

    internal fun select(item: T, select: Boolean): SelectionInfo<T> = SelectionInfo(
        identifier,
        if (select) selectionMap + (identifier(item) to item) else selectionMap - identifier(item)
    )

    internal fun toggle(item: T): SelectionInfo<T> = SelectionInfo(
        identifier,
        if (selectionMap.containsKey(identifier(item))) {
            selectionMap - identifier(item)
        } else {
            selectionMap + (identifier(item) to item)
        }
    )
}
