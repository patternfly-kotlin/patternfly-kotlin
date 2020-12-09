package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithText
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.debug
import org.patternfly.dom.matches
import org.patternfly.dom.minusAssign
import org.patternfly.dom.plusAssign
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

public fun <T> RenderContext.optionsMenu(
    store: OptionStore<T> = OptionStore(),
    align: Align? = null,
    up: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, align, up, id = id, baseClass = baseClass, job), content)

public fun <T> OptionsMenu<T>.optionsMenuToggle(
    id: String? = Id.unique(ComponentType.OptionsMenu.id, "tgl", "btn"),
    baseClass: String? = null,
    content: OptionsMenuToggle<T>.() -> Unit = {}
): OptionsMenuToggle<T> = register(OptionsMenuToggle(this, id = id, baseClass = baseClass, job), content)

public fun <T> OptionsMenu<T>.optionsMenuTogglePlain(
    id: String? = Id.unique(ComponentType.OptionsMenu.id, "tgl", "pln"),
    baseClass: String? = null,
    content: OptionsMenuTogglePlain<T>.() -> Unit = {}
): OptionsMenuTogglePlain<T> =
    register(OptionsMenuTogglePlain(this, id = id, baseClass = baseClass, job), content)

public fun <T> OptionsMenu<T>.optionsMenuItems(
    id: String? = null,
    baseClass: String? = null,
    block: ItemsBuilder<T>.() -> Unit = {}
): OptionsMenuEntries<HTMLUListElement, T> {
    val element = this.register(
        OptionsMenuEntries<HTMLUListElement, T>(this, "ul", id = id, baseClass = baseClass, job), {})
    this.store.update(ItemsBuilder<T>().apply(block).build())
    return element
}

public fun <T> OptionsMenu<T>.optionsMenuGroups(
    id: String? = null,
    baseClass: String? = null,
    block: GroupsBuilder<T>.() -> Unit = {}
): OptionsMenuEntries<HTMLDivElement, T> {
    val element = this.register(
        OptionsMenuEntries<HTMLDivElement, T>(this, "div", id = id, baseClass = baseClass, job), {})
    this.store.update(GroupsBuilder<T>().apply(block).build())
    return element
}

// ------------------------------------------------------ tag

public open class OptionsMenu<T> internal constructor(
    public val store: OptionStore<T>,
    internal val optionsMenuAlign: Align?,
    up: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
    +ComponentType.OptionsMenu
    +optionsMenuAlign?.modifier
    +("top".modifier() `when` up)
    +baseClass
}, job) {
    public lateinit var toggle: OptionsMenuToggleBase<out HTMLElement, T>

    public val ces: CollapseExpandStore = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("options-menu".component("menu-item")))
    }

    public var display: OldComponentDisplay<Button, Item<T>> = {
        {
            +it.item.toString()
        }
    }

    init {
        markAs(ComponentType.OptionsMenu)
        classMap(ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
    }
}

public sealed class OptionsMenuToggleBase<E : HTMLElement, T>(
    private val optionsMenu: OptionsMenu<T>,
    tagName: String,
    id: String? = null,
    baseClass: String? = null,
    job: Job
) : Tag<E>(tagName = tagName, id = id, baseClass = baseClass, job), WithText<E> {

    internal lateinit var toggleId: String

    public abstract fun disabled(value: Boolean)
    public abstract fun disabled(value: Flow<Boolean>)

    internal fun initToggle(toggleTag: Tag<HTMLElement>) {
        with(toggleTag) {
            aria["haspopup"] = "listbox"
            aria["expanded"] = this@OptionsMenuToggleBase.optionsMenu.ces.data.map { it.toString() }
            clicks handledBy this@OptionsMenuToggleBase.optionsMenu.ces.toggle
            this@OptionsMenuToggleBase.toggleId = id ?: Id.unique()
        }
        optionsMenu.toggle = this
    }
}

