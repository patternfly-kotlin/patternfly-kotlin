package org.patternfly

import dev.fritz2.binding.OfferingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.action
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDropdown(
    store: DropdownStore<T> = DropdownStore(),
    align: Align? = null,
    up: Boolean = false,
    classes: String? = null,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, align, up, classes), content)

fun <T> HtmlElements.pfDropdown(
    store: DropdownStore<T> = DropdownStore(),
    align: Align? = null,
    up: Boolean = false,
    modifier: Modifier,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, align, up, modifier.value), content)

fun <T> Dropdown<T>.pfDropdownToggle(
    classes: String? = null,
    content: Span.() -> Unit = {}
): DropdownToggle<T> = register(DropdownToggle(this, classes, textContent = content), {})

fun <T> Dropdown<T>.pfDropdownToggle(
    modifier: Modifier,
    content: Span.() -> Unit = {}
): DropdownToggle<T> = register(DropdownToggle(this, modifier.value, textContent = content), {})

fun <T> Dropdown<T>.pfDropdownIcon(
    iconClass: String,
    classes: String? = null,
    content: Icon.() -> Unit = {}
): DropdownToggle<T> =
    register(DropdownToggle(this, classes, iconClass = iconClass, iconContent = content), {})

fun <T> Dropdown<T>.pfDropdownIcon(
    iconClass: String,
    modifier: Modifier,
    content: Icon.() -> Unit = {}
): DropdownToggle<T> =
    register(DropdownToggle(this, modifier.value, iconClass = iconClass, iconContent = content), {})

fun <T> Dropdown<T>.pfDropdownKebab(
    classes: String? = null,
    content: Icon.() -> Unit = {}
): DropdownToggle<T> =
    register(DropdownToggle(this, classes, iconClass = "ellipsis-v".fas(), iconContent = content), {})

fun <T> Dropdown<T>.pfDropdownKebab(
    modifier: Modifier,
    content: Icon.() -> Unit = {}
): DropdownToggle<T> =
    register(DropdownToggle(this, modifier.value, iconClass = "ellipsis-v".fas(), iconContent = content), {})

fun <T> Dropdown<T>.pfDropdownItems(
    classes: String? = null,
    block: ItemsBuilder<T>.() -> Unit
): DropdownEntries<HTMLUListElement, T> {
    val element = register(
        DropdownEntries<HTMLUListElement, T>(this, "ul", classes), {})
    val items = ItemsBuilder<T>().apply(block).build()
    action(items) handledBy this.store.update
    return element
}

fun <T> Dropdown<T>.pfDropdownItems(
    modifier: Modifier,
    block: ItemsBuilder<T>.() -> Unit
): DropdownEntries<HTMLUListElement, T> {
    val element = register(
        DropdownEntries<HTMLUListElement, T>(this, "ul", modifier.value), {})
    val items = ItemsBuilder<T>().apply(block).build()
    action(items) handledBy this.store.update
    return element
}

fun <T> Dropdown<T>.pfDropdownGroups(
    classes: String? = null,
    block: GroupsBuilder<T>.() -> Unit
): DropdownEntries<HTMLDivElement, T> {
    val element = register(
        DropdownEntries<HTMLDivElement, T>(this, "div", classes), {})
    val groups = GroupsBuilder<T>().apply(block).build()
    action(groups) handledBy this.store.update
    return element
}

fun <T> Dropdown<T>.pfDropdownGroups(
    modifier: Modifier,
    block: GroupsBuilder<T>.() -> Unit
): DropdownEntries<HTMLDivElement, T> {
    val element = register(
        DropdownEntries<HTMLDivElement, T>(this, "div", modifier.value), {})
    val groups = GroupsBuilder<T>().apply(block).build()
    action(groups) handledBy this.store.update
    return element
}

// ------------------------------------------------------ tag

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class Dropdown<T> internal constructor(
    val store: DropdownStore<T>,
    internal val dropdownAlign: Align?,
    up: Boolean,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +ComponentType.Dropdown
    +dropdownAlign?.modifier
    +("top".modifier() `when` up)
    +classes
}) {

    internal var toggleId: String? = null
    val ces = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("dropdown".component("menu-item")))
    }
    var asText: AsText<T> = { it.toString() }
    var display: ComponentDisplay<Button, T> = {
        {
            +this@Dropdown.asText(it)
        }
    }

    init {
        markAs(ComponentType.Dropdown)
        classMap = ces.data.map { expanded -> mapOf(Modifier.expanded.value to expanded) }
    }
}

