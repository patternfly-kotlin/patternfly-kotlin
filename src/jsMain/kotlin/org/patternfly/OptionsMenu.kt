package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SingleMountPoint
import dev.fritz2.binding.action
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithText
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear
import org.patternfly.SelectionMode.SINGLE
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfOptionsMenu(
    store: OptionStore<T> = OptionStore(),
    selectionMode: SelectionMode = SINGLE,
    align: Align? = null,
    up: Boolean = false,
    classes: String? = null,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, selectionMode, align, up, classes), content)

fun <T> OptionsMenu<T>.pfOptionsMenuToggle(
    classes: String? = null,
    content: OptionsMenuToggle<T>.() -> Unit = {}
): OptionsMenuToggle<T> = register(OptionsMenuToggle(this, classes), content)

fun <T> OptionsMenu<T>.pfOptionsMenuTogglePlain(
    classes: String? = null,
    content: OptionsMenuTogglePlain<T>.() -> Unit = {}
): OptionsMenuTogglePlain<T> = register(OptionsMenuTogglePlain(this, classes), content)

fun <T> OptionsMenu<T>.pfOptionsMenuItems(
    classes: String? = null,
    block: ItemsBuilder<T>.() -> Unit = {}
): OptionsMenuEntries<HTMLUListElement, T> {
    val element = this.register(
        OptionsMenuEntries<HTMLUListElement, T>(this, "ul", classes), {})
    val items = ItemsBuilder<T>().apply(block).build()
    action(items) handledBy this.store.update
    return element
}

fun <T> OptionsMenu<T>.pfOptionsMenuGroups(
    classes: String? = null,
    block: GroupsBuilder<T>.() -> Unit = {}
): OptionsMenuEntries<HTMLDivElement, T> {
    val element = this.register(
        OptionsMenuEntries<HTMLDivElement, T>(this, "div", classes), {})
    val groups = GroupsBuilder<T>().apply(block).build()
    action(groups) handledBy this.store.update
    return element
}

// ------------------------------------------------------ tag

class OptionsMenu<T> internal constructor(
    val store: OptionStore<T>,
    selectionMode: SelectionMode,
    internal val optionsMenuAlign: Align?,
    up: Boolean,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +ComponentType.OptionsMenu
    +optionsMenuAlign?.modifier
    +("top".modifier() `when` up)
    +classes
}) {

    internal var toggle: OptionsMenuToggleBase<out HTMLElement, T>? = null

    val ces = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("options-menu".component("menu-item")))
    }

    var display: ComponentDisplay<Button, Item<T>> = {
        {
            +it.item.toString()
        }
    }

    init {
        store.selectionMode = selectionMode
        markAs(ComponentType.OptionsMenu)
        classMap = ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
    }
}

sealed class OptionsMenuToggleBase<E : HTMLElement, T>(
    private val optionsMenu: OptionsMenu<T>,
    tagName: String,
    id: String? = null,
    baseClass: String? = null,
) : Tag<E>(tagName, id, baseClass), WithText<E> {

    internal var toggleId: String? = null
    abstract var disabled: Flow<Boolean>

    internal fun initToggle(toggleTag: Tag<HTMLElement>) {
        with(toggleTag) {
            aria["haspopup"] = "listbox"
            clicks handledBy this@OptionsMenuToggleBase.optionsMenu.ces.toggle
            this@OptionsMenuToggleBase.toggleId = id
            this@OptionsMenuToggleBase.optionsMenu.ces.data.map { it.toString() }.bindAttr("aria-expanded")
        }
        optionsMenu.toggle = this
    }
}

class OptionsMenuToggle<T> internal constructor(
    optionsMenu: OptionsMenu<T>,
    classes: String?,
) : OptionsMenuToggleBase<HTMLButtonElement, T>(
    optionsMenu = optionsMenu,
    tagName = "button",
    id = Id.unique(ComponentType.OptionsMenu.id, "tgl", "btn"),
    baseClass = classes {
        +"options-menu".component("toggle")
        +"plain".modifier()
        +classes
    }),
    WithTextDelegate<HTMLButtonElement, HTMLSpanElement> {

    private var textElement: HTMLSpanElement? = null

    init {
        initToggle(this)
    }

    var icon: (Tag<HTMLButtonElement>.() -> Unit)? = null
        set(value) {
            field = value
            if (textElement != null) {
                textElement.removeFromParent()
                textElement = null
            }
            value?.invoke(this)
        }

    override var disabled: Flow<Boolean>
        get() {
            throw NotImplementedError()
        }
        set(flow) {
            object : SingleMountPoint<Boolean>(flow) {
                override fun set(value: Boolean, last: Boolean?) {
                    domNode.disabled = value
                }
            }
        }

    override fun delegate(): HTMLSpanElement {
        if (textElement == null) {
            domNode.clear()
            domNode.classList -= "plain".modifier()
            textElement = register(span(baseClass = "options-menu".component("toggle", "text")) {}, {}).domNode
            register(span(baseClass = "options-menu".component("toggle", "icon")) {
                pfIcon("caret-down".fas())
            }, {})
        }
        return textElement!!
    }
}

