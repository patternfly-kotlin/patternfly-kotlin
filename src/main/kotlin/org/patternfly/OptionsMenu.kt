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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.debug
import org.patternfly.dom.matches
import org.patternfly.dom.minusAssign
import org.patternfly.dom.plusAssign
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

// TODO Refactor and document me
// ------------------------------------------------------ dsl

/**
 * Creates a [OptionsMenu] component.
 *
 * @param store the store for the options menu
 * @param grouped whether the options menu contains groups or just flat items
 * @param multiSelect whether multiple entries can be selected
 * @param align the alignment of the options menu
 * @param up controls the direction of the options menu
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.optionsMenu(
    store: OptionsMenuStore<T> = OptionsMenuStore(),
    grouped: Boolean = false,
    multiSelect: Boolean = false,
    align: Align? = null,
    up: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, grouped, multiSelect, align, up, id = id, baseClass = baseClass, job), content)

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

public fun <T> OptionsMenu<T>.items(block: ItemsBuilder<T>.() -> Unit = {}) {
    store.update(ItemsBuilder<T>().apply(block).build())
}

public fun <T> OptionsMenu<T>.groups(block: GroupsBuilder<T>.() -> Unit = {}) {
    if (!grouped) {
        console.warn("Options menu ${domNode.debug()} has not been created using `grouped = true`")
    }
    store.update(GroupsBuilder<T>().apply(block).build())
}

// ------------------------------------------------------ tag

public class OptionsMenu<T> internal constructor(
    public val store: OptionsMenuStore<T>,
    internal val grouped: Boolean,
    multiSelect: Boolean,
    optionsMenuAlign: Align?,
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

    private var selector: (T) -> String = { it.toString() }
    private var customDisplay: ComponentDisplay<Button, T>? = null
    private var defaultDisplay: ComponentDisplay<Button, Item<T>> = { item ->
        +this@OptionsMenu.selector(item.item)
    }

    private var toggle: Toggle<T, Node> = RecordingToggle()
    internal val toggleId: String = Id.unique(ComponentType.OptionsMenu.id, "tgl")

    /**
     * Manages the **c**ollapse / **e**xpand **s**tate of the [OptionsMenu]. Use this property if you want to track the collapse / expand state.
     *
     * @sample org.patternfly.sample.OptionsMenuSample.ces
     */
    public val ces: CollapseExpandStore = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("options-menu".component("menu-item")))    }

    init {
        store.multiSelect = multiSelect
        markAs(ComponentType.OptionsMenu)
        classMap(ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) })

        val classes = classes {
            +"options-menu".component("menu")
            +optionsMenuAlign?.modifier
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
        with (tag) {
            attr("role", "menu")
            attr("hidden", this@OptionsMenu.ces.data.map { !it })
            aria["labelledby"] = this@OptionsMenu.toggleId

            this@OptionsMenu.store.data.renderEach { entry ->
                when (entry) {
                    is Item<T> -> {
                        li(content = this@OptionsMenu.itemContent(entry))
                    }
                    is Group<T> -> {
                        section(baseClass = "options-menu".component("group")) {
                            entry.text?.let {
                                h1(baseClass = "options-menu".component("group", "title")) { +it }
                            }
                            ul {
                                entry.items.forEach { groupEntry ->
                                    when (groupEntry) {
                                        is Item<T> -> {
                                            li(content = this@OptionsMenu.itemContent(groupEntry))
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

    private fun itemContent(item: Item<T>): Li.() -> Unit = {
        attr("role", "menuitem")
        button(baseClass = classes {
            +"options-menu".component("menu-item")
            +("disabled".modifier() `when` item.disabled)
        }) {
            attr("tabindex", "-1")
            if (item.disabled) {
                aria["disabled"] = true
                attr("disabled", "true")
            }
            if (this@OptionsMenu.customDisplay != null) {
                this@OptionsMenu.customDisplay?.invoke(this, item.item)
            } else {
                this@OptionsMenu.defaultDisplay.invoke(this, item)
            }
            if (item.selected) {
                span(baseClass = "options-menu".component("menu-item", "icon")) {
                    icon("check".fas())
                }
            }

            clicks handledBy this@OptionsMenu.ces.collapse
//            clicks.map { item } handledBy this@OptionsMenu.store.selectItemHandler
//            clicks.map { item } handledBy this@OptionsMenuEntries.optionsMenu.store.select
//            clicks.map { item.item } handledBy this@OptionsMenu.store.selectHandler
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
            console.warn("Reassignment of options menu toggle in ${domNode.debug()} not supported. Toggle has already been assigned to ${this.toggle::class.simpleName}.")
        }
    }

    /**
     * Sets the selector which is used by the built in display function to select a string from `T`.
     */
    public fun selector(selector: (T) -> String) {
        this.selector = selector
    }

    /**
     * Sets a custom display function to render the data inside the options menu.
     */
    public fun display(display: ComponentDisplay<Button, T>) {
        this.customDisplay = display
    }

    /**
     * Disables or enables the options menu toggle.
     */
    public fun disabled(value: Boolean) {
        toggle.disabled(value)
    }

    /**
     * Disables or enables the options menu toggle based on the values from the flow.
     */
    public fun disabled(value: Flow<Boolean>) {
        toggle.disabled(value)
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
//        optionsMenu.toggle = this
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

// ------------------------------------------------------ store

public class OptionsMenuStore<T> : RootStore<List<Entry<T>>>(listOf()) {

    internal var multiSelect: Boolean = false
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
