package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Interface representing item page contents
 */
public interface ItemPageContents<T> : WithIdProvider<T>, PageInfoHandler {
    // FIXME Should be pageItems.
    public val page: Flow<List<T>>

    // FIXME PageInfo should be an interface too...
    public val currPageInfo: Flow<PageInfo>
}

public sealed class SelectedItemsState {
    public data class SelectedItems(val itemIds: Set<String>) : SelectedItemsState()

    public object AllItems : SelectedItemsState()

    public object NoItems : SelectedItemsState()
}

public interface Selectable<T> {
    // Helpers to data components

    public fun isSelected(item: T): Flow<Boolean>

    /**
     * Flow containing the number of selected items.
     */
    // FIXME Should be something like selectedCount
    public val selected: Flow<Int>

    public val selectedItemsState: Flow<SelectedItemsState>

    public val preSelect: Handler<PreSelection>

    // Actual data components need these

    /**
     * Handler to deselect all items.
     */
    public val selectNone: Handler<Unit>

    /**
     * Handler to toggle the selection of the specified item.
     */
    public val toggleSelection: Handler<T>

    /**
     * Handler to only select the specified item and deselect all other items.
     */
    public val selectOnly: Handler<T>

    /**
     * Handler to (de)select the specified item.
     */
    public val select: Handler<Pair<T, Boolean>>

    /**
     * Handler to select all items.
     */
    public val selectAll: Handler<Unit>

    /**
     * Handler to select the items of the current page.
     */
    public val selectPage: Handler<Unit>
}

public interface SelectableItemPageContents<T> : ItemPageContents<T>, Selectable<T>

public interface SelectableAndSortableItemPageContents<T> : SelectableItemPageContents<T>, Sortable<T>

// This still has to be reworked... since there's still comparable use in SortOptions.kt
public interface Sortable<T> {
    public val currentSortInfo: Flow<SortInfo<T>?>

    /**
     * Handler to sort the items using the specified [SortInfo].
     */
    public val sortWith: Handler<SortInfo<T>>

    /**
     * Handler to sort the items using the specified [SortInfo], if the specified sort info is already used, the sort direction is reversed.
     */
    public val sortOrToggle: Handler<SortInfo<T>>
}

/**
 * Store used in data-driven components like [CardView], [DataList] and [DataTable]. The store uses an instance of [Items] as its data. It provides flows to get the properties of [Items] and handlers to modify it.
 *
 * @param idProvider used to uniquely identify each item
 * @param T the type of the payload
 */
public class ItemsStore<T>(override val idProvider: IdProvider<T, String> = { it.toString() }) :
    RootStore<Items<T>>(Items(idProvider)),
    PageInfoHandler,
    SelectableAndSortableItemPageContents<T> {

    /**
     * Flow containing the current page.
     */
    public override val page: Flow<List<T>> = data.map { it.page }

    public override val currPageInfo: Flow<PageInfo> = data.map { it.pageInfo }

    public override fun isSelected(item: T): Flow<Boolean> = data.map { it.isSelected(item) }

    public override val selectedItemsState: Flow<SelectedItemsState> = data.map {
        when {
            it.selected.isEmpty() -> SelectedItemsState.NoItems
            it.selected.size == it.items.size -> SelectedItemsState.AllItems
            else -> SelectedItemsState.SelectedItems(it.selected)
        }
    }

    /**
     * Flow containing the number of selected items.
     */
    public override val selected: Flow<Int> = data.map { it.selected.size }

    /**
     * Flow containing the selected items.
     */
    public val selection: Flow<List<T>> = data.map { it.selection }

    /**
     * Flow containing the single selected item.
     */
    public val singleSelection: Flow<T?> = data.map { it.selection.firstOrNull() }

    /**
     * Handler to add all specified items.
     */
    public val addAll: Handler<List<T>> = handle { items, newItems -> items.addAll(newItems) }

    /**
     * Handler to add the specified filter.
     */
    public val addFilter: Handler<Pair<String, ItemFilter<T>>> =
        handle { items, (name, filter) ->
            items.addFilter(name, filter)
        }

    /**
     * Handler to remove the specified filter.
     */
    public val removeFilter: Handler<String> = handle { items, name -> items.removeFilter(name) }

    /**
     * Removes all filters.
     */
    public val clearFilter: Handler<Unit> = handle { items -> items.clearFilter() }

    /**
     * Handler to go to the first page.
     */
    override val gotoFirstPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoFirstPage()) }

    /**
     * Handler to go to the previous (if any) page.
     */
    override val gotoPreviousPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoPreviousPage()) }

    /**
     * Handler to go to the next (if any) page.
     */
    override val gotoNextPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoNextPage()) }

    /**
     * Handler to go to the last page.
     */
    override val gotoLastPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoLastPage()) }

    /**
     * Handler to go to the specified page.
     */
    override val gotoPage: Handler<Int> = handle { items, page ->
        items.copy(pageInfo = items.pageInfo.gotoPage(page))
    }

    /**
     * Handler to set a new page size.
     */
    override val pageSize: Handler<Int> = handle { items, pageSize ->
        items.copy(pageInfo = items.pageInfo.pageSize(pageSize))
    }

    /**
     * Handler to set a new number of total items.
     */
    override val total: Handler<Int> = handle { items, _ -> items } // not implemented!

    /**
     * Does nothing!
     */
    override val refresh: Handler<Unit> = handle { it } // not implemented!

    /**
     * Handler to select items depending on the value of [PreSelection].
     */
    public override val preSelect: Handler<PreSelection> = handle { items, preSelection ->
        when (preSelection) {
            PreSelection.NONE -> items.selectNone()
            PreSelection.PAGE -> items.selectPage()
            PreSelection.ALL -> items.selectAll()
        }
    }

    /**
     * Handler to deselect all items.
     */
    public override val selectNone: Handler<Unit> = handle { it.selectNone() }

    /**
     * Handler to select the items of the current page.
     */
    public override val selectPage: Handler<Unit> = handle { it.selectPage() }

    /**
     * Handler to select all items.
     */
    public override val selectAll: Handler<Unit> = handle { it.selectAll() }

    /**
     * Handler to (de)select the specified item.
     */
    public override val select: Handler<Pair<T, Boolean>> = handle { items, (item, select) ->
        items.select(item, select)
    }

    /**
     * Handler to only select the specified item and deselect all other items.
     */
    public override val selectOnly: Handler<T> = handle { items, item -> items.selectOnly(item) }

    /**
     * Handler to toggle the selection of the specified item.
     */
    public override val toggleSelection: Handler<T> = handle { items, item -> items.toggleSelection(item) }

    public override val currentSortInfo: Flow<SortInfo<T>?> = data.map { it.sortInfo }

    /**
     * Handler to sort the items using the specified [SortInfo].
     */
    public override val sortWith: Handler<SortInfo<T>> = handle { items, sortInfo ->
        items.sortWith(sortInfo)
    }

    /**
     * Handler to sort the items using the specified [SortInfo], if the specified sort info is already used, the sort direction is reversed.
     */
    public override val sortOrToggle: Handler<SortInfo<T>> = handle { items, sortInfo ->
        val newSortInfo = if (items.sortInfo == null) {
            sortInfo
        } else {
            if (items.sortInfo.id == sortInfo.id) {
                items.sortInfo.toggle()
            } else {
                sortInfo
            }
        }
        items.sortWith(newSortInfo)
    }

    internal companion object {
        internal val NOOP: ItemsStore<Unit> = ItemsStore()
    }
}

public enum class PreSelection(public val text: String) {
    NONE("Select none"), PAGE("Select visible"), ALL("Select all")
}
