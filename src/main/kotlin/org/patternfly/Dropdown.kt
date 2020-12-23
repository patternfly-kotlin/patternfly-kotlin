@file:Suppress("TooManyFunctions")

package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.Listener
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.debug
import org.patternfly.dom.matches
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

/**
 * Creates a [Dropdown] component.
 *
 * @param store the store for the dropdown
 * @param grouped whether the dropdown contains groups or just flat items
 * @param align the alignment of the dropdown
 * @param up controls the direction of the dropdown menu
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.dropdown(
    store: DropdownStore<T> = DropdownStore(),
    grouped: Boolean = false,
    align: Align? = null,
    up: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, grouped, align, up, id = id, baseClass = baseClass, job), content)

/**
 * Creates a text toggle. Specify the text using the [content] function.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the text toggle
 *
 * @sample org.patternfly.sample.DropdownSample.textToggle
 */
public fun <T> Dropdown<T>.textToggle(
    vararg variation: ButtonVariation,
    baseClass: String? = null,
    content: Span.() -> Unit
) {
    assignToggle(DropdownTextToggle(this, variation, baseClass, job, content))
}

/**
 * Creates an icon toggle. Use the [content] function to setup the icon.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the icon toggle
 *
 * @sample org.patternfly.sample.DropdownSample.iconToggle
 */
public fun <T> Dropdown<T>.iconToggle(baseClass: String? = null, content: Button.() -> Unit) {
    assignToggle(DropdownIconToggle(this, baseClass, job, content))
}

/**
 * Creates an icon toggle using `icon("ellipsis-v".fas())` as the icon.
 *
 * @param baseClass optional CSS class that should be applied to the element
 *
 * @sample org.patternfly.sample.DropdownSample.kebabToggle
 */
public fun <T> Dropdown<T>.kebabToggle(baseClass: String? = null) {
    assignToggle(
        DropdownIconToggle(
            this,
            baseClass,
            job
        ) {
            icon("ellipsis-v".fas())
        }
    )
}

/**
 * Creates a [DropdownCheckboxToggle]. A checkbox toggle can contain optional text.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the checkbox toggle
 *
 * @sample org.patternfly.sample.DropdownSample.checkboxToggle
 */
public fun <T> Dropdown<T>.checkboxToggle(baseClass: String? = null, content: DropdownCheckboxToggle.() -> Unit = {}) {
    assignToggle(DropdownInternalCheckboxToggle(this, baseClass, job, content))
}

/**
 * Creates an action toggle and returns a [Listener] (basically a [Flow]) in order to combine the button of the action toggle declaration directly to a fitting handler.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the action toggle
 *
 * @sample org.patternfly.sample.DropdownSample.actionToggle
 */
public fun <T> Dropdown<T>.actionToggle(
    baseClass: String? = null,
    content: Button.() -> Unit
): Listener<MouseEvent, HTMLButtonElement> {
    val actionToggle = DropdownActionToggle(this, baseClass, job, content)
    assignToggle(actionToggle)
    return actionToggle.clickEvents!!
}

/**
 * Creates a [DropdownCustomToggle]. Use this function if you want to have full control over the layout of the toggle. You can use one of the `toggleXyz()` functions to add specific content.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the custom toggle
 *
 * @sample org.patternfly.sample.DropdownSample.customToggle
 */
public fun <T> Dropdown<T>.customToggle(
    baseClass: String? = null,
    content: DropdownCustomToggle<T>.() -> Unit
): DropdownCustomToggle<T> {
    val toggle = DropdownCustomToggle(this, baseClass, job).apply(content)
    assignToggle(toggle)
    return toggle
}

/**
 * Creates an container for the image inside a [DropdownCustomToggle] component.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the image container
 */
public fun <T> DropdownCustomToggle<T>.toggleImage(baseClass: String? = null, content: Span.() -> Unit): Span =
    span(baseClass = classes("dropdown".component("toggle", "image"), baseClass), content = content)

/**
 * Creates a container for the text inside a [DropdownCustomToggle] component.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda for setting up the text container
 */
public fun <T> DropdownCustomToggle<T>.toggleText(baseClass: String? = null, content: Span.() -> Unit): Span =
    span(baseClass = classes("dropdown".component("toggle", "text"), baseClass), content = content)

/**
 * Creates a container inside a [DropdownCustomToggle] containing an `icon("caret-down".fas())` icon.
 *
 * @param baseClass optional CSS class that should be applied to the element
 */
