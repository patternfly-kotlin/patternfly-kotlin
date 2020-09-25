package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ItemStore<T>(val identifier: IdProvider<T, String>) :
    RootStore<Items<T>>(Items(identifier)), PageInfoHandler {

    val visible: Flow<List<T>> = data.map { it.page }
    val selected: Flow<Int> = data.map { it.selected.size }

    val addAll: Handler<List<T>> = handle { items, newItems -> items.addAll(newItems) }

    val addFilter: Handler<Pair<String, ItemFilter<T>>> = handle { items, (name, filter) ->
        items.addFilter(name, filter)
    }
    val removeFilter: Handler<String> = handle { items, name -> items.removeFilter(name) }

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

    val selectNone: Handler<Unit> = handle { it.selectNone() }
    val selectVisible: Handler<Unit> = handle { it.selectPage() }
    val selectAll: Handler<Unit> = handle { it.selectAll() }
    val select: Handler<Pair<T, Boolean>> = handle { items, (item, select) ->
        items.select(item, select)
    }
    val toggleSelection: Handler<T> = handle { items, item -> items.toggleSelection(item) }

    val sortWith: Handler<Comparator<T>> = handle { items, comparator ->
        items.sortWith(comparator)
    }
}
