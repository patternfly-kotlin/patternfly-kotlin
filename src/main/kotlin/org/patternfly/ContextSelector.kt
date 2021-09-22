package org.patternfly

import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.values
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.control
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.matches
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

/**
 * Creates a [ContextSelector] component.
 *
 * @param store the store for the context selector
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.contextSelector(
    store: ContextSelectorStore<T> = ContextSelectorStore(),
    id: String? = null,
    baseClass: String? = null,
    content: ContextSelector<T>.() -> Unit = {}
): ContextSelector<T> = register(
    ContextSelector(
        store,
        screenReaderLabel = null,
        id = id,
        baseClass = baseClass,
        job
    ),
    content
)

/**
 * Starts a block to add items using the DSL.
 *
 * @param block code block for adding the items.
 *
 * @sample org.patternfly.sample.ContextSelectorSample.items
 */
public fun <T> ContextSelector<T>.items(block: ItemsBuilder<T>.() -> Unit = {}) {
    val entries = ItemsBuilder(store.idProvider, store.itemSelection).apply(block).build()
    store.update(entries)
}

// ------------------------------------------------------ tag

/**
 * PatternFly [context selector](https://www.patternfly.org/v4/components/context-selector/design-guidelines) component.
 *
 * A context selector can be used in addition to global navigation when the data or resources you show in the interface need to change depending on the user’s context. A context selector consists of a toggle control to open and close a menu of [entries][Entry].
 *
 * The data in the menu is managed by a [ContextSelectorStore] and is wrapped inside instances of [Item].
 *
 * ### Adding entries
 *
 * Entries can be added by using the [ContextSelectorStore] or by using the DSL. Items can be grouped. Nested groups are not supported.
 *
 * ### Rendering entries
 *
 * By default the options menu uses a builtin function to render the [Item]s in the [ContextSelectorStore]. This function takes the [Item.text] into account (if specified). If [Item.text] is `null`, the builtin function falls back to `Item.item.toString()`.
 *
 * If you don't want to use the builtin defaults you can specify a custom display function by calling [display]. In this case you have full control over the rendering of the data in the options menu entries.
 *
 * @sample org.patternfly.sample.ContextSelectorSample.contextSelectorDsl
 * @sample org.patternfly.sample.ContextSelectorSample.contextSelectorStore
 */
public class ContextSelector<T> internal constructor(
    public val store: ContextSelectorStore<T>,
    screenReaderLabel: String?,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyElement<HTMLDivElement>, Div(
    id = id,
    baseClass = classes(ComponentType.ContextSelector, baseClass),
    job,
    scope = Scope()
) {

    private var asString: (T) -> String = { item -> item.toString() }
    private var display: ComponentDisplay<A, T> = { item -> +this@ContextSelector.asString(item) }
    private var filter: (String, T) -> Boolean = { filter, item ->
        this@ContextSelector.asString(item).toLowerCase().contains(filter.toLowerCase())
    }
    private var selected: (T) -> String = asString
    private var toggle: Button

    /**
     * Manages the expanded state of the [ContextSelector]. Use this property if you want to track the collapse / expand state.
     *
     * @sample org.patternfly.sample.ContextSelectorSample.expanded
     */
    public val expanded: ExpandedStore = ExpandedStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("context-selector".component("menu-list-item")))
    }

    init {
        markAs(ComponentType.ContextSelector)

        val toggleId = Id.unique(ComponentType.ContextSelector.id, "tgl")
        val searchInputId = Id.unique(ComponentType.ContextSelector.id, "s", "in")
        val searchButtonId = Id.unique(ComponentType.ContextSelector.id, "s", "btn")

        if (screenReaderLabel != null) {
            span(id = Id.unique(ComponentType.ContextSelector.id, "sr")) {
                attr("hidden", true)
                +screenReaderLabel
            }
        }
        toggle = button(id = toggleId, baseClass = "context-selector".component("toggle")) {
            aria["expanded"] = this@ContextSelector.expanded.data.map { it.toString() }
            aria["labelledby"] = buildString {
                append(toggleId)
                if (screenReaderLabel != null) {
                    append(" ")
                    append(screenReaderLabel)
                }
            }
            clicks handledBy this@ContextSelector.expanded.toggle
            span(baseClass = "context-selector".component("toggle", "text")) {
                this@ContextSelector.store.singleSelection.map {
                    this@ContextSelector.selected.invoke(it.item)
                }.asText()
            }
            span(baseClass = "context-selector".component("toggle", "icon")) {
                icon("caret-down".fas())
            }
        }
        div(baseClass = "context-selector".component("menu")) {
            attr("hidden", this@ContextSelector.expanded.data.map { !it })
            div(baseClass = "context-selector".component("menu", "search")) {
                inputGroup {
                    inputFormControl(id = searchInputId) {
                        type("search")
                        placeholder("Search")
                        aria["labeledby"] = searchButtonId
                        changes.values()
                            .filter { it.isEmpty() }
                            .map { }
                            .handledBy(this@ContextSelector.store.removeFilter)
                        changes.values()
                            .filter { it.isNotEmpty() }
                            .map { { item: T -> this@ContextSelector.filter(it, item) } }
                            .handledBy(this@ContextSelector.store.addFilter)
                    }
                    pushButton(control, id = searchButtonId) {
                        aria["label"] = "Search menu items"
                        icon("search".fas())
                    }
                }
            }
            ul(baseClass = "context-selector".component("menu", "list")) {
                attr("role", "menu")
                // only items are supported for context selector!
                this@ContextSelector.store.data.map { it.items }.renderEach { item ->
                    li {
                        a(
                            baseClass = classes {
                                +"context-selector".component("menu", "list", "item")
                                +("disabled".modifier() `when` item.disabled)
                            }
                        ) {
                            if (item.disabled) {
                                aria["disabled"] = true
                                attr("tabindex", "-1")
                            }
                            this@ContextSelector.display(this, item.item)
                            clicks handledBy this@ContextSelector.expanded.collapse
                            clicks.map { item.unwrap() } handledBy this@ContextSelector.store.handleSelection
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets a custom display function to render the data inside the context selector menu.
     */
    public fun display(display: ComponentDisplay<A, T>) {
        this.display = display
    }

    /**
     * Sets the display function for the selected item.
     */
    public fun selected(selected: (T) -> String) {
        this.selected = selected
    }

    /**
     * Sets a filter function for the search input field.
     */
    public fun filter(filter: (String, T) -> Boolean) {
        this.filter = filter
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

// ------------------------------------------------------ store

/**
 * An [EntriesStore] with [ItemSelection.SINGLE] selection mode.
 */
public class ContextSelectorStore<T>(idProvider: IdProvider<T, String> = { Id.build(it.toString()) }) :
    EntriesStore<T>(idProvider, ItemSelection.SINGLE)
