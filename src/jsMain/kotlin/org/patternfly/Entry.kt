package org.patternfly

fun <T> pfEntries(block: EntryBuilder<T>.() -> Unit): List<Entry<T>> =
    EntryBuilder<T>().apply(block).build()

fun <T> EntryBuilder<T>.pfItem(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

fun <T> EntryBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

fun <T> EntryBuilder<T>.pfGroup(title: String, block: GroupBuilder<T>.() -> Unit) {
    entries.add(GroupBuilder<T>(title).apply(block).build())
}

fun <T> GroupBuilder<T>.pfItem(item: T, block: ItemBuilder<T>.() -> Unit = {}) {
    entries.add(ItemBuilder(item).apply(block).build())
}

fun <T> GroupBuilder<T>.pfSeparator() {
    entries.add(Separator())
}

/** Entry used in simple components like [Dropdown], [OptionsMenu] or [Select]. */
sealed class Entry<T>

data class Group<T>(val title: String, val items: List<Entry<T>>) : Entry<T>()

data class Item<T>(val item: T, val disabled: Boolean = false, val selected: Boolean = false) :
    Entry<T>()

class Separator<T> : Entry<T>()

class EntryBuilder<T> {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): List<Entry<T>> = entries
}

class GroupBuilder<T>(private val title: String) {
    internal val entries: MutableList<Entry<T>> = mutableListOf()
    internal fun build(): Group<T> = Group(title, entries)
}

class ItemBuilder<T>(private val item: T) {
    var disabled: Boolean = false
    var selected: Boolean = false
    internal fun build(): Item<T> = Item(item, disabled, selected)
}
