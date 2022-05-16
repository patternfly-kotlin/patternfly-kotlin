package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.dom.html.handledBy
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

// ------------------------------------------------------ data binding

internal fun <T> Map<String, T>.dataBinding(
    idStore: SingleIdStore,
    dataStore: Store<T?>,
    idProvider: IdProvider<T, String>
) {
    idStore.data.map { this[it] } handledBy dataStore.update
    dataStore.data.map { if (it != null) idProvider(it) else null } handledBy idStore.update
}

internal fun <T> Map<String, T>.dataBinding(
    idStore: MultiIdStore,
    dataStore: Store<List<T>>,
    idProvider: IdProvider<T, String>
) {
    idStore.data.map { ids ->
        this.filterKeys { it in ids }
    }.map { it.values.toList() } handledBy dataStore.update
    dataStore.data.map { data -> data.map { idProvider(it) } } handledBy idStore.update
}

// ------------------------------------------------------ flag or flow

internal class FlagOrFlow(private val id: String) {
    internal var flag: Boolean? = null
    internal var flow: Flow<Boolean>? = null

    internal fun singleSelect(idSelection: SingleIdStore) {
        flag?.let { selected ->
            if (selected) {
                idSelection.update(id)
            }
        }
        flow?.let { selected ->
            // setup one-way selection data binding: flow -> id
            selected.filter { it }.map { id } handledBy idSelection.update
            selected.filter { !it }.map { null } handledBy idSelection.update
        }
    }

    internal fun multiSelect(idSelection: MultiIdStore) {
        flag?.let { selected ->
            if (selected) {
                idSelection.select(id to true)
            }
        }
        flow?.let { selected ->
            selected.map { id to it } handledBy idSelection.select
        }
    }

    internal fun disable(disabledIds: MultiIdStore) {
        flag?.let { disabled ->
            if (disabled) {
                disabledIds.disable(id)
            }
        }
        flow?.let { disabled ->
            // setup disabled one-way disabled data binding: flow -> id
            disabled.filter { it }.map { id } handledBy disabledIds.disable
            disabled.filter { !it }.map { id } handledBy disabledIds.enable
        }
    }
}

// ------------------------------------------------------ stores

internal class OnOffStore : RootStore<Boolean>(false) {
    val toggle = handle { !it }
}

internal class SingleIdStore : RootStore<String?>(null)

internal class MultiIdStore : RootStore<List<String>>(emptyList()) {

    val disable: Handler<String> = handle { ids, id ->
        ids + id
    }

    val enable: Handler<String> = handle { ids, id ->
        ids - id
    }

    val select: Handler<Pair<String, Boolean>> = handle { ids, (id, select) ->
        if (select) ids + id else ids - id
    }

    val toggle: Handler<String> = handle { ids, id ->
        if (ids.contains(id)) ids - id else ids + id
    }
}

internal class HeadTailItemStore<I> : RootStore<List<I>>(emptyList()) {

    private var itemsInStore: Boolean = false
    private val headItems: MutableList<I> = mutableListOf()
    private val tailItems: MutableList<I> = mutableListOf()

    val headTailItems: List<I>
        get() = headItems + tailItems

    val allItems: Flow<List<I>>
        get() = data.map { items -> headItems + items + tailItems }

    fun add(item: I): Boolean = (if (itemsInStore) tailItems else headItems).add(item)

    fun <T> collect(values: Flow<List<T>>, collector: FlowCollector<List<T>>) {
        (MainScope() + job).launch {
            values.collect(collector)
        }
    }

    fun <T> update(values: List<T>, transform: (T) -> I) {
        update(values.map(transform)) // invoke handler from super
        itemsInStore = true
    }
}
