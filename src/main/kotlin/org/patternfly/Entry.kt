package org.patternfly

import dev.fritz2.dom.html.Span
import dev.fritz2.lenses.IdProvider
import org.patternfly.dom.Id

// ------------------------------------------------------ dsl

/**
 * Creates and returns an instance of [Entries] containing [Item]s from the specified code block.
 *
 * @param T the payload of the [Item]s
 *
 * @param block function executed in the context of an [ItemsBuilder]
 */
public fun <T> items(
    idProvider: IdProvider<T, String>,
    itemSelection: ItemSelection,
    block: ItemsBuilder<T>.() -> Unit = {}
): Entries<T> = ItemsBuilder(idProvider, itemSelection).apply(block).build()

/**
 * Creates an instance of [Entries] containing [Item]s from the specified code block and updates the specified store.
 *
 * @receiver the store to update
 *
 * @param T the payload of the [Item]s
 *
 * @param block function executed in the context of an [ItemsBuilder]
 *
 * @sample org.patternfly.sample.EntrySample.storeItems
 */
public fun <T> EntriesStore<T>.items(block: ItemsBuilder<T>.() -> Unit = {}) {
    val entries = ItemsBuilder(this.idProvider, this.itemSelection).apply(block).build()
    update(entries)
}

/**
 * Creates and returns an instance of [Entries] containing [Group]s from the specified code block.
 *
 * @param T the payload of the [Item]s
 *
 * @param block function executed in the context of an [GroupsBuilder]
 */
public fun <T> groups(
    idProvider: IdProvider<T, String>,
    itemSelection: ItemSelection,
    block: GroupsBuilder<T>.() -> Unit = {}
): Entries<T> = GroupsBuilder(idProvider, itemSelection).apply(block).build()

/**
 * Creates an instance of [Entries] containing [Group]s from the specified code block and updates the specified store.
 *
 * @receiver the store to update
 *
 * @param T the payload of the [Item]s
 *
 * @param block function executed in the context of an [GroupsBuilder]
 */
public fun <T> EntriesStore<T>.groups(block: GroupsBuilder<T>.() -> Unit = {}) {
    val entries = GroupsBuilder(this.idProvider, this.itemSelection).apply(block).build()
    update(entries)
}

/**
 * Adds an item to the enclosing [ItemsBuilder].
 *
 * @receiver an items builder this item is added to
 *
 * @param T the payload of the [Item]s
 */
public fun <T> ItemsBuilder<T>.item(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

/**
 * Adds a separator to the enclosing [ItemsBuilder].
 *
 * @receiver an items builder this separator is added to
 *
 * @param T the payload of the [Item]s
 */
public fun <T> ItemsBuilder<T>.separator() {
    entries.add(Separator())
}

/**
 * Add a group to the enclosing [GroupsBuilder].
 *
 * @receiver a groups builder this group is added to
 *
 * @param T the payload of the [Item]s
 */
public fun <T> GroupsBuilder<T>.group(title: String? = null, block: GroupBuilder<T>.() -> Unit) {
    entries.add(GroupBuilder<T>(title).apply(block).build())
}

/**
 * Add a separator to the enclosing [GroupsBuilder].
 *
 * @receiver a groups builder this separator is added to
 *
 * @param T the payload of the [Item]s
 */
public fun <T> GroupsBuilder<T>.separator() {
    entries.add(Separator())
}

/**
 * Adds an item to the enclosing [GroupBuilder].
 *
 * @receiver a group builder this item is added to
 *
 * @param T the payload of the [Item]s
 */
public fun <T> GroupBuilder<T>.item(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

/**
 * Adds a separator to the enclosing [GroupBuilder].
 *
 * @receiver a group builder this separator is added to
 *
 * @param T the payload of the [Item]s
 */
public fun <T> GroupBuilder<T>.separator() {
    entries.add(Separator())
}

// ------------------------------------------------------ types

/**
 * An [Entry] is either an [Item], a [Group] or a [Separator].
 *
 * @param T the payload of the [Item]s
 */
public sealed class Entry<T>

/**
 * Group containing a list of nested [entries][Entry] and an optional group heading. A group can contain nested [Item]s or [Separator]s, but must **not** contain nested groups.
 *
 * @param T the payload of the [Item]s
 */
public data class Group<T> internal constructor(
    internal val id: String = Id.unique("grp"),
    val text: String?,
    val entries: List<Entry<T>>
) : Entry<T>() {

    /**
     * A flat list of all [Item]s based on [entries].
     */
    public val items: List<Item<T>>
        get() = entries.filterIsInstance<Item<T>>()

    internal val hasSelection: Boolean
        get() = entries.any { it is Item<T> && it.selected }
}

/**
 * Item containing the actual data and additional properties.
 *
 * @param T the payload of the [Item]s
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
    val disabled: Boolean = false,
    val selected: Boolean = false,
    val favorite: Boolean = false,
    val href: String? = null,
    val description: String? = null,
    val icon: (Span.() -> Unit)? = null,
    internal var group: Group<T>? = null
) : Entry<T>(), HasItem<T>

/**
 * Separator used for visual purposes only.
 */
public class Separator<T> : Entry<T>()

// ------------------------------------------------------ builder

/**
 * Builder for a list of [Item]s.
 *
 * @param T the payload of the [Item]s
 */
public class ItemsBuilder<T> internal constructor(
    private val idProvider: IdProvider<T, String>,
    private val itemSelection: ItemSelection
) {

    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Entries<T> = Entries(
        idProvider = idProvider,
        all = entries,
        itemSelection = itemSelection
    )
}

/**
 * Builder for a list of [Group]s.
 *
 * @param T the payload of the [Item]s
 */
public class GroupsBuilder<T> internal constructor(
    private val idProvider: IdProvider<T, String>,
    private val itemSelection: ItemSelection
) {

    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Entries<T> = Entries(
        idProvider = idProvider,
        all = entries,
        itemSelection = itemSelection
    )
}

/**
 * Builder for a [Group].
 *
 * @param T the payload of the [Item]s
 */
public class GroupBuilder<T> internal constructor(private val text: String?) {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Group<T> = Group(text = text, entries = entries).apply {
        entries.filterIsInstance<Item<T>>().forEach { it.group = this }
    }
}

/**
 * Builder for an [Item].
 *
 * @param T the payload of the [Item]s
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