class DropdownToggle<T> internal constructor(
    dropdown: Dropdown<T>,
    classes: String?,
    textContent: (Span.() -> Unit)? = null,
    iconClass: String = "",
    iconContent: (Icon.() -> Unit)? = null
) : Button(id = Id.unique(ComponentType.Dropdown.id, "btn"), baseClass = classes {
    +"dropdown".component("toggle")
    +(Modifier.plain `when` (iconContent != null))
    +classes
}) {
    init {
        dropdown.toggleId = id
        aria["haspopup"] = true
        clicks handledBy dropdown.ces.toggle
        dropdown.ces.data.map { it.toString() }.bindAttr("aria-expanded")
        textContent?.let {
            span(baseClass = "dropdown".component("toggle", "text"), content = it)
            span(baseClass = "dropdown".component("toggle", "icon")) {
                pfIcon("caret-down".fas())
            }
        }
        iconContent?.let {
            pfIcon(iconClass) {
                it(this)
            }
        }
    }
}

class DropdownEntries<E : HTMLElement, T> internal constructor(
    private val dropdown: Dropdown<T>,
    tagName: String,
    classes: String?
) : Tag<E>(tagName = tagName, baseClass = classes {
    +"dropdown".component("menu")
    +dropdown.dropdownAlign?.modifier
    +classes
}) {
    init {
        dropdown.toggleId?.let {
            aria["labelledby"] = it
        }
        attr("role", "menu")
        dropdown.ces.data.map { !it }.bindAttr("hidden")

        dropdown.store.data.each().render { entry ->
            when (entry) {
                is Item<T> -> {
                    li(content = itemContent(entry))
                }
                is Group<T> -> {
                    section(baseClass = "dropdown".component("group")) {
                        entry.title?.let {
                            h1(baseClass = "dropdown".component("group", "title")) { +it }
                        }
                        ul {
                            entry.items.forEach { groupEntry ->
                                when (groupEntry) {
                                    is Item<T> -> {
                                        li(content = this@DropdownEntries.itemContent(groupEntry))
                                    }
                                    is Separator<T> -> {
                                        pfDivider(DividerVariant.LI)
                                    }
                                    else -> {
                                        console.warn("Nested groups are not supported for ${this@DropdownEntries.dropdown.domNode.debug()}")
                                    }
                                }
                            }
                        }
                    }
                }
                is Separator<T> -> {
                    if (domNode.tagName.toLowerCase() == "ul") {
                        pfDivider(DividerVariant.LI)
                    } else {
                        pfDivider(DividerVariant.DIV)
                    }
                }
            }
        }.bind()
    }

    private fun itemContent(entry: Item<T>): Li.() -> Unit = {
        attr("role", "menuitem")
        button(baseClass = "dropdown".component("menu-item")) {
            attr("tabindex", "-1")
            if (entry.disabled) {
                aria["disabled"] = true
                attr("disabled", "true")
                domNode.classList += Modifier.disabled
            }

            this@DropdownEntries.dropdown.display(entry.item).invoke(this)
            clicks.map { entry.item } handledBy this@DropdownEntries.dropdown.store.clicked
            clicks handledBy this@DropdownEntries.dropdown.ces.collapse

            if (entry.selected) {
                domNode.autofocus = true
            }
        }
    }
}

// ------------------------------------------------------ store

class DropdownStore<T> : RootStore<List<Entry<T>>>(listOf()) {

    internal val clicked: OfferingHandler<T, T> = handleAndOffer { items, item ->
        offer(item)
        items
    }

    val clicks: Flow<T> = clicked.map { it }

    val items: Flow<List<T>> = data.map {
        it.filterIsInstance<Item<T>>()
    }.map { it.map { item -> item.item } }

    val groups: Flow<List<Group<T>>> = data.map { it.filterIsInstance<Group<T>>() }
}