public fun <T> DropdownCustomToggle<T>.toggleIcon(baseClass: String? = null): Span =
    span(baseClass = classes("dropdown".component("toggle", "text"), baseClass)) {
        icon("caret-down".fas())
    }

/**
 * Starts a block to add flat dropdown items using the DSL.
 *
 * @sample org.patternfly.sample.DropdownSample.items
 */
public fun <T> Dropdown<T>.items(block: ItemsBuilder<T>.() -> Unit = {}) {
    val entries = ItemsBuilder(store.idProvider, store.itemSelection).apply(block).build()
    store.update(entries)
}

/**
 * Starts a block to add dropdown groups using the DSL.
 *
 * @sample org.patternfly.sample.DropdownSample.groups
 */
public fun <T> Dropdown<T>.groups(block: GroupsBuilder<T>.() -> Unit = {}) {
    if (!grouped) {
        console.warn("Dropdown ${domNode.debug()} has not been created using `grouped = true`")
    }
    val entries = GroupsBuilder(store.idProvider, store.itemSelection).apply(block).build()
    store.update(entries)
}

// ------------------------------------------------------ tag

/**
 * PatternFly [dropdown](https://www.patternfly.org/v4/components/dropdown/design-guidelines) component.
 *
 * A dropdown presents a menu of actions in a constrained space that will trigger a process or navigate to a new location. A dropdown consists of a toggle control to open and close a menu of [entries][Entry].
 *
 * You can choose between different toggle variations:
 * - [text toggle][DropdownTextToggle]
 * - [icon toggle][DropdownIconToggle]
 * - [checkbox toggle][DropdownCheckboxToggle]
 * - [action toggle][DropdownActionToggle]
 * - [custom toggle][DropdownCustomToggle]
 *
 * The data in the menu is managed by a [DropdownStore].
 *
 * **Adding entries**
 *
 * Entries can be added by using the [DropdownStore] or by using the DSL. Items can be grouped. Nested groups are not supported. See the samples below.
 *
 * **Rendering entries**
 *
 * By default the dropdown uses a builtin function to render the [Item]s in the [DropdownStore]. This function takes the [Item.icon] and the [Item.description] into account (if specified). It uses the function passed to [selector] to select a string from [Item.item] which defaults to `{ it.toString() }`.
 *
 * If you don't want to use the builtin defaults you can specify a custom display function by calling [display]. In this case you have full control over the rendering of the data in the dropdown entries.
 *
 * @sample org.patternfly.sample.DropdownSample.dropdownDsl
 * @sample org.patternfly.sample.DropdownSample.dropdownStore
 */
