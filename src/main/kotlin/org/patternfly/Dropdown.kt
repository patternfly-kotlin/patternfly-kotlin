package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithText
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.elemento.By
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.debug
import dev.fritz2.elemento.matches
import dev.fritz2.elemento.minusAssign
import dev.fritz2.elemento.plusAssign
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

public fun <T> RenderContext.dropdown(
    store: DropdownStore<T> = DropdownStore(),
    align: Align? = null,
    up: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, align, up, id = id, baseClass = baseClass, job), content)

public fun <T> Dropdown<T>.dropdownToggle(
    id: String? = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
    baseClass: String? = null,
    content: DropdownToggle<T>.() -> Unit = {}
): DropdownToggle<T> = register(DropdownToggle(this, id = id, baseClass = baseClass, job), content)

public fun <T> Dropdown<T>.dropdownKebabToggle(
    id: String? = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
    baseClass: String? = null,
    content: DropdownToggle<T>.() -> Unit = {}
): DropdownToggle<T> = register(
    DropdownToggle(this, id = id, baseClass = baseClass, job).apply {
        icon = { icon("ellipsis-v".fas()) }
    }, content
)

public fun <T> Dropdown<T>.dropdownCheckboxToggle(
    id: String? = null,
    baseClass: String? = null,
    content: DropdownToggleCheckbox<T>.() -> Unit = {}
): DropdownToggleCheckbox<T> = register(DropdownToggleCheckbox(this, id = id, baseClass = baseClass, job), content)

public fun <T> Dropdown<T>.dropdownActionToggle(
    id: String? = null,
    baseClass: String? = null,
    content: DropdownToggleAction<T>.() -> Unit = {}
): DropdownToggleAction<T> = register(DropdownToggleAction(this, id = id, baseClass = baseClass, job), content)

public fun <T> Dropdown<T>.dropdownItems(
    id: String? = null,
    baseClass: String? = null,
    block: ItemsBuilder<T>.() -> Unit = {}
): DropdownEntries<HTMLUListElement, T> {
    val element = this.register(
        DropdownEntries<HTMLUListElement, T>(this, "ul", id = id, baseClass = baseClass, job), {})
    val items = ItemsBuilder<T>().apply(block).build()
    store.update(items)
    return element
}

public fun <T> Dropdown<T>.dropdownGroups(
    id: String? = null,
    baseClass: String? = null,
    block: GroupsBuilder<T>.() -> Unit = {}
): DropdownEntries<HTMLDivElement, T> {
    val element = this.register(
        DropdownEntries<HTMLDivElement, T>(this, "div", id = id, baseClass = baseClass, job), {})
    val groups = GroupsBuilder<T>().apply(block).build()
    store.update(groups)
    return element
}

// ------------------------------------------------------ tag

public open class Dropdown<T> internal constructor(
    public val store: DropdownStore<T>,
    internal val dropdownAlign: Align?,
    up: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
    +ComponentType.Dropdown
    +dropdownAlign?.modifier
    +("top".modifier() `when` up)
    +baseClass
}, job) {
    public lateinit var toggle: DropdownToggleBase<out HTMLElement, T>

    public val ces: CollapseExpandStore = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("dropdown".component("menu-item")))
    }

    public var display: ComponentDisplay<Button, Item<T>> = { item ->
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
        classMap(ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
    }

    public fun display(display: ComponentDisplay2<Button, T>) {
        store.data.renderEach { entry ->
            button {
                if (entry is Item<T>) {
                    display.invoke(this, entry.item)
                }
            }
        }
    }
}

public sealed class DropdownToggleBase<E : HTMLElement, T>(
    private val dropdown: Dropdown<T>,
    tagName: String,
    id: String?,
    baseClass: String?,
    job: Job
) : Tag<E>(tagName = tagName, id = id, baseClass = baseClass, job), WithText<E> {

    internal lateinit var toggleId: String

    public abstract fun disabled(value: Boolean)
    public abstract fun disabled(value: Flow<Boolean>)

    internal fun initToggle(toggleTag: Tag<HTMLElement>) {
        with(toggleTag) {
            aria["haspopup"] = true
            aria["expanded"] = this@DropdownToggleBase.dropdown.ces.data.map { it.toString() }
            this@DropdownToggleBase.toggleId = id ?: Id.unique()
            clicks handledBy this@DropdownToggleBase.dropdown.ces.toggle
        }
        dropdown.toggle = this
    }
}

