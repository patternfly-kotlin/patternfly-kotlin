package org.patternfly

import dev.fritz2.dom.html.Span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id

// ------------------------------------------------------ dsl

/**
 * Entrypoint for building items.
 */
public fun <T> items(block: ItemsBuilder<T>.() -> Unit = {}): List<Entry<T>> =
    ItemsBuilder<T>().apply(block).build()

/**
 * Entrypoint for building groups.
 */
public fun <T> groups(block: GroupsBuilder<T>.() -> Unit = {}): List<Entry<T>> =
    GroupsBuilder<T>().apply(block).build()

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

// ------------------------------------------------------ flow extensions

/**
 * Maps the specified flow into a flow with groups only.
 */
public fun <T> Flow<List<Entry<T>>>.groups(): Flow<List<Group<T>>> = this.map { it.filterIsInstance<Group<T>>() }

/**
 * Maps the specified flow into a flow with all items across all groups (if any).
 */
public fun <T> Flow<List<Entry<T>>>.flatItems(): Flow<List<Item<T>>> = this.map {
    it.flatMap { entry ->
        when (entry) {
            is Item<T> -> listOf(entry)
            is Group<T> -> entry.items
            is Separator<T> -> emptyList()
        }
    }.filterIsInstance<Item<T>>()
}

// ------------------------------------------------------ data classes

/**
 * Entry used in components like [Dropdown], [OptionsMenu] or [Select].
 *
 * Each [Entry] is either an [Item], a [Group] or a [Separator].
 */
public sealed class Entry<T>

/**
 * Group containing a list of nested [Entry] instances and an optional text.
 */
public data class Group<T> internal constructor(
    internal val id: String = Id.unique("grp"),
    val text: String?,
    val items: List<Entry<T>>
) : Entry<T>()

/**
 * Item containing the actual data and an optional description and icon.
 */
public data class Item<T> internal constructor(
    override val item: T,
    val disabled: Boolean,
    val selected: Boolean,
    val description: String,
    val icon: (Span.() -> Unit)?,
    internal var group: Group<T>?
) : Entry<T>(), HasItem<T>

/**
 * Separator used for visual purposes only.
 */
public class Separator<T> : Entry<T>()

/**
 * Builder for a list of [Item]s.
 */
public class ItemsBuilder<T> internal constructor() {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): List<Entry<T>> = entries
}

/**
 * Builder for a list of [Group]s.
 */
public class GroupsBuilder<T> internal constructor() {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): List<Entry<T>> = entries
}

/**
 * Builder for a [Group].
 */
public class GroupBuilder<T> internal constructor(private val text: String?) {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Group<T> = Group(text = text, items = entries).apply {
        items.filterIsInstance<Item<T>>().forEach { it.group = this }
    }
}

/**
 * Builder for an [Item].
 */
public class ItemBuilder<T> internal constructor(private val item: T) {
    public var disabled: Boolean = false
    public var selected: Boolean = false
    public var description: String = ""
    public var icon: (Span.() -> Unit)? = null

    internal fun build(): Item<T> = Item(item, disabled, selected, description, icon, null)
}
