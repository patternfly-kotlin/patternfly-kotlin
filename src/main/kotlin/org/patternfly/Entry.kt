package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Span
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.patternfly.ItemSelection.SINGLE
import org.patternfly.ItemSelection.SINGLE_PER_GROUP
import org.patternfly.dom.Id

// ------------------------------------------------------ dsl

/**
 * Extension function to add all data in an [Iterable] as [Item]s to the specified store.
 */
public fun <T> Iterable<T>.addTo(store: EntriesStore<T>, block: ItemBuilder<T>.(T) -> Unit = {}) {
    store.items {
        forEach {
            item(it) {
                block(this, it)
            }
        }
    }
}

/**
 * Adds the [Item]s built by the specified [ItemsBuilder] to this store.
 *
 * @receiver the store the built items are added to
 */
public fun <T> EntriesStore<T>.items(block: ItemsBuilder<T>.() -> Unit = {}) {
    val entries = ItemsBuilder(this).apply(block).build()
    update(entries)
}

/**
 * Adds the [Group]s built by the specified [GroupsBuilder] to this store.
 *
 * @receiver the store the built groups are added to
 */
public fun <T> EntriesStore<T>.groups(block: GroupsBuilder<T>.() -> Unit = {}) {
    val entries = GroupsBuilder(this).apply(block).build()
    update(entries)
}

/**
 * Adds an item to the enclosing [ItemsBuilder].
 *
 * @receiver an items builder this item is added to
 */