class OptionsMenuTogglePlain<T> internal constructor(
    optionsMenu: OptionsMenu<T>,
    classes: String?,
) : OptionsMenuToggleBase<HTMLDivElement, T>(
    optionsMenu = optionsMenu,
    tagName = "div",
    id = Id.unique(ComponentType.OptionsMenu.id, "tgl", "pln"),
    baseClass = classes {
        +"options-menu".component("toggle")
        +"plain".modifier()
        +"text".modifier()
        +classes
    }),
    WithTextDelegate<HTMLDivElement, HTMLSpanElement> {

    private var textElement: HTMLSpanElement =
        span(baseClass = "options-menu".component("toggle-text")) { }.domNode
    private var toggleTag: Button

    init {
        toggleTag = button(
            id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
            baseClass = "options-menu".component("toggle", "button")
        ) {
            this@OptionsMenuTogglePlain.initToggle(this)
            span(baseClass = "options-menu".component("toggle", "button", "icon")) {
                pfIcon("caret-down".fas())
            }
        }
    }

    override var disabled: Flow<Boolean>
        get() = throw NotImplementedError()
        set(flow) {
            object : SingleMountPoint<Boolean>(flow) {
                override fun set(value: Boolean, last: Boolean?) {
                    domNode.classList.toggle("disabled".modifier(), value)
                    toggleTag.domNode.disabled = value
                }
            }
        }

    override fun delegate(): HTMLSpanElement = textElement
}

class OptionsMenuEntries<E : HTMLElement, T> internal constructor(
    private val optionsMenu: OptionsMenu<T>,
    tagName: String,
    classes: String?
) : Tag<E>(tagName = tagName, baseClass = classes {
    +"options-menu".component("menu")
    +optionsMenu.optionsMenuAlign?.modifier
    +classes
}) {
    init {
        attr("role", "menu")
        optionsMenu.toggle?.toggleId?.let {
            aria["labelledby"] = it
        }
        optionsMenu.ces.data.map { !it }.bindAttr("hidden")
        optionsMenu.store.data.each().render { entry ->
            when (entry) {
                is Item<T> -> {
                    li(content = itemContent(entry))
                }
                is Group<T> -> {
                    section(baseClass = "options-menu".component("group")) {
                        entry.title?.let {
                            h1(baseClass = "options-menu".component("group", "title")) { +it }
                        }
                        ul {
                            entry.items.forEach { groupEntry ->
                                when (groupEntry) {
                                    is Item<T> -> {
                                        li(content = this@OptionsMenuEntries.itemContent(groupEntry))
                                    }
                                    is Separator<T> -> {
                                        pfDivider(DividerVariant.LI)
                                    }
                                    else -> {
                                        console.warn("Nested groups are not supported for ${this@OptionsMenuEntries.optionsMenu.domNode.debug()}")
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
        button(baseClass = "options-menu".component("menu-item")) {
            attr("tabindex", "-1")
            if (item.disabled) {
                aria["disabled"] = true
                attr("disabled", "true")
                domNode.classList += "disabled".modifier()
            }
            clicks.map { item } handledBy this@OptionsMenuEntries.optionsMenu.store.toggle
            this@OptionsMenuEntries.optionsMenu.display(item).invoke(this)
            if (item.selected) {
                span(baseClass = "options-menu".component("menu-item", "icon")) {
                    pfIcon("check".fas())
                }
            }
        }
    }
}

// ------------------------------------------------------ store

class OptionStore<T> : RootStore<List<Entry<T>>>(listOf()) {

    internal var selectionMode: SelectionMode = SINGLE
        set(value) {
            field = if (value == SelectionMode.NONE) {
                console.warn("Selection mode $value is not supported for options menu")
                SINGLE
            } else {
                value
            }
        }

    internal val toggle = handle<Item<T>> { entries, item ->
        entries.map { entry ->
            when (entry) {
                is Item<T> -> handleSelection(entry, item)
                is Group<T> -> {
                    if (entry.id == item.group?.id) {
                        val groupItems = entry.items.map { groupEntry ->
                            when (groupEntry) {
                                is Item<T> -> handleSelection(groupEntry, item)
                                else -> groupEntry
                            }
                        }
                        entry.copy(items = groupItems)
                    } else {
                        entry
                    }
                }
                else -> entry
            }
        }
    }

    private fun handleSelection(entry: Item<T>, current: Item<T>): Entry<T> =
        if (entry.item == current.item) {
            if (entry.selected) {
                entry
            } else {
                entry.copy(selected = true)
            }
        } else {
            if (selectionMode == SINGLE) {
                entry.copy(selected = false)
            } else {
                entry
            }
        }

    private val wrappedItems = data.map {
        it.flatMap { entry ->
            when (entry) {
                is Item<T> -> listOf(entry)
                is Group<T> -> entry.items
                is Separator<T> -> emptyList()
            }
        }.filterIsInstance<Item<T>>()
    }

    val items: Flow<List<T>> = wrappedItems.map { items -> items.map { it.item } }

    val groups: Flow<List<Group<T>>> = data.map { it.filterIsInstance<Group<T>>() }

    val selection: Flow<List<T>> = wrappedItems.drop(1).map { items ->
        items.filter { it.selected }.map { it.item }
    }

    val singleSelection: Flow<T?> = selection.map { it.firstOrNull() }
}
