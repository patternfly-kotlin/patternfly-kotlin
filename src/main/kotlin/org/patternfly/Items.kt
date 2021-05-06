package org.patternfly

import dev.fritz2.lenses.IdProvider

/**
 * Immutable collection of items used in [ItemsStore]. Items can be paged, filtered, selected and sorted. Every modification leads to a new instance with changed properties. Each item has to be uniquely identifiable using the specified [idProvider].
 *
 * The data inside an [Items] instance can be obtained using different properties:
 *
 * - [all]: All items given when this instance was created. This collection never changes.
 * - [items]: List after [sortInfo] and [filters] have been applied to [all].
 * - [page]: Paged version of [items].
 * - [selection]: Selected items based on [all].
 *
 * ### Paging
 *
 * Paging information is kept in an instance of [PageInfo]. Use [page] to get the items of the current page. Use a call to `copy(pageInfo = ...)` to change the paging.
 *
 * ### Filter
 *
 * Each filter is identified by an unique name. They are applied to [all] items. Filters can be added and removed using [addFilter] and [removeFilter].
 *
 * ### Select
 *
 * Items can be selected using of of the following methods
 *
 * - [select]: (de)select single items
 * - [selectOnly]: select one item and deselect all other items
 * - [selectNone]: select no items
 * - [selectPage]: select all items of the current [page]
 * - [selectAll]: select all items
 * - [toggleSelection]: toggle the selection of an item
 *
 * Selected items are stored in the [selection] property.
 *
 * ### Sort
 *
 * To sort the items use an instance of [SortInfo]. Only one sort info at a time is supported.
 *
 * @param idProvider used to uniquely identify each item
 * @param all all items managed by this instance
 * @param items list after [sortInfo] and [filters] have been applied to [all]
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
@Suppress("TooManyFunctions")
public data class Items<T>(
    val idProvider: IdProvider<T, String>,
    val all: List<T> = emptyList(),
    val items: List<T> = emptyList(),
    val pageInfo: PageInfo = PageInfo(),
    val filters: Map<String, ItemFilter<T>> = emptyMap(),
    val selected: Set<String> = emptySet(), // selected identifiers
    val singleSelection: Boolean = false,
    val sortInfo: SortInfo<T>? = null
) {

    /**
     * The items of the current page. Filters and sorting (in that order) are applied to this list.
     */
    public val page: List<T>
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
        // TODO Handle selection
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
     * Removes all filters.
     */
    public fun clearFilter(): Items<T> = copy(filters = emptyMap())

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
