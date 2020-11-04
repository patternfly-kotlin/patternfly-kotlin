package org.patternfly

import dev.fritz2.dom.html.Span
import dev.fritz2.elemento.Id
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ------------------------------------------------------ dsl

public fun <T> pfItems(block: ItemsBuilder<T>.() -> Unit = {}): List<Entry<T>> = ItemsBuilder<T>().apply(block).build()

public fun <T> pfGroups(block: GroupsBuilder<T>.() -> Unit = {}): List<Entry<T>> = GroupsBuilder<T>().apply(block).build()

public fun <T> ItemsBuilder<T>.pfItem(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

public fun <T> ItemsBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

public fun <T> GroupsBuilder<T>.pfGroup(title: String? = null, block: GroupBuilder<T>.() -> Unit) {
    entries.add(GroupBuilder<T>(title).apply(block).build())
}

public fun <T> GroupsBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

public fun <T> EntriesBuilder<T>.pfItem(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

public fun <T> EntriesBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

public fun <T> EntriesBuilder<T>.pfGroup(title: String? = null, block: GroupBuilder<T>.() -> Unit) {
    entries.add(GroupBuilder<T>(title).apply(block).build())
}

public fun <T> GroupBuilder<T>.pfItem(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

public fun <T> GroupBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

// ------------------------------------------------------ flow extensions

public fun <T> Flow<List<Entry<T>>>.groups(): Flow<List<Group<T>>> = this.map { it.filterIsInstance<Group<T>>() }

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

/** Entry used in components like [Dropdown], [OptionsMenu] or [Select]. */
public sealed class Entry<T>

public data class Group<T> internal constructor(
    internal val id: String = Id.unique("grp"),
    val title: String?,
    val items: List<Entry<T>>
) : Entry<T>() {
    override fun toString(): String = buildString {
        append("Group(id=").append(id)
        append(", items=")
        items.joinTo(this, prefix = "[", postfix = "]")
        append(")")
    }
}

public data class Item<T> internal constructor(
    override val item: T,
    val disabled: Boolean,
    val selected: Boolean,
    val description: String,
    val icon: (Span.() -> Unit)?,
    internal var group: Group<T>?
) : Entry<T>(), HasItem<T> {
    override fun toString(): String = buildString {
        append("Item(item=").append(item)
        append(", disabled=").append(disabled)
        append(", selected=").append(selected)
        if (description.isNotEmpty()) {
            append(", description=").append(description)
        }
        if (icon != null) {
            append(", with icon")
        }
        group?.let {
            append(", group=").append(it.id)
        }
        append(")")
    }
}

public class Separator<T> : Entry<T>()

public class ItemsBuilder<T> {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): List<Entry<T>> = entries
}

public class EntriesBuilder<T> {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): List<Entry<T>> = entries
}

public class GroupsBuilder<T> {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): List<Entry<T>> = entries
}

public class GroupBuilder<T>(private val title: String?) {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Group<T> = Group(title = title, items = entries).apply {
        items.filterIsInstance<Item<T>>().forEach { it.group = this }
    }
}

public class ItemBuilder<T>(private val item: T) {
    public var disabled: Boolean = false
    public var selected: Boolean = false
    public var description: String = ""
    public var icon: (Span.() -> Unit)? = null

    internal fun build(): Item<T> = Item(item, disabled, selected, description, icon, null)
}
