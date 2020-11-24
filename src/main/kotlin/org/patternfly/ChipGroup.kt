package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.html.Ul
import dev.fritz2.elemento.By
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.hide
import dev.fritz2.elemento.matches
import dev.fritz2.elemento.plusAssign
import dev.fritz2.elemento.querySelector
import dev.fritz2.elemento.querySelectorAll
import dev.fritz2.elemento.removeFromParent
import dev.fritz2.elemento.show
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.dom.clear
import org.patternfly.ButtonVariation.plain
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

/**
 * Creates a [ChipGroup] component with [Chip]s backed by a [ChipGroupStore].
 *
 * @param store the store for the chips
 * @param limit the maximum number of chips to show at once
 * @param closable whether the chip group is closeable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample ChipGroupSamples.chipGroupStore
 */
public fun <T> RenderContext.chipGroup(
    store: ChipGroupStore<T> = ChipGroupStore(),
    limit: Int = 3,
    closable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: ChipGroup<T>.() -> Unit = {}
): ChipGroup<T> = register(ChipGroup(store, limit, closable, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [ChipGroup] component with static [Chip]s.
 *
 * @param limit the maximum number of chips to show at once
 * @param closable whether the chip group is closeable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample ChipGroupSamples.chipGroupStatic
 */
public fun RenderContext.chipGroup(
    limit: Int = 3,
    closable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: ChipGroup<Unit>.() -> Unit = {}
): ChipGroup<Unit> = register(ChipGroup(null, limit, closable, id = id, baseClass = baseClass, job), content)

/**
 * Creates a container for the [Chip]s inside a [ChipGroup].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> ChipGroup<T>.chips(
    id: String? = null,
    baseClass: String? = null,
    content: ChipGroupChips<T>.() -> Unit = {}
): ChipGroupChips<T> =
    register(ChipGroupChips(this, id = id, baseClass = baseClass, job), content).also {
        it.setupToggle()
    }

// ------------------------------------------------------ tag

/**
 * PatternFly [chip group](https://www.patternfly.org/v4/components/chip-group/design-guidelines) component.
 *
 * A chip group contains an optional text, a list of [Chip]s and an optional close button. The chips can be provided using two different ways:
 * 1. Backed by a store: Use the [display] function to specify how the items from the store is turned into a [Chip] component.
 * 1. Using static chips: Use the DSL to specify the [Chip]s statically.
 *
 * @param T the type which is used for the single [Chip]s (matters only if a store is used)
 *
 * @sample ChipGroupSamples.chipGroupStore
 * @sample ChipGroupSamples.chipGroupStatic
 */
public class ChipGroup<T> internal constructor(
    private val store: ChipGroupStore<T>?,
    internal val limit: Int,
    closable: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>,
    WithTextDelegate<HTMLDivElement, HTMLSpanElement>,
    Div(id = id, baseClass = classes {
        +ComponentType.ChipGroup
        +baseClass
    }, job) {

    private val chips: ChipGroupChips<T>
    private val expanded = CollapseExpandStore()
    private var textElement: Span? = null
    private var closeButton: PushButton? = null

    /**
     * Listener for the close button (if any).
     *
     * @sample ChipGroupSamples.closes
     */
    public val closes: Listener<MouseEvent, HTMLButtonElement> by lazy { subscribe(closeButton, Events.click) }

    init {
        markAs(ComponentType.ChipGroup)
        chips = chips()
        if (closable) {
            div(baseClass = "chip-group".component("close")) {
                this@ChipGroup.closeButton = pushButton(plain) {
                    icon("times-circle".fas())
                    aria["label"] = "Close chip group"
                    domNode.addEventListener(Events.click.name, this@ChipGroup::close)
                }
            }
        }
    }

    /**
     * Defines how to display the [Chip]s backed by the [ChipGroupStore]. Please call this function *before* populating the store.
     */
    public fun display(display: (T) -> Chip) {
        with(chips) {
            if (this@ChipGroup.store != null) {
                this@ChipGroup.store.data
                    .combine(this@ChipGroup.expanded.data) { items, expanded -> Pair(items, expanded) }
                    .onEach { (items, expanded) ->
                        domNode.clear()
                        val visibleItems = if (expanded) items else items.take(this@ChipGroup.limit)
                        visibleItems.forEach { item ->
                            li(baseClass = "chip-group".component("list-item")) {
                                val chip = display(item)
                                register(chip) {
                                    val chipId = this@ChipGroup.store.identifier(item)
                                    it.closes.map { chipId } handledBy this@ChipGroup.store.remove
                                }
                            }
                        }
                        if (items.size > this@ChipGroup.limit) {
                            li(baseClass = "chip-group".component("list-item")) {
                                button(baseClass = classes("chip".component(), "overflow".modifier())) {
                                    span(baseClass = "chip".component("text")) {
                                        +(if (expanded) "Shoe less" else "${items.size - this@ChipGroup.limit} more")
                                    }
                                    clicks handledBy this@ChipGroup.expanded.toggle
                                }
                            }
                        } else {
                            // reset to collapsed, so that "... more" is shown next time (items.size > limit)
                            this@ChipGroup.expanded.collapse(Unit)
                        }
                    }.launchIn(MainScope() + job)

                (MainScope() + job).launch {
                    this@ChipGroup.store.remove.distinctUntilChanged().collect { size ->
                        if (size == 0) {
                            domNode.removeFromParent()
                        }
                    }
                }
            }
        }
    }

    override fun delegate(): HTMLSpanElement {
        if (textElement == null) {
            val textId = Id.unique("cgl")
            textElement = Span(id = textId, baseClass = "chip-group".component("label"), job).apply {
                aria["hidden"] = true
            }
            with(domNode) {
                classList += "category".modifier()
                prepend(textElement!!.domNode)
                querySelector(By.classname("chip-group".component("list")))?.let {
                    it.aria["labelledby"] = textId
                }
                querySelector(
                    By.classname("chip-group".component("close"))
                        .child(By.classname(ComponentType.Button.baseClass!!))
                )?.let {
                    it.aria["labelledby"] = textId
                }
            }
        }
        return textElement!!.domNode
    }

    private fun close(ignore: Event) {
        closeButton?.domNode?.removeEventListener(Events.click.name, ::close)
        domNode.removeFromParent()
    }
}

/**
 * Container for the [Chip]s inside a [ChipGroup].
 */
public class ChipGroupChips<T>(private val chipGroup: ChipGroup<T>, id: String?, baseClass: String?, job: Job) :
    Ul(id = id, baseClass = classes("chip-group".component("list"), baseClass), job) {

    private var toggleElement: Element? = null
    private var toggleText: Element? = null
    private var expanded: Boolean = false
    private val chipSelector = By.classname("chip-group".component("list-item"))
        .child(By.classname(ComponentType.Chip.baseClass!!))

    init {
        attr("role", "list")
    }

    internal fun setupToggle() {
        if (chips() > chipGroup.limit) {
            toggleElement = li(baseClass = "chip-group".component("list-item")) {
                button(baseClass = classes("chip".component(), "overflow".modifier())) {
                    domNode.onclick = { this@ChipGroupChips.toggle() }
                    this@ChipGroupChips.toggleText = span(baseClass = "chip".component("text")) {}.domNode
                }
            }.domNode
            collapse()
        }
    }

    internal fun toggle() {
        if (expanded) collapse() else expand()
    }

    private fun collapse() {
        domNode.children.asList().forEachIndexed { index, element ->
            if (index >= chipGroup.limit && element.matches(chipSelector)) {
                element.hide()
            }
            toggleText?.textContent = "${chips() - chipGroup.limit} more"
        }
    }

    private fun expand() {
        domNode.children.asList().forEach {
            it.show()
        }
        toggleText?.textContent = "Shoe less"
        expanded = true
    }

    private fun chips(): Int = domNode.querySelectorAll(chipSelector).asList().size
}

// ------------------------------------------------------ store

/**
 * Store containing a list of items used by the [ChipGroup] component. Each item is identified by the specified [IdProvider].
 */
public class ChipGroupStore<T>(internal val identifier: IdProvider<T, String> = { Id.build(it.toString()) }) :
    RootStore<List<T>>(listOf()) {

    /**
     * Adds the specified item to the list of items.
     */
    public val add: SimpleHandler<T> = handle { items, item -> items + item }

    /**
     * Adds all specified items to the list of items.
     */
    public val addAll: SimpleHandler<List<T>> = handle { items, newItems -> items + newItems }

    /**
     * Removes the specified item from the list of items and emits the new size of the remaining items.
     */
    public val remove: EmittingHandler<String, Int> = handleAndEmit { items, id ->
        val removed = items.filterNot { identifier(it) == id }
        emit(removed.size)
        removed
    }
}
