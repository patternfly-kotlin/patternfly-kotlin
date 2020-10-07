package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
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
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfOptionsMenu(
    store: OptionStore<T> = OptionStore(),
    align: Align? = null,
    up: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, align, up, id = id, baseClass = baseClass), content)

fun <T> OptionsMenu<T>.pfOptionsMenuToggle(
    id: String? = Id.unique(ComponentType.OptionsMenu.id, "tgl", "btn"),
    baseClass: String? = null,
    content: OptionsMenuToggle<T>.() -> Unit = {}
): OptionsMenuToggle<T> = register(OptionsMenuToggle(this, id = id, baseClass = baseClass), content)

fun <T> OptionsMenu<T>.pfOptionsMenuTogglePlain(
    id: String? = Id.unique(ComponentType.OptionsMenu.id, "tgl", "pln"),
    baseClass: String? = null,
    content: OptionsMenuTogglePlain<T>.() -> Unit = {}
): OptionsMenuTogglePlain<T> = register(OptionsMenuTogglePlain(this, id = id, baseClass = baseClass), content)

fun <T> OptionsMenu<T>.pfOptionsMenuItems(
    id: String? = null,
    baseClass: String? = null,
    block: ItemsBuilder<T>.() -> Unit = {}
): OptionsMenuEntries<HTMLUListElement, T> {
    val element = this.register(
        OptionsMenuEntries<HTMLUListElement, T>(this, "ul", id = id, baseClass = baseClass), {})
    val items = ItemsBuilder<T>().apply(block).build()
    action(items) handledBy this.store.update
    return element
}

fun <T> OptionsMenu<T>.pfOptionsMenuGroups(
    id: String? = null,
    baseClass: String? = null,
    block: GroupsBuilder<T>.() -> Unit = {}
): OptionsMenuEntries<HTMLDivElement, T> {
    val element = this.register(
        OptionsMenuEntries<HTMLDivElement, T>(this, "div", id = id, baseClass = baseClass), {})
    val groups = GroupsBuilder<T>().apply(block).build()
    action(groups) handledBy this.store.update
    return element
}

// ------------------------------------------------------ tag

open class OptionsMenu<T> internal constructor(
    val store: OptionStore<T>,
    internal val optionsMenuAlign: Align?,
    up: Boolean,
    id: String?,
    baseClass: String?
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
    +ComponentType.OptionsMenu
    +optionsMenuAlign?.modifier
    +("top".modifier() `when` up)
    +baseClass
}) {
    lateinit var toggle: OptionsMenuToggleBase<out HTMLElement, T>

    val ces = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("options-menu".component("menu-item")))
    }

    var display: ComponentDisplay<Button, Item<T>> = {
        {
            +it.item.toString()
        }
    }

    init {
        markAs(ComponentType.OptionsMenu)
        classMap = ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
    }
}

sealed class OptionsMenuToggleBase<E : HTMLElement, T>(
    private val optionsMenu: OptionsMenu<T>,
    tagName: String,
    id: String? = null,
    baseClass: String? = null,
) : Tag<E>(tagName = tagName, id = id, baseClass = baseClass), WithText<E> {

    internal lateinit var toggleId: String
    abstract var disabled: Flow<Boolean>

    internal fun initToggle(toggleTag: Tag<HTMLElement>) {
        with(toggleTag) {
            aria["haspopup"] = "listbox"
            clicks handledBy this@OptionsMenuToggleBase.optionsMenu.ces.toggle
            this@OptionsMenuToggleBase.toggleId = id ?: Id.unique()
            this@OptionsMenuToggleBase.optionsMenu.ces.data.map { it.toString() }.bindAttr("aria-expanded")
        }
        optionsMenu.toggle = this
    }
}

class OptionsMenuToggle<T> internal constructor(
    optionsMenu: OptionsMenu<T>,
    id: String?,
    baseClass: String?,
) : OptionsMenuToggleBase<HTMLButtonElement, T>(
    optionsMenu = optionsMenu,
    tagName = "button",
    id = id,
    baseClass = classes {
        +"options-menu".component("toggle")
        +baseClass
    }) {
    init {
        initToggle(this)
    }

    var content: (Span.() -> Unit)? = null
        set(value) {
            domNode.clear()
            domNode.classList -= "plain".modifier()
            register(span(baseClass = "options-menu".component("toggle", "text")) {
                value?.invoke(this)
            }, {})
            register(span(baseClass = "options-menu".component("toggle", "icon")) {
                pfIcon("caret-down".fas())
            }, {})
            field = value
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

    var icon: (Tag<HTMLButtonElement>.() -> Unit)? = null
        set(value) {
            domNode.clear()
            domNode.classList += "plain".modifier()
            value?.invoke(this)
            field = value
        }
}

class OptionsMenuTogglePlain<T> internal constructor(
    optionsMenu: OptionsMenu<T>,
    id: String?,
    baseClass: String?,
) : OptionsMenuToggleBase<HTMLDivElement, T>(
    optionsMenu = optionsMenu,
    tagName = "div",
    id = id,
    baseClass = classes {
        +"options-menu".component("toggle")
        +"plain".modifier()
        +"text".modifier()
        +baseClass
    }) {
    private var toggleButton: Button

    init {
        toggleButton = button(
            id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
            baseClass = "options-menu".component("toggle", "button")
        ) {
            this@OptionsMenuTogglePlain.initToggle(this)
            span(baseClass = "options-menu".component("toggle", "button", "icon")) {
                pfIcon("caret-down".fas())
            }
        }
    }

    var content: (Span.() -> Unit)? = null
        set(value) {
            val span = Span(baseClass = "options-menu".component("toggle-text")).apply {
                value?.invoke(this)
            }
            domNode.prepend(span.domNode)
            field = value
        }

    override var disabled: Flow<Boolean>
        get() = throw NotImplementedError()
        set(flow) {
            object : SingleMountPoint<Boolean>(flow) {
                override fun set(value: Boolean, last: Boolean?) {
                    domNode.classList.toggle("disabled".modifier(), value)
                    toggleButton.domNode.disabled = value
                }
            }
        }
}

class OptionsMenuEntries<E : HTMLElement, T> internal constructor(
    private val optionsMenu: OptionsMenu<T>,
    tagName: String,
    id: String?,
    baseClass: String?
) : Tag<E>(tagName = tagName, id = id, baseClass = classes {
    +"options-menu".component("menu")
    +optionsMenu.optionsMenuAlign?.modifier
    +baseClass
}) {
    init {
        attr("role", "menu")
        aria["labelledby"] = optionsMenu.toggle.toggleId
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

    internal val toggle: SimpleHandler<Item<T>> = handle { entries, item ->
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
            entry.copy(selected = false)
        }

    val items: Flow<List<Item<T>>> = data.flatItems()
    val groups: Flow<List<Group<T>>> = data.groups()
    val selection: Flow<List<Item<T>>> = items.drop(1).map { items ->
        items.filter { it.selected }
    }
    val singleSelection: Flow<Item<T>?> = selection.map { it.firstOrNull() }
}
