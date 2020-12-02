package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class ItemStore<T>(public val identifier: IdProvider<T, String>) :
    RootStore<Items<T>>(Items(identifier)), PageInfoHandler {

    public val visible: Flow<List<T>> = data.map { it.page }
    public val selected: Flow<Int> = data.map { it.selected.size }
    public val selection: Flow<List<T>> = data.map { it.selection() }

    public val addAll: Handler<List<T>> = handle { items, newItems -> items.addAll(newItems) }

    public val addFilter: Handler<Pair<String, ItemFilter<T>>> = handle { items, (name, filter) ->
        items.addFilter(name, filter)
    }
    public val removeFilter: Handler<String> = handle { items, name -> items.removeFilter(name) }

    override val gotoFirstPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoFirstPage()) }
    override val gotoPreviousPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoPreviousPage()) }
    override val gotoNextPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoNextPage()) }
    override val gotoLastPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoLastPage()) }
    override val gotoPage: Handler<Int> = handle { items, page ->
        items.copy(pageInfo = items.pageInfo.gotoPage(page))
    }
    override val pageSize: Handler<Int> = handle { items, pageSize ->
        items.copy(pageInfo = items.pageInfo.pageSize(pageSize))
    }
    override val total: Handler<Int> = handle { items, _ -> items } // not implemented!
    override val refresh: Handler<Unit> = handle { it } // not implemented!

    public val preSelect: Handler<PreSelection> = handle { items, preSelection ->
        when(preSelection) {
            PreSelection.NONE -> items.selectNone()
            PreSelection.PAGE -> items.selectPage()
            PreSelection.ALL -> items.selectAll()
        }
    }
    public val selectNone: Handler<Unit> = handle { it.selectNone() }
    public val selectPage: Handler<Unit> = handle { it.selectPage() }
    public val selectAll: Handler<Unit> = handle { it.selectAll() }
    public val select: EmittingHandler<Pair<T, Boolean>, Pair<T, Boolean>> =
        handleAndEmit { items, (item, select) ->
            val updatedItems = items.select(item, select)
            emit(item to updatedItems.isSelected(item))
            updatedItems
        }
    public val toggleSelection: Handler<T> = handle { items, item -> items.toggleSelection(item) }

    public val sortWith: Handler<SortInfo<T>> = handle { items, sortInfo ->
        items.sortWith(sortInfo)
    }
    public val sortOrToggle: Handler<SortInfo<T>> = handle { items, sortInfo ->
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
}
