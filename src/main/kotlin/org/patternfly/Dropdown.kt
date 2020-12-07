package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.Listener
import dev.fritz2.dom.Tag
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
import dev.fritz2.elemento.plusAssign
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

/**
 * Creates a [Dropdown] component.
 *
 * @param store the store for the dropdown
 * @param align the alignment of the dropdown
 * @param up controls the direction of the dropdown menu
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.dropdown(
    store: DropdownStore<T> = DropdownStore(),
    align: Align? = null,
    up: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, align, up, id = id, baseClass = baseClass, job), content)

/**
 * Creates a text toggle.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the text toggle
 *
 * @sample DropdownSamples.textToggle
 */
public fun <T> Dropdown<T>.textToggle(baseClass: String? = null, content: Span.() -> Unit) {
    toggle = register(TextToggle(this, baseClass, job, content), {})
}

/**
 * Creates an icon toggle.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the icon toggle
 *
 * @sample DropdownSamples.iconToggle
 */
public fun <T> Dropdown<T>.iconToggle(baseClass: String? = null, content: Button.() -> Unit) {
    toggle = register(IconToggle(this, baseClass, job, content), {})
}

/**
 * Creates an icon toggle using `icon("ellipsis-v".fas())` as the icon.
 *
 * @param baseClass optional CSS class that should be applied to the element
 *
 * @sample DropdownSamples.kebabToggle
 */
public fun <T> Dropdown<T>.kebabToggle(baseClass: String? = null) {
    toggle = register(IconToggle(this, baseClass, job) {
        icon("ellipsis-v".fas())
    }, {})
}

/**
 * Creates a [CheckboxToggle].
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the checkbox toggle
 *
 * @sample DropdownSamples.checkboxToggle
 */
public fun <T> Dropdown<T>.checkboxToggle(baseClass: String? = null, content: CheckboxToggle.() -> Unit) {
    toggle = register(CheckboxToggleContainer(this, baseClass, job, content), {})
}

/**
 * Creates an action toggle and returns a [Listener] (basically a [Flow]) in order to combine the button declaration directly to a fitting _handler_.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the action toggle
 *
 * @sample DropdownSamples.actionToggle
 */
public fun <T> Dropdown<T>.actionToggle(
    baseClass: String? = null,
    content: Button.() -> Unit
): Listener<MouseEvent, HTMLButtonElement> {
    val actionToggle = ActionToggle(this, baseClass, job, content)
    toggle = register(actionToggle, {})
    return actionToggle.clickEvents!!
}

/**
 * Creates a [CustomToggle]. Use this function if you want to have full control over the layout of the toggle.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the custom toggle
 *
 * @sample DropdownSamples.customToggle
 */
public fun <T> Dropdown<T>.customToggle(
    baseClass: String? = null,
    content: CustomToggle<T>.() -> Unit
): CustomToggle<T> {
    val customToggle = register(CustomToggle(this, baseClass, job), content)
    toggle = customToggle
    return customToggle
}

/**
 * Creates an image container inside a [CustomToggle].
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the image container
 */
public fun <T> CustomToggle<T>.toggleImage(baseClass: String? = null, content: Span.() -> Unit): Span =
    span(baseClass = classes("dropdown".component("toggle", "image"), baseClass), content = content)

/**
 * Creates a text container inside a [CustomToggle].
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the text container
 */
public fun <T> CustomToggle<T>.toggleText(baseClass: String? = null, content: Span.() -> Unit): Span =
    span(baseClass = classes("dropdown".component("toggle", "text"), baseClass), content = content)

/**
 * Creates an container inside a [CustomToggle] containing an `icon("caret-down".fas())` icon.
 *
 * @param baseClass optional CSS class that should be applied to the element
 */
public fun <T> CustomToggle<T>.toggleIcon(baseClass: String? = null): Span =
    span(baseClass = classes("dropdown".component("toggle", "text"), baseClass)) {
        icon("caret-down".fas())
    }

/**
 * Starts a block to add dropdown items using the DSL. Use this function if you don't need groups.
 */
public fun <T> Dropdown<T>.items(
    id: String? = null,
    baseClass: String? = null,
    block: ItemsBuilder<T>.() -> Unit = {}
) {
    register(
        DropdownMenu<HTMLUListElement, T>(this, "ul", id = id, baseClass = baseClass, job), {})
    val items = ItemsBuilder<T>().apply(block).build()
    store.update(items)
}

/**
 * Starts a block to add dropdown groups using the DSL.
 */
public fun <T> Dropdown<T>.groups(
    id: String? = null,
    baseClass: String? = null,
    block: GroupsBuilder<T>.() -> Unit = {}
) {
    register(
        DropdownMenu<HTMLDivElement, T>(this, "div", id = id, baseClass = baseClass, job), {})
    val groups = GroupsBuilder<T>().apply(block).build()
    store.update(groups)
}