public class Dropdown<T> internal constructor(
    public val store: DropdownStore<T>,
    internal val grouped: Boolean,
    dropdownAlign: Align?,
    up: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>, Div(
    id = id,
    baseClass = classes {
        +ComponentType.Dropdown
        +dropdownAlign?.modifier
        +("top".modifier() `when` up)
        +baseClass
    },
    job
) {

    private var selector: (T) -> String = { it.toString() }
    private var customDisplay: ComponentDisplay<Button, T>? = null
    private var defaultDisplay: ComponentDisplay<Button, Item<T>> = { item ->
        if (item.description != null) {
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

    private var toggle: Toggle<T, Node> = RecordingToggle()
    internal val toggleId: String = Id.unique(ComponentType.Dropdown.id, "tgl")

    /**
     * Manages the **c**ollapse / **e**xpand **s**tate of the [Dropdown]. Use this property if you want to track the collapse / expand state.
     *
     * @sample org.patternfly.sample.DropdownSample.ces
     */
    public val ces: CollapseExpandStore = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("dropdown".component("menu-item")))
    }

    init {
        markAs(ComponentType.Dropdown)
        classMap(ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) })

        val classes = classes {
            +"dropdown".component("menu")
            +dropdownAlign?.modifier
        }
        val tag = if (grouped) {
            div(baseClass = classes) {
                attr("hidden", true)
            }
        } else {
            ul(baseClass = classes) {
                attr("hidden", true)
            }
        }
        with(tag) {
            attr("role", "menu")
            attr("hidden", this@Dropdown.ces.data.map { !it })
            aria["labelledby"] = this@Dropdown.toggleId

            this@Dropdown.store.entries.renderEach { entry ->
                when (entry) {
                    is Item<T> -> {
                        li(content = this@Dropdown.itemContent(entry))
                    }
                    is Group<T> -> {
                        section(baseClass = "dropdown".component("group")) {
                            entry.text?.let {
                                h1(baseClass = "dropdown".component("group", "title")) { +it }
                            }
                            ul {
                                entry.entries.forEach { groupEntry ->
                                    when (groupEntry) {
                                        is Item<T> -> {
                                            li(content = this@Dropdown.itemContent(groupEntry))
                                        }
                                        is Separator<T> -> {
                                            divider(DividerVariant.LI)
                                        }
                                        else -> {
                                            console.warn("Nested groups are not supported for ${domNode.debug()}")
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
    }

    // TODO Support links if item.href != null
    private fun itemContent(item: Item<T>): Li.() -> Unit = {
        attr("role", "menuitem")
        button(
            baseClass = classes {
                +"dropdown".component("menu-item")
                +("icon".modifier() `when` (item.icon != null))
                +("description".modifier() `when` (item.description != null))
                +("disabled".modifier() `when` item.disabled)
            }
        ) {
            attr("tabindex", "-1")
            if (item.disabled) {
                aria["disabled"] = true
                attr("disabled", "true")
            }
            if (item.selected) {
                domNode.autofocus = true
            }
            if (this@Dropdown.customDisplay != null) {
                this@Dropdown.customDisplay?.invoke(this, item.item)
            } else {
                this@Dropdown.defaultDisplay.invoke(this, item)
            }
            clicks handledBy this@Dropdown.ces.collapse
            clicks.map { item } handledBy this@Dropdown.store.selectHandler
        }
    }

    internal fun <N : Node> assignToggle(toggle: Toggle<T, N>) {
        // when switching from the recording to a valid toggle
        // replay the recorded values (if any)
        if (this.toggle is RecordingToggle<T> && toggle !is RecordingToggle<T>) {
            domNode.prepend(toggle.domNode)
            (this.toggle as RecordingToggle<T>).playback(toggle)
            this.toggle = toggle
        } else {
            console.warn(
                "Reassignment of dropdown toggle in ${domNode.debug()} not supported. " +
                    "Toggle has already been assigned to ${this.toggle::class.simpleName}."
            )
        }
    }

    /**
     * Sets the selector which is used by the built in display function to select a string from `T`.
     */
    public fun selector(selector: (T) -> String) {
        this.selector = selector
    }

    /**
     * Sets a custom display function to render the data inside the dropdown entries.
     */
    public fun display(display: ComponentDisplay<Button, T>) {
        this.customDisplay = display
    }

    /**
     * Disables or enables the dropdown toggle.
     */
    public fun disabled(value: Boolean) {
        toggle.disabled(value)
    }

    /**
     * Disables or enables the dropdown toggle based on the values from the flow.
     */
    public fun disabled(value: Flow<Boolean>) {
        toggle.disabled(value)
    }
}

// ------------------------------------------------------ toggle

private fun <T> initToggle(dropdown: Dropdown<T>, tag: Tag<HTMLElement>) {
    with(tag) {
        domNode.id = dropdown.toggleId
        aria["haspopup"] = true
        aria["expanded"] = dropdown.ces.data.map { it.toString() }
        clicks handledBy dropdown.ces.toggle
    }
}

internal class DropdownTextToggle<T>(
    dropdown: Dropdown<T>,
    variations: Array<out ButtonVariation>,
    baseClass: String?,
    job: Job,
    content: Span.() -> Unit
) : Toggle<T, HTMLButtonElement>,
    Button(
        baseClass = classes {
            +"dropdown".component("toggle")
            +variations.joinToString(" ") { it.modifier }
            +baseClass
        },
        job = job
    ) {

    init {
        initToggle(dropdown, this)
        span(baseClass = "dropdown".component("toggle", "text")) {
            content(this)
        }
        span(baseClass = "dropdown".component("toggle", "icon")) {
            icon("caret-down".fas())
        }
    }

    override fun disabled(value: Boolean) {
        disabled(value, trueValue = "")
    }

    override fun disabled(value: Flow<Boolean>) {
        disabled(value, trueValue = "")
    }
}

internal class DropdownIconToggle<T>(
    dropdown: Dropdown<T>,
    baseClass: String?,
    job: Job,
    content: Button.() -> Unit
) : Toggle<T, HTMLButtonElement>,
    Button(
        baseClass = classes {
            +"dropdown".component("toggle")
            +"plain".modifier()
            +baseClass
        },
        job = job
    ) {

    init {
        initToggle(dropdown, this)
        content(this)
    }

    override fun disabled(value: Boolean) {
        disabled(value, trueValue = "")
    }

    override fun disabled(value: Flow<Boolean>) {
        disabled(value, trueValue = "")
    }
}

internal class DropdownInternalCheckboxToggle<T>(
    dropdown: Dropdown<T>,
    baseClass: String?,
    job: Job,
    content: DropdownCheckboxToggle.() -> Unit,
) : Toggle<T, HTMLDivElement>,
    Div(
        baseClass = classes {
            +"dropdown".component("toggle")
            +"split-button".modifier()
            +baseClass
        },
        job = job
    ) {

    private val label: Label
    private val button: Button
    private lateinit var checkbox: Input

    init {
        val inputId = Id.unique(ComponentType.Dropdown.id, "tgl", "chk")
        label = label(baseClass = "dropdown".component("toggle", "check")) {
            `for`(inputId)
            this@DropdownInternalCheckboxToggle.checkbox = input(id = inputId) {
                type("checkbox")
            }
        }
        button = button(baseClass = "dropdown".component("toggle", "button")) {
            icon("caret-down".fas())
        }
        initToggle(dropdown, button)
        DropdownCheckboxToggle(label, checkbox, job).apply(content)
    }

    override fun disabled(value: Boolean) {
        domNode.classList.toggle("disabled".modifier(), value)
        checkbox.disabled(value)
        button.disabled(value)
    }

    override fun disabled(value: Flow<Boolean>) {
        mountSingle(job, value) { v, _ -> disabled(v) }
    }
}

/**
 * Provides access to the [text] and [checkbox] of a checkbox toggle.
 *
 * @sample org.patternfly.sample.DropdownSample.checkboxToggle
 */
public class DropdownCheckboxToggle internal constructor(
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
                ),
                {
                    it.aria["hidden"] = true
                }
            )
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

internal class DropdownActionToggle<T>(
    dropdown: Dropdown<T>,
    baseClass: String?,
    job: Job,
    content: Button.() -> Unit
) : Toggle<T, HTMLDivElement>,
    Div(
        baseClass = classes {
            +"dropdown".component("toggle")
            +"split-button".modifier()
            +"action".modifier()
            +baseClass
        },
        job = job
    ) {

    private val actionButton: Button
    private val toggleButton: Button
    internal var clickEvents: Listener<MouseEvent, HTMLButtonElement>? = null

    init {
        actionButton = button(baseClass = "dropdown".component("toggle", "button")) {
            this@DropdownActionToggle.clickEvents = clicks
            content(this)
        }
        toggleButton = button(baseClass = "dropdown".component("toggle", "button")) {
            icon("caret-down".fas())
        }
        initToggle(dropdown, toggleButton)
    }

    override fun disabled(value: Boolean) {
        domNode.classList.toggle("disabled".modifier(), value)
        actionButton.disabled(value)
        toggleButton.disabled(value)
    }

    override fun disabled(value: Flow<Boolean>) {
        mountSingle(job, value) { v, _ -> disabled(v) }
    }
}

/**
 * Custom toggle of a [Dropdown] component. Allows full control over the content of the toggle. You can use one of the `toggleXyz()` functions to add specific content.
 *
 * @sample org.patternfly.sample.DropdownSample.customToggle
 */
public class DropdownCustomToggle<T>(dropdown: Dropdown<T>, baseClass: String?, job: Job) :
    Toggle<T, HTMLButtonElement>,
    Button(baseClass = classes("dropdown".component("toggle"), baseClass), job = job) {

    init {
        initToggle(dropdown, this)
    }

    override fun disabled(value: Boolean) {
        disabled(value, trueValue = "")
    }

    override fun disabled(value: Flow<Boolean>) {
        disabled(value, trueValue = "")
    }
}

// ------------------------------------------------------ store

/**
 * An [EntriesStore] with [ItemSelection.SINGLE] selection mode.
 */
public class DropdownStore<T>(idProvider: IdProvider<T, String> = { Id.build(it.toString()) }) :
    EntriesStore<T>(idProvider, ItemSelection.SINGLE) {

    internal val selectHandler: EmittingHandler<Item<T>, Item<T>> = handleAndEmit { entries, item ->
        emit(item)
        entries.select(item.item)
    }

    /**
     * Flow for selected items. Shortcut for `singleSelection.filterNotNull()`
     */
    public val selects: Flow<Item<T>> = selectHandler
}
