package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.dom.clear
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.plusAssign
import org.patternfly.dom.querySelector
import org.patternfly.dom.removeFromParent
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

/**
 * Creates a [ChipGroup] component.
 *
 * @param store the store for the chips
 * @param limit the maximum number of chips to show at once
 * @param closable whether the chip group is closeable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
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
 * Adds the specified items to the [ChipGroupStore]. The items are displayed according to the [ChipGroup.display] function.
 *
 * @sample org.patternfly.sample.ChipGroupSample.vararg
 */
public fun <T> ChipGroup<T>.chips(vararg chips: T) {
    store.addAll(chips.asList())
}

/**
 * Adds the specified items to the [ChipGroupStore]. The items are displayed according to the [ChipGroup.display] function.
 *
 * @sample org.patternfly.sample.ChipGroupSample.list
 */
public fun <T> ChipGroup<T>.chips(chips: List<T>) {
    store.addAll(chips)
}

/**
 * Adds the specified items to the [ChipGroupStore]. The items are displayed according to the [ChipGroup.display] function.
 *
 * @sample org.patternfly.sample.ChipGroupSample.builder
 */
public fun <T> ChipGroup<T>.chips(block: ChipsBuilder<T>.() -> Unit) {
    store.addAll(ChipsBuilder<T>().apply(block).chips)
}

/**
 * Builder for adding items to the [ChipGroupStore].
 *
 * @sample org.patternfly.sample.ChipGroupSample.builder
 */
public class ChipsBuilder<T> {
    internal val chips: MutableList<T> = mutableListOf()

    /**
     * Adds one item to this builder using the unary plus operator.
     */
    public operator fun T.unaryPlus() {
        chips.add(this)
    }

    /**
     * Adds all items in the specified list to this builder using the unary plus operator.
     */
    public operator fun List<T>.unaryPlus() {
        chips.addAll(this)
    }

    /**
     * Adds one item to this builder.
     */
    public fun add(chip: T) {
        chips.add(chip)
    }

    /**
     * Adds all items in the specified list to this builder.
     */
    public fun addAll(chips: List<T>) {
        this.chips.addAll(chips)
    }
}

// ------------------------------------------------------ tag

/**
 * PatternFly [chip group](https://www.patternfly.org/v4/components/chip-group/design-guidelines) component.
 *
 * A chip group contains an optional text, a list of chips and an optional close button. The data for the chips is managed by a [ChipGroupStore] and rendered by the [display] function which defaults to
 *
 * `{ chip { +it.toString() } }`
 *
 * The items can be added to the [ChipGroupStore] using different ways (see samples).
 *
 * @param T the type which is used for the single [Chip]s
 *
 * @sample org.patternfly.sample.ChipGroupSample.vararg
 * @sample org.patternfly.sample.ChipGroupSample.list
 * @sample org.patternfly.sample.ChipGroupSample.builder
 * @sample org.patternfly.sample.ChipGroupSample.store
 */
public class ChipGroup<T> internal constructor(
    public val store: ChipGroupStore<T>,
    private val limit: Int,
    closable: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>,
    WithTextDelegate<HTMLDivElement, HTMLSpanElement>,
    WithIdProvider<T> by store,
    Div(id = id, baseClass = classes {
        +ComponentType.ChipGroup
        +baseClass
    }, job) {

    private val expanded = CollapseExpandStore()
    private var display: (T) -> Chip = { chip { +it.toString() } }
    private var textElement: Span? = null
    private var closeButton: PushButton? = null

    /**
     * Listener for the close button (if any).
     *
     * @sample org.patternfly.sample.ChipGroupSample.closes
     */
    public val closes: Listener<MouseEvent, HTMLButtonElement> by lazy { subscribe(closeButton, Events.click) }

    init {
        markAs(ComponentType.ChipGroup)
        ul(baseClass = classes("chip-group".component("list"))) {
            this@ChipGroup.store.data
                .combine(this@ChipGroup.expanded.data) { items, expanded -> Pair(items, expanded) }
                .onEach { (items, expanded) ->
                    domNode.clear()
                    val visibleItems = if (expanded) items else items.take(this@ChipGroup.limit)
                    visibleItems.forEach { item ->
                        li(baseClass = "chip-group".component("list-item")) {
                            val chip = this@ChipGroup.display.invoke(item)
                            register(chip) {
                                val chipId = this@ChipGroup.itemId(item)
                                it.closes.map { chipId } handledBy this@ChipGroup.store.removeHandler
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
        }

        if (closable) {
            div(baseClass = "chip-group".component("close")) {
                this@ChipGroup.closeButton = pushButton(plain) {
                    icon("times-circle".fas())
                    aria["label"] = "Close chip group"
                    domNode.addEventListener(Events.click.name, this@ChipGroup::close)
                }
            }
        }

        (MainScope() + job).launch {
            this@ChipGroup.store.removes.collect {
                // The item is emitted before it is removed, so check for size == 1
                if (this@ChipGroup.store.current.size == 1) {
                    domNode.removeFromParent()
                }
            }
        }
    }

    /**
     * Defines how to display the items in the [ChipGroupStore].
     *
     * Defaults to `{ chip { +it.toString() } }`.
     *
     * @sample org.patternfly.sample.ChipGroupSample.display
     */
    public fun display(display: (T) -> Chip) {
        this.display = display
    }

    override fun delegate(): HTMLSpanElement {
        if (textElement == null) {
            val textId = Id.unique("cgt")
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

    private fun close(@Suppress("UNUSED_PARAMETER") event: Event) {
        closeButton?.domNode?.removeEventListener(Events.click.name, ::close)
        domNode.removeFromParent()
    }
}

// ------------------------------------------------------ store

/**
 * Store containing a list of items used by the [ChipGroup] component. Each item is identified by the specified [IdProvider] which defaults to
 *
 * `{ Id.build(it.toString()) }`
 *
 * @sample org.patternfly.sample.ChipGroupSample.store
 */
public class ChipGroupStore<T>(override val idProvider: IdProvider<T, String> = { Id.build(it.toString()) }) :
    WithIdProvider<T>,
    RootStore<List<T>>(listOf()) {

    internal val removeHandler: EmittingHandler<String, T> = handleAndEmit { items, id ->
        items.find { idProvider(it) == id }?.let { emit(it) }
        items.filterNot { idProvider(it) == id }
    }

    /**
     * Flow containing the removed items.
     *
     * @sample org.patternfly.sample.ChipGroupSample.remove
     */
    public val removes: Flow<T> = removeHandler

    /**
     * Adds the specified item to the list of items.
     */
    public val add: Handler<T> = handle { items, item -> items + item }

    /**
     * Adds all specified items to the list of items.
     */
    public val addAll: Handler<List<T>> = handle { items, newItems -> items + newItems }
}