// ------------------------------------------------------ dropdown tag

/**
 * PatternFly [dropdown](https://www.patternfly.org/v4/components/dropdown/design-guidelines) component.
 *
 * A dropdown presents a menu of actions or links in a constrained space that will trigger a process or navigate to a new location. A dropdown consists of a toggle control to open and close a menu of entries.
 *
 * You can choose between different toggle variations:
 * - text toggle
 * - icon toggle
 * - checkbox toggle
 * - action toggle
 * - custom toggle
 *
 * The data in the menu are wrapped inside instances of [Entry] and managed by a [DropdownStore]. Each [Entry] is either an [Item] or a [Group] of [Item]s. An [Item] can have additional properties such as an icon, a description or a disabled state.
 *
 * **Adding entries**
 *
 * Entries can be added by using the [DropdownStore] or by using the DSL. Items can be grouped. Nested groups are not supported. See the samples below.
 *
 * **Rendering entries**
 *
 * By default the dropdown uses a builtin function to render the [Item]s in the [DropdownStore]. This function takes the [Item.icon] and the [Item.description] into account (if specified). It uses the function passed to [selector] to select a string from [Item.item] which defaults to `{ it.toString() }`.
 *
 * If you don't want to use the builtin defaults you can specify a custom display function by calling [display]. In this case you have full control over the rendering of the wrapped items.
 *
 * @sample DropdownSamples.dropdownDsl
 * @sample DropdownSamples.dropdownStore
 */
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

    private var selector: (T) -> String = { it.toString() }
    private var customDisplay: ComponentDisplay<Button, T>? = null
    private var defaultDisplay: ComponentDisplay<Button, Item<T>> = { item ->
        if (item.description.isNotEmpty()) {
            div(baseClass = "dropdown".component("menu-item", "main")) {
                item.icon?.let { iconDisplay ->
                    span(baseClass = "dropdown".component("menu-item", "icon")) {
                        iconDisplay(this)
                    }
                }
                +this@Dropdown.selector(item.item)
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
            +this@Dropdown.selector(item.item)
        }
    }

    internal var toggle: DropdownToggle<T> = NoopToggle()
    internal val toggleId: String = Id.unique(ComponentType.Dropdown.id, "tgl")

    /**
     * Manages the **c**ollapse / **e**xpand **s**tate of the [Dropdown]. Use this property if you want to track the collapse / expand state.
     *
     * @sample DropdownSamples.ces
     */
    public val ces: CollapseExpandStore = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("dropdown".component("menu-item")))
    }

    init {
        markAs(ComponentType.Dropdown)
        classMap(ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
    }

    /**
     * Sets the selector which is used by the built in display function to select a string from `T`.
     */
    public fun selector(selector: (T) -> String) {
        this.selector = selector
    }

    /**
     * Sets a custom display function to render the dropdown entries.
     */
    public fun display(display: ComponentDisplay<Button, T>) {
        this.customDisplay = display
    }

    internal fun render(item: Item<T>, button: Button) {
        if (customDisplay != null) {
            customDisplay?.invoke(button, item.item)
        } else {
            defaultDisplay.invoke(button, item)
        }
    }
}

// ------------------------------------------------------ dropdown toggle

internal interface DropdownToggle<T> {

    fun initToggle(dropdown: Dropdown<T>, tag: Tag<HTMLElement>) {
        with(tag) {
            domNode.id = dropdown.toggleId
            aria["haspopup"] = true
            aria["expanded"] = dropdown.ces.data.map { it.toString() }
            clicks handledBy dropdown.ces.toggle
        }
    }
}

internal class NoopToggle<T> : DropdownToggle<T>

internal class TextToggle<T>(
    dropdown: Dropdown<T>,
    baseClass: String?,
    job: Job,
    content: Span.() -> Unit
) : DropdownToggle<T>,
    Button(baseClass = classes("dropdown".component("toggle"), baseClass), job = job) {

    init {
        initToggle(dropdown, this)
        span(baseClass = "dropdown".component("toggle", "text")) {
            content(this)
        }
        span(baseClass = "dropdown".component("toggle", "icon")) {
            icon("caret-down".fas())
        }
    }
}

internal class IconToggle<T>(
    dropdown: Dropdown<T>,
    baseClass: String?,
    job: Job,
    content: Button.() -> Unit
) : DropdownToggle<T>,
    Button(baseClass = classes {
        +"dropdown".component("toggle")
        +"plain".modifier()
        +baseClass
    }, job = job) {

    init {
        initToggle(dropdown, this)
        content(this)
    }
}

