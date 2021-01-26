package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * Creates an instance of [Entries] containing [Item]s from the specified code block and updates the store given as receiver.
 *
 * @receiver the store to update
 *
 * @param block function executed in the context of an [ItemsBuilder]
 *
 * @sample org.patternfly.sample.EntrySample.storeItems
 */
public fun <T> EntriesStore<T>.updateItems(block: ItemsBuilder<T>.() -> Unit = {}) {
    update(ItemsBuilder<T>(idProvider, itemSelection).apply(block).build())
}

/**
 * Creates an instance of [Entries] containing [Group]s from the specified code block and updates the store given as receiver.
 *
 * @receiver the store to update
 *
 * @param block function executed in the context of an [GroupsBuilder]
 */
public fun <T> EntriesStore<T>.updateGroups(block: GroupsBuilder<T>.() -> Unit = {}) {
    update(GroupsBuilder<T>(idProvider, itemSelection).apply(block).build())
}

/**
 * Abstract store for [Entries].
 *
 * The flows in this store use [Item] instead of the wrapped data. Use one of the [unwrap] functions to get the actual payload.
 *
 * @sample org.patternfly.sample.EntrySample.unwrap
 */
public abstract class EntriesStore<T> internal constructor(
    override val idProvider: IdProvider<T, String>,
    public val itemSelection: ItemSelection
) : RootStore<Entries<T>>(Entries(idProvider, itemSelection, emptyList())),
    WithIdProvider<T> {

    @Suppress("LeakingThis")
    internal val select: Handler<T> = handle { entries, data ->
        entries.select(data)
    }

    /**
     * Flow with the List of [entries][Entry] after an optional filter has been applied.
     */
    public val entries: Flow<List<Entry<T>>>
        get() = data.map { it.entries }

    /**
     * Flow with the list of selected [Item]s.
     */
    public val selection: Flow<List<Item<T>>>
        get() = data.map { it.selection }

    /**
     * Flow with the first selected [Item] (if any)
     */
    public val singleSelection: Flow<Item<T>>
        get() = data.map { it.singleSelection }.filterNotNull()
}
