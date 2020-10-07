package org.patternfly

import dev.fritz2.dom.html.Span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ------------------------------------------------------ dsl

fun <T> pfItems(block: ItemsBuilder<T>.() -> Unit = {}): List<Entry<T>> = ItemsBuilder<T>().apply(block).build()

fun <T> pfGroups(block: GroupsBuilder<T>.() -> Unit = {}): List<Entry<T>> = GroupsBuilder<T>().apply(block).build()

fun <T> ItemsBuilder<T>.pfItem(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

fun <T> ItemsBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

fun <T> GroupsBuilder<T>.pfGroup(title: String? = null, block: GroupBuilder<T>.() -> Unit) {
    entries.add(GroupBuilder<T>(title).apply(block).build())
}

fun <T> GroupsBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

fun <T> EntriesBuilder<T>.pfItem(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

fun <T> EntriesBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

fun <T> EntriesBuilder<T>.pfGroup(title: String? = null, block: GroupBuilder<T>.() -> Unit) {
    entries.add(GroupBuilder<T>(title).apply(block).build())
}

fun <T> GroupBuilder<T>.pfItem(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

fun <T> GroupBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

// ------------------------------------------------------ flow extensions

fun <T> Flow<List<Entry<T>>>.groups() = this.map { it.filterIsInstance<Group<T>>() }

fun <T> Flow<List<Entry<T>>>.flatItems() = this.map {
    it.flatMap { entry ->
        when (entry) {
            is Item<T> -> listOf(entry)
            is Group<T> -> entry.items
            is Separator<T> -> emptyList()
        }
    }.filterIsInstance<Item<T>>()
}

fun <T> Flow<List<Item<T>>>.unwrap(): Flow<List<T>> = this.map { items -> items.map { it.item } }

fun <T> Flow<Item<T>>.unwrap(): Flow<T> = this.map { it.item }

fun <T> Flow<Item<T>?>.unwrapOrNull(): Flow<T?> = this.map { it?.item }

// ------------------------------------------------------ data classes

/** Entry used in simple components like [Dropdown], [OptionsMenu] or [Select]. */
sealed class Entry<T>

data class Group<T> internal constructor(
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

data class Item<T> internal constructor(
    val item: T,
    val disabled: Boolean,
    val selected: Boolean,
    val description: String,
    val icon: (Span.() -> Unit)?,
    internal var group: Group<T>?
) : Entry<T>() {
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

class Separator<T> : Entry<T>()

class ItemsBuilder<T> {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): List<Entry<T>> = entries
}

class EntriesBuilder<T> {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): List<Entry<T>> = entries
}

class GroupsBuilder<T> {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): List<Entry<T>> = entries
}

class GroupBuilder<T>(private val title: String?) {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Group<T> = Group(title = title, items = entries).apply {
        items.filterIsInstance<Item<T>>().forEach { it.group = this }
    }
}

class ItemBuilder<T>(private val item: T) {
    var disabled: Boolean = false
    var selected: Boolean = false
    var description: String = ""
    var icon: (Span.() -> Unit)? = null

    internal fun build(): Item<T> = Item(item, disabled, selected, description, icon, null)
}
