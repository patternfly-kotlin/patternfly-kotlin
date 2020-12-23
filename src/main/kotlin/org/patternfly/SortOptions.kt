package org.patternfly

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

public fun <T> ToolbarItem.sortOptions(
    itemStore: ItemStore<T>,
    options: List<SortInfo<T>>,
    id: String? = null,
    baseClass: String? = null,
    content: OptionsMenu<SortOption>.() -> Unit = {}
): OptionsMenu<SortOption> = optionsMenu(
    itemSelection = ItemSelection.SINGLE_PER_GROUP,
    grouped = true,
    id = id,
    baseClass = baseClass
) {
    iconToggle {
        icon("sort-amount-down".fas())
    }
    display { +it.text }
    groups {
        group {
            options.forEach {
                item(SortProperty(it.id, it.text, it.comparator))
            }
        }
        separator()
        group {
            item(SortOrder(true)) {
                selected = true
            }
            item(SortOrder(false))
        }
    }

    // Two-way data binding (1): update selection according to Items.sortInfo
    select(
        itemStore.data.map { it.sortInfo }.filterNotNull().map {
            listOf(
                SortProperty(it.id, it.text, it.comparator),
                SortOrder(it.ascending)
            )
        }
    )

    // Two-way data binding (2): sort items according to selection
    store.selection.unwrap()
        .map { items ->
            val property = items.filterIsInstance<SortProperty<T>>().firstOrNull()
            val order = items.filterIsInstance<SortOrder>().firstOrNull()
            if (property != null && order != null) {
                SortInfo(property.id, property.text, property.comparator, order.ascending)
            } else {
                null
            }
        }.filterNotNull() handledBy itemStore.sortWith

    content(this)
}

public sealed class SortOption(public var text: String)

public class SortProperty<T>(public val id: String, text: String, public val comparator: Comparator<T>) :
    SortOption(text) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false
        other as SortProperty<*>
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "SortProperty(id=$id)"
    }
}

public class SortOrder(public val ascending: Boolean) : SortOption(if (ascending) "Ascending" else "Descending") {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false
        other as SortOrder
        if (ascending != other.ascending) return false
        return true
    }

    override fun hashCode(): Int {
        return ascending.hashCode()
    }

    override fun toString(): String {
        return "SortOrder(ascending=$ascending)"
    }
}
