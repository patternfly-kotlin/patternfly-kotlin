package org.patternfly

import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.states
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfToolbar(
    id: String? = null,
    baseClass: String? = null,
    content: Toolbar.() -> Unit = {}
): Toolbar = register(Toolbar(id = id, baseClass = baseClass), content)

fun Toolbar.pfToolbarContent(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarContent.() -> Unit = {}
): ToolbarContent = register(ToolbarContent(id = id, baseClass = baseClass), content)

fun ToolbarContent.pfToolbarContentSection(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarContentSection.() -> Unit = {}
): ToolbarContentSection = register(ToolbarContentSection(id = id, baseClass = baseClass), content)

fun ToolbarContentSection.pfToolbarGroup(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarGroup.() -> Unit = {}
): ToolbarGroup = register(ToolbarGroup(id = id, baseClass = baseClass), content)

fun ToolbarContentSection.pfToolbarItem(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarItem.() -> Unit = {}
): ToolbarItem = register(ToolbarItem(id = id, baseClass = baseClass), content)

fun ToolbarGroup.pfToolbarItem(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarItem.() -> Unit = {}
): ToolbarItem = register(ToolbarItem(id = id, baseClass = baseClass), content)

fun ToolbarContent.pfToolbarExpandableContent(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarExpandableContent.() -> Unit = {}
): ToolbarExpandableContent = register(ToolbarExpandableContent(id = id, baseClass = baseClass), content)

fun ToolbarExpandableContent.pfToolbarGroup(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarGroup.() -> Unit = {}
): ToolbarGroup = register(ToolbarGroup(id = id, baseClass = baseClass), content)

fun <T> ToolbarItem.pfBulkSelect(
    itemStore: ItemStore<T>,
    id: String? = null,
    baseClass: String? = null,
    content: BulkSelect<T>.() -> Unit = {}
): BulkSelect<T> {
    this.domNode.classList += "bulk-select".modifier()
    return register(BulkSelect(itemStore, id = id, baseClass = baseClass), content)
}

fun <T> ToolbarItem.pfSortOptions(
    itemStore: ItemStore<T>,
    options: List<SortInfo<T>>,
    id: String? = null,
    baseClass: String? = null,
    content: SortOptions<T>.() -> Unit = {}
): SortOptions<T> = register(SortOptions(itemStore, options, id = id, baseClass = baseClass), content)

fun <T> ToolbarItem.pfPagination(
    itemStore: ItemStore<T>,
    pageSizes: Array<Int> = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination {
    this.domNode.classList += "pagination".modifier()
    return register(
        Pagination(
            itemStore,
            itemStore.data.map { it.pageInfo },
            pageSizes,
            compact,
            id = id,
            baseClass = baseClass
        ), content
    )
}

// ------------------------------------------------------ tag

class Toolbar internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Toolbar, baseClass)) {
    init {
        markAs(ComponentType.Toolbar)
    }
}

class ToolbarContent internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("content"), baseClass))

class ToolbarContentSection internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("content", "section"), baseClass))

class ToolbarGroup internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("group"), baseClass))

class ToolbarItem internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("item"), baseClass))

class ToolbarExpandableContent internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("expandable", "content"), baseClass))

class BulkSelect<T> internal constructor(itemStore: ItemStore<T>, id: String?, baseClass: String?) :
    Dropdown<PreSelection>(DropdownStore(), dropdownAlign = null, up = false, id = id, baseClass = baseClass) {

    init {
        pfDropdownToggleCheckbox {
            content = {
                itemStore.selected.map {
                    if (it == 0) "" else "$it selected"
                }.bind()
            }
            triState = itemStore.data.map {
                when {
                    it.selected.isEmpty() -> TriState.OFF
                    it.selected.size == it.items.size -> TriState.ON
                    else -> TriState.INDETERMINATE
                }
            }
            input.changes.states().filter { !it }.map { Unit } handledBy itemStore.selectNone
            input.changes.states().filter { it }.map { Unit } handledBy itemStore.selectAll
        }
        display = {
            { +it.item.text }
        }
        store.clicked.unwrap().filter { it == PreSelection.NONE }.map { Unit } handledBy itemStore.selectNone
        store.clicked.unwrap().filter { it == PreSelection.VISIBLE }.map { Unit } handledBy itemStore.selectVisible
        store.clicked.unwrap().filter { it == PreSelection.ALL }.map { Unit } handledBy itemStore.selectAll

        pfDropdownItems {
            PreSelection.values().map { pfItem(it) }
        }
    }
}

class SortOptions<T> internal constructor(
    itemStore: ItemStore<T>,
    options: List<SortInfo<T>>,
    id: String?,
    baseClass: String?
) : OptionsMenu<SortOption>(OptionStore(), optionsMenuAlign = null, up = false, id = id, baseClass = baseClass) {

    init {
        display = {
            { +it.item.text }
        }
        pfOptionsMenuToggle { icon = { pfIcon("sort-amount-down".fas()) } }
        pfOptionsMenuGroups {
            pfGroup {
                options.forEach {
                    pfItem(SortProperty(it.id, it.text, it.comparator))
                }
            }
            pfSeparator()
            pfGroup {
                pfItem(SortOrder(true)) {
                    selected = true
                }
                pfItem(SortOrder(false))
            }
        }

        // TODO Update selection when ItemStore.sortWith has changed

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
    }
}

// ------------------------------------------------------ types

enum class PreSelection(val text: String) {
    NONE("Select none"), VISIBLE("Select visible"), ALL("Select all")
}

sealed class SortOption(var text: String)

class SortProperty<T>(val id: String, text: String, val comparator: Comparator<T>) : SortOption(text) {
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

class SortOrder(val ascending: Boolean) : SortOption(if (ascending) "Ascending" else "Descending") {
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
