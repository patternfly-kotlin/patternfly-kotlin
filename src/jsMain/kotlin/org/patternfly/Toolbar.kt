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

fun HtmlElements.pfToolbar(classes: String? = null, content: Toolbar.() -> Unit = {}): Toolbar =
    register(Toolbar(classes), content)

fun Toolbar.pfToolbarContent(classes: String? = null, content: ToolbarContent.() -> Unit = {}): ToolbarContent =
    register(ToolbarContent(classes), content)

fun ToolbarContent.pfToolbarContentSection(
    classes: String? = null,
    content: ToolbarContentSection.() -> Unit = {}
): ToolbarContentSection = register(ToolbarContentSection(classes), content)

fun ToolbarContentSection.pfToolbarGroup(classes: String? = null, content: ToolbarGroup.() -> Unit = {}): ToolbarGroup =
    register(ToolbarGroup(classes), content)

fun ToolbarContentSection.pfToolbarItem(classes: String? = null, content: ToolbarItem.() -> Unit = {}): ToolbarItem =
    register(ToolbarItem(classes), content)

fun ToolbarGroup.pfToolbarItem(classes: String? = null, content: ToolbarItem.() -> Unit = {}): ToolbarItem =
    register(ToolbarItem(classes), content)

fun ToolbarContent.pfToolbarExpandableContent(
    classes: String? = null,
    content: ToolbarExpandableContent.() -> Unit = {}
): ToolbarExpandableContent = register(ToolbarExpandableContent(classes), content)

fun ToolbarExpandableContent.pfToolbarGroup(
    classes: String? = null,
    content: ToolbarGroup.() -> Unit = {}
): ToolbarGroup =
    register(ToolbarGroup(classes), content)

fun <T> ToolbarItem.pfBulkSelect(
    itemStore: ItemStore<T>,
    classes: String? = null,
    content: BulkSelect<T>.() -> Unit = {}
): BulkSelect<T> {
    this.domNode.classList += "bulk-select".modifier()
    return register(BulkSelect(itemStore, classes), content)
}

fun <T> ToolbarItem.pfSortOptions(
    itemStore: ItemStore<T>,
    options: Map<String, Comparator<T>>,
    classes: String? = null,
    content: SortOptions<T>.() -> Unit = {}
): SortOptions<T> {
    return register(SortOptions(itemStore, options, classes), content)
}

fun <T> ToolbarItem.pfPagination(
    itemStore: ItemStore<T>,
    pageSizes: Array<Int> = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    classes: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination {
    this.domNode.classList += "pagination".modifier()
    return register(Pagination(itemStore, itemStore.data.map { it.pageInfo }, pageSizes, compact, classes), content)
}

// ------------------------------------------------------ tag

class Toolbar internal constructor(classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes(ComponentType.Toolbar, classes)) {
    init {
        markAs(ComponentType.Toolbar)
    }
}

class ToolbarContent internal constructor(classes: String?) :
    Div(baseClass = classes("toolbar".component("content"), classes))

class ToolbarContentSection internal constructor(classes: String?) :
    Div(baseClass = classes("toolbar".component("content", "section"), classes))

class ToolbarGroup internal constructor(classes: String?) :
    Div(baseClass = classes("toolbar".component("group"), classes))

class ToolbarItem internal constructor(classes: String?) :
    Div(baseClass = classes("toolbar".component("item"), classes))

class ToolbarExpandableContent internal constructor(classes: String?) :
    Div(baseClass = classes("toolbar".component("expandable", "content"), classes))

enum class PreSelection(val text: String) {
    NONE("Select none"), VISIBLE("Select visible"), ALL("Select all")
}

class BulkSelect<T>(itemStore: ItemStore<T>, classes: String?) :
    Dropdown<PreSelection>(DropdownStore(), dropdownAlign = null, up = false, classes = classes) {

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

sealed class SortOption(val text: String)
class SortProperty<T>(text: String, val comparator: Comparator<T>) : SortOption(text)
class SortOrder(val ascending: Boolean) : SortOption(if (ascending) "Ascending" else "Descending")

class SortOptions<T>(itemStore: ItemStore<T>, options: Map<String, Comparator<*>>, classes: String?) :
    OptionsMenu<SortOption>(OptionStore(), optionsMenuAlign = null, up = false, classes = classes) {

    init {
        display = {
            { +it.item.text }
        }
        pfOptionsMenuToggle { icon = { pfIcon("sort-amount-down".fas()) } }
        pfOptionsMenuGroups {
            pfGroup {
                options.map { (name, comparator) ->
                    pfItem(SortProperty(name, comparator))
                }
            }
            pfSeparator()
            pfGroup {
                pfItem(SortOrder(true)) { selected = true }
                pfItem(SortOrder(false))
            }
        }
        store.selection.unwrap()
            .map { items ->
                val property = items.filterIsInstance<SortProperty<T>>().firstOrNull()
                val order = items.filterIsInstance<SortOrder>().first()
                if (order.ascending) property?.comparator else property?.comparator?.reversed()
            }.filterNotNull() handledBy itemStore.sortWith
    }
}