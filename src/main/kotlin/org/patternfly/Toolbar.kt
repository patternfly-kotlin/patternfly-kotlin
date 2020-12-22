package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.states
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.patternfly.ItemSelection.SINGLE_PER_GROUP
import org.patternfly.dom.plusAssign
import org.w3c.dom.HTMLDivElement

// TODO Document me
// ------------------------------------------------------ dsl

public fun RenderContext.toolbar(
    id: String? = null,
    baseClass: String? = null,
    content: Toolbar.() -> Unit = {}
): Toolbar = register(Toolbar(id = id, baseClass = baseClass, job), content)

public fun Toolbar.toolbarContent(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarContent.() -> Unit = {}
): ToolbarContent = register(ToolbarContent(id = id, baseClass = baseClass, job), content)

public fun ToolbarContent.toolbarContentSection(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarContentSection.() -> Unit = {}
): ToolbarContentSection = register(ToolbarContentSection(id = id, baseClass = baseClass, job), content)

public fun ToolbarContentSection.toolbarGroup(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarGroup.() -> Unit = {}
): ToolbarGroup = register(ToolbarGroup(id = id, baseClass = baseClass, job), content)

public fun ToolbarContentSection.toolbarItem(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarItem.() -> Unit = {}
): ToolbarItem = register(ToolbarItem(id = id, baseClass = baseClass, job), content)

public fun ToolbarGroup.toolbarItem(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarItem.() -> Unit = {}
): ToolbarItem = register(ToolbarItem(id = id, baseClass = baseClass, job), content)

public fun ToolbarContent.toolbarExpandableContent(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarExpandableContent.() -> Unit = {}
): ToolbarExpandableContent = register(ToolbarExpandableContent(id = id, baseClass = baseClass, job), content)

public fun ToolbarExpandableContent.toolbarGroup(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarGroup.() -> Unit = {}
): ToolbarGroup = register(ToolbarGroup(id = id, baseClass = baseClass, job), content)

public fun <T> ToolbarItem.bulkSelect(
    itemStore: ItemStore<T>,
    id: String? = null,
    baseClass: String? = null,
    content: Dropdown<PreSelection>.() -> Unit = {}
): Dropdown<PreSelection> {
    domNode.classList += "bulk-select".modifier()
    return dropdown(id = id, baseClass = baseClass) {
        checkboxToggle {
            text {
                itemStore.selected.map {
                    if (it == 0) "" else "$it selected"
                }.asText()
            }
            checkbox {
                triState(
                    itemStore.data.map {
                        when {
                            it.selected.isEmpty() -> TriState.OFF
                            it.selected.size == it.items.size -> TriState.ON
                            else -> TriState.INDETERMINATE
                        }
                    }
                )
                changes.states().filter { !it }.map { } handledBy itemStore.selectNone
                changes.states().filter { it }.map { } handledBy itemStore.selectAll
            }
        }
        display { +it.text }
        items {
            PreSelection.values().map { item(it) }
        }
        store.selects.unwrap() handledBy itemStore.preSelect
        content(this)
    }
}

public fun <T> ToolbarItem.sortOptions(
    itemStore: ItemStore<T>,
    options: List<SortInfo<T>>,
    id: String? = null,
    baseClass: String? = null,
    content: OptionsMenu<SortOption>.() -> Unit = {}
): OptionsMenu<SortOption> = optionsMenu(
    itemSelection = SINGLE_PER_GROUP,
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

public fun <T> ToolbarItem.pagination(
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
            baseClass = baseClass,
            job
        ),
        content
    )
}

// ------------------------------------------------------ tag

public class Toolbar internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Toolbar, baseClass), job) {
    init {
        markAs(ComponentType.Toolbar)
    }
}

public class ToolbarContent internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("toolbar".component("content"), baseClass), job)

public class ToolbarContentSection internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("toolbar".component("content", "section"), baseClass), job)

public class ToolbarGroup internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("toolbar".component("group"), baseClass), job)

public class ToolbarItem internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("toolbar".component("item"), baseClass), job)

public class ToolbarExpandableContent internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("toolbar".component("expandable", "content"), baseClass), job)

// ------------------------------------------------------ types

public enum class PreSelection(public val text: String) {
    NONE("Select none"), PAGE("Select visible"), ALL("Select all")
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