public class DropdownToggle<T> internal constructor(
    dropdown: Dropdown<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : DropdownToggleBase<HTMLButtonElement, T>(
    dropdown = dropdown,
    tagName = "button",
    id = id,
    baseClass = classes("dropdown".component("toggle"), baseClass), job
) {
    init {
        initToggle(this)
    }

    public var content: (Span.() -> Unit)? = null
        set(value) {
            domNode.clear()
            domNode.classList -= "plain".modifier()
            register(span(baseClass = "dropdown".component("toggle", "text")) {
                value?.invoke(this)
            }, {})
            register(span(baseClass = "dropdown".component("toggle", "icon")) {
                icon("caret-down".fas())
            }, {})
            field = value
        }

    public var icon: (Tag<HTMLButtonElement>.() -> Unit)? = null
        set(value) {
            domNode.clear()
            domNode.classList += "plain".modifier()
            value?.invoke(this)
            field = value
        }

    override fun disabled(value: Boolean) {
        attr("disabled", value)
    }

    override fun disabled(value: Flow<Boolean>) {
        attr("disabled", value)
    }
}

public class DropdownToggleCheckbox<T> internal constructor(
    dropdown: Dropdown<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : DropdownToggleBase<HTMLDivElement, T>(
    dropdown = dropdown,
    tagName = "div",
    id = id,
    baseClass = classes {
        +"dropdown".component("toggle")
        +"split-button".modifier()
        +baseClass
    },
    job
) {

    private val labelTag: Label
    private val toggleButton: Button
    private val checkId = Id.unique(ComponentType.Dropdown.id, "tgl", "chk")
    private val textId = Id.unique(ComponentType.Dropdown.id, "tgl", "txt")

    public val input: Input = Input(id = checkId, job = job)

    init {
        labelTag = label(baseClass = "dropdown".component("toggle", "check")) {
            `for`(this@DropdownToggleCheckbox.checkId)
            register(this@DropdownToggleCheckbox.input) {
                it.type("checkbox")
            }
        }
        toggleButton = button(
            id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
            baseClass = "dropdown".component("toggle", "button")
        ) {
            this@DropdownToggleCheckbox.initToggle(this)
            icon("caret-down".fas())
        }
    }

    public fun text(label: Span.() -> Unit) {
        labelTag.register(
            span(id = textId, baseClass = "dropdown".component("toggle", "text")) {
                aria["hidden"] = true
                label(this)
            }, {})
    }

    override fun disabled(value: Boolean) {
        domNode.classList.toggle("disabled".modifier(), value)
        input.attr("disabled", value)
        toggleButton.attr("disabled", value)
    }

    override fun disabled(value: Flow<Boolean>) {
        classMap(value.map { mapOf("disabled".modifier() to it) })
        input.attr("disabled", value)
        toggleButton.attr("disabled", value)
    }

    public fun triState(value: Flow<TriState>) {
        mountSingle(job, value) { v, _ ->
            when (v) {
                TriState.OFF -> {
                    input.domNode.checked = false
                    input.domNode.indeterminate = false
                }
                TriState.INDETERMINATE -> {
                    input.domNode.checked = false
                    input.domNode.indeterminate = true
                }
                TriState.ON -> {
                    input.domNode.checked = true
                    input.domNode.indeterminate = false
                }
            }
        }
    }
}

public class DropdownToggleAction<T> internal constructor(
    dropdown: Dropdown<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : DropdownToggleBase<HTMLDivElement, T>(
    dropdown = dropdown,
    tagName = "div",
    id = id,
    baseClass = classes {
        +"dropdown".component("toggle")
        +"split-button".modifier()
        +"action".modifier()
        +baseClass
    },
    job
) {
    private val toggleButton = button(
        id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
        baseClass = "dropdown".component("toggle", "button")
    ) {
        this@DropdownToggleAction.initToggle(this)
        icon("caret-down".fas())
    }
    private var actionButton: Button? = null

    public fun action(action: Button.() -> Unit) {
        actionButton = Button(baseClass = "dropdown".component("toggle", "button"), job = job).apply {
            action(this)
        }
        domNode.prepend(actionButton?.domNode)
    }

    override fun disabled(value: Boolean) {
        domNode.classList.toggle("disabled".modifier(), value)
        toggleButton.disabled(value)
        actionButton?.disabled(value)
    }

    override fun disabled(value: Flow<Boolean>) {
        classMap(value.map { mapOf("disabled".modifier() to it) })
        toggleButton.disabled(value)
        actionButton?.disabled(value)
    }
}

public class DropdownEntries<E : HTMLElement, T> internal constructor(
    private val dropdown: Dropdown<T>,
    tagName: String,
    id: String?,
    baseClass: String?,
    job: Job
) : Tag<E>(tagName = tagName, id = id, baseClass = classes {
    +"dropdown".component("menu")
    +dropdown.dropdownAlign?.modifier
    +baseClass
}, job) {
    init {
        attr("role", "menu")
        attr("hidden", dropdown.ces.data.map { !it })
        aria["labelledby"] = dropdown.toggle.toggleId

        dropdown.store.data.renderEach { entry ->
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
                                        divider(DividerVariant.LI)
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
            clicks handledBy this@DropdownEntries.dropdown.ces.collapse
            clicks.map { item } handledBy this@DropdownEntries.dropdown.store.select
            if (item.selected) {
                domNode.autofocus = true
            }
            this@DropdownEntries.dropdown.display(item).invoke(this)
        }
    }
}

// ------------------------------------------------------ store

public class DropdownStore<T> : RootStore<List<Entry<T>>>(listOf()) {

    /**
     * Adds all specified items to the list of items.
     */
    public val addAll: SimpleHandler<List<Entry<T>>> = handle { items, newItems -> items + newItems }

    public val select: EmittingHandler<Item<T>, Item<T>> = handleAndEmit { items, item ->
        emit(item)
        items
    }

    public val items: Flow<List<Item<T>>> = data.flatItems()
    public val groups: Flow<List<Group<T>>> = data.groups()
}
