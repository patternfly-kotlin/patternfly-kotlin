package org.patternfly

import dev.fritz2.binding.OfferingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SingleMountPoint
import dev.fritz2.binding.action
import dev.fritz2.binding.const
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.Li
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear
import org.patternfly.ToggleType.TEXT
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDropdown(
    store: DropdownStore<T> = DropdownStore(),
    align: Align? = null,
    up: Boolean = false,
    classes: String? = null,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, align, up, classes), content)

fun <T> Dropdown<T>.pfDropdownToggle(
    classes: String? = null,
    content: DropdownToggleText<T>.() -> Unit = {}
): DropdownToggleText<T> = register(DropdownToggleText(this, classes), content)

fun <T> Dropdown<T>.pfDropdownCheckboxToggle(
    classes: String? = null,
    content: DropdownCheckboxToggle<T>.() -> Unit = {}
): DropdownCheckboxToggle<T> = register(DropdownCheckboxToggle(this, classes), content)

fun <T> Dropdown<T>.pfDropdownActionToggle(
    classes: String? = null,
    content: Button.() -> Unit = {}
): DropdownActionToggle<T> = register(DropdownActionToggle(this, classes, content), {})

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

// ------------------------------------------------------ tag

internal enum class ToggleType {
    ACTION, CHECKBOX, ICON, IMAGE, TEXT
}

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
    internal var toggleType: ToggleType = TEXT

    val ces = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("dropdown".component("menu-item")))
    }

    var display: ComponentDisplay<Button, Item<T>> = { item ->
        {
            if (item.description.isNotEmpty()) {
                div(baseClass = "dropdown".component("menu-item", "main")) {
                    item.icon?.let { iconDisplay ->
                        span(baseClass = "dropdown".component("menu-item", "icon")) {
                            iconDisplay(this)
                        }
                    }
                    +item.item.toString()
                }
                div(baseClass = "dropdown".component("menu-item", "description")) {
                    +item.description
                }
            } else {
                item.icon?.let { iconDisplay ->
                    span(baseClass = "dropdown".component("menu-item", "icon")) {
                        iconDisplay(this)
                    }
                }
                +item.item.toString()
            }
        }
    }


    init {
        markAs(ComponentType.Dropdown)
        classMap = ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
    }
}

class DropdownToggleText<T> internal constructor(
    dropdown: Dropdown<T>,
    classes: String?
) : WithTextDelegate<HTMLButtonElement, HTMLSpanElement>,
    Button(id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"), baseClass = classes {
        +"dropdown".component("toggle")
        +"plain".modifier()
        +classes
    }) {

    private var textElement: HTMLSpanElement? = null

    init {
        dropdown.toggleId = id
        dropdown.toggleType = TEXT
        aria["haspopup"] = true
        clicks handledBy dropdown.ces.toggle
        dropdown.ces.data.map { it.toString() }.bindAttr("aria-expanded")
    }

    override fun delegate(): HTMLSpanElement {
        if (textElement == null) {
            domNode.clear()
            domNode.classList -= "plain".modifier()
            textElement = span(baseClass = "dropdown".component("toggle", "text"), content = {}).domNode
            span(baseClass = "dropdown".component("toggle", "icon")) {
                pfIcon("caret-down".fas())
            }
        }
        return textElement!!
    }
}

class DropdownCheckboxToggle<T> internal constructor(
    dropdown: Dropdown<T>,
    classes: String?
) : WithTextDelegate<HTMLDivElement, HTMLSpanElement>,
    Div(baseClass = classes {
        +"dropdown".component("toggle")
        +"split-button".modifier()
        +classes
    }) {

    private val labelTag: Label
    private lateinit var inputTag: Input
    private val toggleTag: Button
    private var textElement: HTMLSpanElement? = null

    var disabled: Flow<Boolean>
        get() {
            throw NotImplementedError()
        }
        set(value) {
            object : SingleMountPoint<Boolean>(value) {
                override fun set(value: Boolean, last: Boolean?) {
                    if (value) {
                        domNode.classList += "disabled".modifier()
                        inputTag.domNode.setAttribute("disabled", "")
                        toggleTag.domNode.setAttribute("disabled", "")
                    } else {
                        domNode.classList -= "disabled".modifier()
                        inputTag.domNode.removeAttribute("disabled")
                        toggleTag.domNode.removeAttribute("disabled")
                    }
                }
            }
        }

    init {
        val checkId = Id.unique(ComponentType.Dropdown.id, "tgl", "chk")
        labelTag = label(baseClass = "dropdown".component("toggle", "check")) {
            `for` = const(checkId)
            this@DropdownCheckboxToggle.inputTag = input(id = checkId) {
                type = const("checkbox")
            }
        }
        toggleTag = button(
            id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
            baseClass = "dropdown".component("toggle", "button")
        ) {
            dropdown.toggleId = id
            aria["haspopup"] = true
            clicks handledBy dropdown.ces.toggle
            dropdown.ces.data.map { it.toString() }.bindAttr("aria-expanded")
            pfIcon("caret-down".fas())
        }
    }

    override fun delegate(): HTMLSpanElement {
        if (textElement == null) {
            val textId = Id.unique(ComponentType.Dropdown.id, "tgl", "txt")
            textElement = labelTag.register(
                span(id = textId, baseClass = "dropdown".component("toggle", "text")) {
                    aria["hidden"] = true
                }, {}).domNode
            inputTag.aria["labelledby"] = textId
        }
        return textElement!!
    }
}

// TODO Implement disable!
@Suppress("JoinDeclarationAndAssignment")
class DropdownActionToggle<T> internal constructor(
    dropdown: Dropdown<T>,
    classes: String?,
    content: Button.() -> Unit
) : Div(baseClass = classes {
    +"dropdown".component("toggle")
    +"split-button".modifier()
    +"action".modifier()
    +classes
}) {

    private val actionTag: Button
    private val toggleTag: Button

    init {
        actionTag = button(baseClass = "dropdown".component("toggle", "button"), content = content)
        toggleTag = button(
            id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
            baseClass = "dropdown".component("toggle", "button")
        ) {
            dropdown.toggleId = id
            aria["haspopup"] = true
            clicks handledBy dropdown.ces.toggle
            dropdown.ces.data.map { it.toString() }.bindAttr("aria-expanded")
            pfIcon("caret-down".fas())
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

    private fun itemContent(item: Item<T>): Li.() -> Unit = {
        attr("role", "menuitem")
        button(baseClass = classes {
            +"dropdown".component("menu-item")
            +("icon".modifier() `when` (item.icon != null))
            +("description".modifier() `when` item.description.isNotEmpty())
        }) {
            attr("tabindex", "-1")
            if (item.disabled) {
                aria["disabled"] = true
                attr("disabled", "true")
                domNode.classList += "disabled".modifier()
            }
            clicks.map { item.item } handledBy this@DropdownEntries.dropdown.store.clicked
            clicks handledBy this@DropdownEntries.dropdown.ces.collapse
            if (item.selected) {
                domNode.autofocus = true
            }
            this@DropdownEntries.dropdown.display(item).invoke(this)
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