internal class CheckboxToggleContainer<T>(
    dropdown: Dropdown<T>,
    baseClass: String?,
    job: Job,
    content: CheckboxToggle.() -> Unit,
) : DropdownToggle<T>,
    Div(baseClass = classes {
        +"dropdown".component("toggle")
        +"split-button".modifier()
        +baseClass
    }, job = job) {

    private val label: Label
    private lateinit var checkbox: Input

    init {
        val inputId = Id.unique(ComponentType.Dropdown.id, "tgl", "chk")
        label = label(baseClass = "dropdown".component("toggle", "check")) {
            `for`(inputId)
            this@CheckboxToggleContainer.checkbox = input(id = inputId) {
                type("checkbox")
            }
        }
        initToggle(dropdown, button(baseClass = "dropdown".component("toggle", "button")) {
            icon("caret-down".fas())
        })
        CheckboxToggle(label, checkbox, job).apply(content)
    }
}

/**
 * Provides access to the [text] and [checkbox] of a checkbox toggle.
 *
 * @sample DropdownSamples.checkboxToggle
 */
public class CheckboxToggle internal constructor(
    private val label: Label,
    private val checkbox: Input,
    private val job: Job
) {

    private var text: Span? = null

    /**
     * Sets up the text of this checkbox toggle.
     */
    public fun text(content: Span.() -> Unit) {
        if (text == null) {
            text = label.register(
                Span(
                    id = Id.unique(ComponentType.Dropdown.id, "tgl", "txt"),
                    baseClass = "dropdown".component("toggle", "text"),
                    job
                ), {
                    it.aria["hidden"] = true
                })
            checkbox.aria["labelledby"] = text!!.domNode.id
        }
        content.invoke(text!!)
    }

    /**
     * Sets up the checkbox of this checkbox toggle.
     */
    public fun checkbox(block: Input.() -> Unit) {
        block.invoke(checkbox)
    }
}

internal class ActionToggle<T>(
    dropdown: Dropdown<T>,
    baseClass: String?,
    job: Job,
    content: Button.() -> Unit
) : DropdownToggle<T>,
    Div(baseClass = classes {
        +"dropdown".component("toggle")
        +"split-button".modifier()
        +"action".modifier()
        +baseClass
    }, job = job) {

    internal var clickEvents: Listener<MouseEvent, HTMLButtonElement>? = null

    init {
        button(baseClass = "dropdown".component("toggle", "button")) {
            this@ActionToggle.clickEvents = clicks
            content(this)
        }
        initToggle(dropdown, button(baseClass = "dropdown".component("toggle", "button")) {
            icon("caret-down".fas())
        })
    }
}

/**
 * Custom toggle of a [Dropdown] component. Allows full control over the content of the toggle. You can use one of the `toggle` function to add specific content.
 *
 * @sample DropdownSamples.customToggle
 */
public class CustomToggle<T>(dropdown: Dropdown<T>, baseClass: String?, job: Job) :
    DropdownToggle<T>,
    Button(baseClass = classes("dropdown".component("toggle"), baseClass), job = job) {

    init {
        initToggle(dropdown, this)
    }
}

// ------------------------------------------------------ dropdown menu

internal class DropdownMenu<E : HTMLElement, T> internal constructor(
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
        aria["labelledby"] = dropdown.toggleId

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
                                        li(content = this@DropdownMenu.itemContent(groupEntry))
                                    }
                                    is Separator<T> -> {
                                        divider(DividerVariant.LI)
                                    }
                                    else -> {
                                        console.warn("Nested groups are not supported for ${this@DropdownMenu.dropdown.domNode.debug()}")
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
            clicks handledBy this@DropdownMenu.dropdown.ces.collapse
            clicks.map { item } handledBy this@DropdownMenu.dropdown.store.select
            if (item.selected) {
                domNode.autofocus = true
            }
            this@DropdownMenu.dropdown.render(item, this)
        }
    }
}

// ------------------------------------------------------ store

/**
 * Store containing the data shown in a dropdown. The data is wrapped inside instances of [Entry]. An entry is either an [Item] or a [Group] of [Item]s. An [Item] can have additional properties such as an icon, a description or a disabled state.
 */
public class DropdownStore<T> : RootStore<List<Entry<T>>>(listOf()) {

    /**
     * Returns a flow containing a list of all items in all groups (if any).
     */
    public val items: Flow<List<Item<T>>> = data.flatItems()

    /**
     * Returns a flow containing a list of all groups.
     */
    public val groups: Flow<List<Group<T>>> = data.groups()

    /**
     * Wraps the specified data inside instances of [Item] and adds them to the list of existing entries.
     */
    public val addAll: SimpleHandler<List<T>> = handle { items, newItems ->
        items + newItems.map {
            Item(it, disabled = false, selected = false, description = "", icon = null, group = null)
        }
    }

    /**
     * Adds all specified items to the list of entries.
     */
    public val addAllItems: SimpleHandler<List<Entry<T>>> = handle { items, newItems -> items + newItems }

    /**
     * Selects and emits the specified item.
     */
    public val select: EmittingHandler<Item<T>, Item<T>> = handleAndEmit { items, item ->
        emit(item)
        items
    }
}