public class OptionsMenuToggle<T> internal constructor(
    optionsMenu: OptionsMenu<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : OptionsMenuToggleBase<HTMLButtonElement, T>(
    optionsMenu = optionsMenu,
    tagName = "button",
    id = id,
    baseClass = classes {
        +"options-menu".component("toggle")
        +baseClass
    }, job
) {

    init {
        initToggle(this)
    }

    public var content: (Span.() -> Unit)? = null
        set(value) {
            domNode.clear()
            domNode.classList -= "plain".modifier()
            register(span(baseClass = "options-menu".component("toggle", "text")) {
                value?.invoke(this)
            }, {})
            register(span(baseClass = "options-menu".component("toggle", "icon")) {
                icon("caret-down".fas())
            }, {})
            field = value
        }

    public override fun disabled(value: Boolean) {
        attr("disabled", value)
    }

    public override fun disabled(value: Flow<Boolean>) {
        attr("disabled", value)
    }

    public var icon: (Tag<HTMLButtonElement>.() -> Unit)? = null
        set(value) {
            domNode.clear()
            domNode.classList += "plain".modifier()
            value?.invoke(this)
            field = value
        }
}

public class OptionsMenuTogglePlain<T> internal constructor(
    optionsMenu: OptionsMenu<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : OptionsMenuToggleBase<HTMLDivElement, T>(
    optionsMenu = optionsMenu,
    tagName = "div",
    id = id,
    baseClass = classes {
        +"options-menu".component("toggle")
        +"plain".modifier()
        +"text".modifier()
        +baseClass
    }, job
) {

    private var toggleButton: Button

    init {
        toggleButton = button(
            id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
            baseClass = "options-menu".component("toggle", "button")
        ) {
            this@OptionsMenuTogglePlain.initToggle(this)
            span(baseClass = "options-menu".component("toggle", "button", "icon")) {
                icon("caret-down".fas())
            }
        }
    }

    public var content: (Span.() -> Unit)? = null
        set(value) {
            val span = Span(baseClass = "options-menu".component("toggle-text"), job = job).apply {
                value?.invoke(this)
            }
            domNode.prepend(span.domNode)
            field = value
        }

    public override fun disabled(value: Boolean) {
        domNode.classList.toggle("disabled".modifier(), value)
        toggleButton.attr("disabled", value)
    }

    public override fun disabled(value: Flow<Boolean>) {
        classMap(value.map { mapOf("disabled".modifier() to it) })
        toggleButton.attr("disabled", value)
    }
}

public class OptionsMenuEntries<E : HTMLElement, T> internal constructor(
    private val optionsMenu: OptionsMenu<T>,
    tagName: String,
    id: String?,
    baseClass: String?,
    job: Job
) : Tag<E>(tagName = tagName, id = id, baseClass = classes {
    +"options-menu".component("menu")
    +optionsMenu.optionsMenuAlign?.modifier
    +baseClass
}, job) {

    init {
        attr("role", "menu")
        attr("hidden", "true")
        aria["labelledby"] = optionsMenu.toggle.toggleId
        attr("hidden", optionsMenu.ces.data.map { !it })
        optionsMenu.store.data.renderEach { entry ->
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
                                        divider(DividerVariant.LI)
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
                        divider(DividerVariant.LI)
                    } else {
                        divider(DividerVariant.DIV)
                    }
                }
            }
        }
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
            clicks.map { item } handledBy this@OptionsMenuEntries.optionsMenu.store.select
            this@OptionsMenuEntries.optionsMenu.display(item).invoke(this)
            if (item.selected) {
                span(baseClass = "options-menu".component("menu-item", "icon")) {
                    icon("check".fas())
                }
            }
        }
    }
}

// ------------------------------------------------------ store

public class OptionStore<T> : RootStore<List<Entry<T>>>(listOf()) {

    public val items: Flow<List<Item<T>>> = data.flatItems()
    public val groups: Flow<List<Group<T>>> = data.groups()
    public val selection: Flow<List<Item<T>>> = items.drop(1).map { items ->
        items.filter { it.selected }
    }
    public val singleSelection: Flow<Item<T>?> = selection.map { it.firstOrNull() }


    /**
     * Selects and emits the specified item. Use this handler if you just want to handle the payload of [Item] and don't need the [Item] instance itself.
     */
    public val select: EmittingHandler<Item<T>, T> = handleAndEmit { items, item ->
        emit(item.item)
        handleEntries(items, item)
    }

    /**
     * Selects and emits the specified item. Use this handler if you need the [Item] instance.
     */
    public val selectItem: EmittingHandler<Item<T>, Item<T>> = handleAndEmit { items, item ->
        emit(item)
        handleEntries(items, item)
    }

    private fun handleEntries(entries: List<Entry<T>>, item: Item<T>): List<Entry<T>> = entries.map { entry ->
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
}
