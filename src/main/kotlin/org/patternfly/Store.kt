package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.dom.html.handledBy
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.map

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