public fun <T> ItemsBuilder<T>.item(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

/**
 * Adds a separator to the enclosing [ItemsBuilder].
 *
 * @receiver an items builder this separator is added to
 */
public fun <T> ItemsBuilder<T>.separator() {
    entries.add(Separator())
}

/**
 * Add a group to the enclosing [GroupsBuilder].
 *
 * @receiver a groups builder this group is added to
 */
public fun <T> GroupsBuilder<T>.group(title: String? = null, block: GroupBuilder<T>.() -> Unit) {
    entries.add(GroupBuilder<T>(title).apply(block).build())
}

/**
 * Add a separator to the enclosing [GroupsBuilder].
 *
 * @receiver a groups builder this separator is added to
 */
public fun <T> GroupsBuilder<T>.separator() {
    entries.add(Separator())
}

/**
 * Adds an item to the enclosing [GroupBuilder].
 *
 * @receiver a group builder this item is added to
 */
public fun <T> GroupBuilder<T>.item(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

/**
 * Adds a separator to the enclosing [GroupBuilder].
 *
 * @receiver a group builder this separator is added to
 */
public fun <T> GroupBuilder<T>.separator() {
    entries.add(Separator())
}

// ------------------------------------------------------ types & data class

/**
 * Enum which controls how to select an [Item] in [Entries].
 */
@Suppress("unused")
public enum class ItemSelection {

    /**
     * Only a single item (across all groups) can be selected at a time.
     */
    SINGLE,

    /**
     * Only one item per group can be selected at a time.
     */
    SINGLE_PER_GROUP,

    /**
     * Multiple items can be selected.
     */
    MULTIPLE
}

/**
 * Abstract store for [Entries].
 *
 * Most flows in this store use [Item] instead of the wrapped data. Use one of the [unwrap] functions to get the actual payload.
 */
public abstract class EntriesStore<T> internal constructor(
    override val idProvider: IdProvider<T, String>,
    internal val selectionMode: ItemSelection
) : RootStore<Entries<T>>(Entries(idProvider, emptyList(), selectionMode)),
    WithIdProvider<T> {

    public val entries: Flow<List<Entry<T>>>
        get() = data.map { it.entries }

    public val selection: Flow<List<Item<T>>>
        get() = data.map { it.selection }

    public val singleSelection: Flow<Item<T>?>
        get() = data.map { it.singleSelection }
}

/**
 * Immutable collection of [entries][Entry] used in different stores such as [DropdownStore], [MenuStore], [OptionsMenuStore] or [SelectStore]. Every modification leads to a new instance with changed properties. Each item has to be uniquely identifiable using the specified [idProvider].
 *
 * This class holds instances of class [Entry]. Each Entry is either an [Item], a [Group] or a [Separator]. A group can contain nested [Item]s or [Separator]s, but must **not** contain nested groups. An [Item] can have additional properties such as an [icon][Item.icon], a [description][Item.description] or a [disabled][Item.disabled] state. A [Separator] has no additional properties and is only used to visually separate groups and items.
 *
 * Entries can be obtained using different properties:
 *
 * - [all]: All entries given when this instance was created. This collection never changes.
 * - [entries]: List of entries after an optional [filter] has been applied to [all].
 * - [items]: A flat list of all [Item]s based on [entries].
 * - [selection]: A flat list of selected [Item]s based on [all].
 *
 * @param idProvider used to uniquely identify each item
 * @param all all entries managed by this instance
 * @param filter a predicate applied to [all]
 * @param selectionMode defines how to select items
 */
public data class Entries<T>(
    val idProvider: IdProvider<T, String>,
    val all: List<Entry<T>> = emptyList(),
    val selectionMode: ItemSelection,
    val filter: ItemFilter<T>? = null
) {

    /**
     * List after an optional [filter] has been applied to [all]. Empty [Group]s w/o items are omitted.
     */
    public val entries: List<Entry<T>>
        get() = if (filter != null) {
            all.filter { entry ->
                when (entry) {
                    is Group -> entry.entries.any { groupEntry ->
                        when (groupEntry) {
                            is Group -> {
                                warnAboutNestedGroups(entry, groupEntry)
                                false
                            }
                            is Item -> filter.invoke(groupEntry.item)
                            is Separator -> true
                        }
                    }
                    is Item -> filter.invoke(entry.item)
                    is Separator -> true
                }
            }
        } else {
            all
        }

    /**
     * A flat list of all [Item]s based on [entries].
     */
    public val items: List<Item<T>>
        get() = entries.flatItems()

    /**
     * The selected items based on [all].
     */
    public val selection: List<Item<T>>
        get() = all.flatItems().filter { it.selected }

    public val singleSelection: Item<T>?
        get() = selection.firstOrNull()

    /**
     * Selects the specified data and returns a new instance. Selecting an item might lead to unselecting other items depending on the value of [selectionMode].
     */
    public fun select(data: T): Entries<T> {
        var groupWithSelection = false
        val item = all.flatItems().find { idProvider(it.item) == idProvider(data) }
        return if (item != null) {
            copy(
                all = all.map { entry ->
                    when (entry) {
                        is Group<T> -> {
                            val groupCopy = if (selectionMode == SINGLE && groupWithSelection) {
                                // there's already a selection in another group
                                // --> unselect all items in this group
                                entry.copy(
                                    entries = entry.entries.map { groupEntry ->
                                        when (groupEntry) {
                                            is Group<T> -> {
                                                warnAboutNestedGroups(entry, groupEntry)
                                                groupEntry
                                            }
                                            is Item<T> -> groupEntry.copy(selected = false)
                                            is Separator -> groupEntry
                                        }
                                    }
                                )
                            } else {
                                // selectionMode is either SINGLE_PER_GROUP or MULTIPLE
                                // --> (de)select according to specified predicates
                                entry.copy(
                                    entries = entry.entries.map { groupEntry ->
                                        when (groupEntry) {
                                            is Group<T> -> {
                                                warnAboutNestedGroups(entry, groupEntry)
                                                groupEntry
                                            }
                                            is Item<T> -> select(
                                                item = groupEntry,
                                                selectPredicate = { groupItem ->
                                                    idProvider(groupItem.item) == idProvider(data)
                                                },
                                                deselectPredicate = { groupItem ->
                                                    if (selectionMode == SINGLE_PER_GROUP) {
                                                        groupItem.group?.id == item.group?.id
                                                    } else {
                                                        false
                                                    }
                                                }
                                            )
                                            is Separator -> groupEntry
                                        }
                                    }
                                )
                            }
                            groupWithSelection = groupCopy.hasSelection
                            groupCopy
                        }
                        is Item<T> -> select(
                            item = entry,
                            selectPredicate = { currentItem ->
                                idProvider(currentItem.item) == idProvider(data)
                            },
                            deselectPredicate = {
                                selectionMode == SINGLE || selectionMode == SINGLE_PER_GROUP
                            }
                        )
                        is Separator<T> -> entry
                    }
                }
            )
        } else {
            this
        }
    }

    private fun select(
        item: Item<T>,
        selectPredicate: (Item<T>) -> Boolean,
        deselectPredicate: (Item<T>) -> Boolean
    ): Item<T> = if (selectPredicate(item)) {
        item.copy(selected = true)
    } else {
        if (deselectPredicate(item)) {
            item.copy(selected = false)
        } else {
            item
        }
    }

    private fun warnAboutNestedGroups(parent: Group<T>, child: Group<T>) {
        console.warn("Nested group detected: Parent group with id ${parent.id} contains nested group with id ${child.id}. Nested groups are not supported!")
    }
}

/**
 * An [Entry] is either an [Item], a [Group] or a [Separator].
 */
public sealed class Entry<T>

/**
 * Group containing a list of nested [entries][Entry] an optional group heading. A group can contain nested [Item]s or [Separator]s, but must **not** contain nested groups.
 */
public data class Group<T> internal constructor(
    internal val id: String = Id.unique("grp"),
    val text: String?,
    val entries: List<Entry<T>>
) : Entry<T>() {

    internal val hasSelection: Boolean
        get() = entries.any { it is Item<T> && it.selected }
}

/**
 * Item containing the actual data and additional properties.
 *
 * @param item the actual data
 * @param disabled whether this item is disabled
 * @param selected whether this item is selected
 * @param favorite whether this item has been marked as a favorite
 * @param href an optional link
 * @param description an optional description
 * @param icon an optional icon content function
 */
public data class Item<T> internal constructor(
    override val item: T,
    val disabled: Boolean,
    val selected: Boolean,
    val favorite: Boolean,
    val href: String?,
    val description: String?,
    val icon: (Span.() -> Unit)?,
    internal var group: Group<T>?
) : Entry<T>(), HasItem<T>

/**
 * Separator used for visual purposes only.
 */
public class Separator<T> : Entry<T>()

internal fun <T> List<Entry<T>>.flatItems(): List<Item<T>> = flatMap { entry ->
    when (entry) {
        is Group<T> -> entry.entries.filterIsInstance<Item<T>>()
        is Item<T> -> listOf(entry)
        is Separator<T> -> emptyList()
    }
}

// ------------------------------------------------------ builder

/**
 * Builder for a list of [Item]s.
 */
public class ItemsBuilder<T> internal constructor(private val store: EntriesStore<T>) {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Entries<T> = Entries(
        idProvider = store.idProvider,
        all = entries,
        selectionMode = store.selectionMode
    )
}

/**
 * Builder for a list of [Group]s.
 */
public class GroupsBuilder<T> internal constructor(private val store: EntriesStore<T>) {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Entries<T> = Entries(
        idProvider = store.idProvider,
        all = entries,
        selectionMode = store.selectionMode
    )
}

/**
 * Builder for a [Group].
 */
public class GroupBuilder<T> internal constructor(private val text: String?) {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Group<T> = Group(text = text, entries = entries).apply {
        entries.filterIsInstance<Item<T>>().forEach { it.group = this }
    }
}

/**
 * Builder for an [Item].
 */
public class ItemBuilder<T> internal constructor(private val item: T) {
    public var disabled: Boolean = false
    public var selected: Boolean = false
    public var favorite: Boolean = false
    public var href: String? = null
    public var description: String? = null
    public var icon: (Span.() -> Unit)? = null

    internal fun build(): Item<T> = Item(
        item = item,
        disabled = disabled,
        selected = selected,
        favorite = favorite,
        href = href,
        description = description,
        icon = icon,
        group = null
    )
}
