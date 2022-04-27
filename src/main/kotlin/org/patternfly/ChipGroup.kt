package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.ButtonVariant.plain
import org.patternfly.dom.Id
import org.patternfly.dom.removeFromParent
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ factory

/**
 * @param limit the maximum number of chips shown at once
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.chipGroup(
    limit: Int = 3,
    baseClass: String? = null,
    id: String? = null,
    context: ChipGroup.() -> Unit = {}
) {
    ChipGroup(limit).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [chip group](https://www.patternfly.org/v4/components/chip-group/design-guidelines) component.
 *
 * A chip group contains an optional text, a list of chips and an optional close button.
 *
 * @sample org.patternfly.sample.ChipGroupSample.staticItems
 * @sample org.patternfly.sample.ChipGroupSample.dynamicItems
 */
public open class ChipGroup(private var limit: Int) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithExpandedStore by ExpandedStoreMixin(),
    WithTitle by TitleMixin() {

    private var closable: Boolean = false
    private var storeItems: Boolean = false
    private val itemStore: ChipItemStore = ChipItemStore()
    private val headItems: MutableList<ChipItem> = mutableListOf()
    private val tailItems: MutableList<ChipItem> = mutableListOf()
    private val closeStore: RootStore<MouseEvent> = storeOf(MouseEvent(""))
    private val closeHandler: (Event) -> Unit = ::removeFromParent
    private lateinit var root: Tag<HTMLElement>

    /**
     * [Flow] for the close events of this chip group.
     *
     * @sample org.patternfly.sample.ChipGroupSample.close
     */
    public val closes: Flow<MouseEvent> = closeStore.data.drop(1)

    /**
     * Sets the maximum number of chips shown at once.
     */
    public fun limit(limit: Int) {
        this.limit = limit
    }

    /**
     * Whether this chip group can be closed.
     */
    public fun closable(closable: Boolean) {
        this.closable = closable
    }

    /**
     * Adds a [Chip] to this chip group.
     */
    public fun chip(
        title: String? = null,
        baseClass: String? = null,
        id: String? = null,
        context: Chip.() -> Unit = {}
    ) {
        val items = if (storeItems) tailItems else headItems
        items.add(
            ChipItem(
                staticItem = true,
                title = title,
                baseClass = baseClass,
                id = id ?: Id.unique(ComponentType.ChipGroup.id, ComponentType.Chip.id),
                context = context
            )
        )
    }

    /**
     * Adds the chips from the specified store.
     */
    public fun <T> chips(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: ChipItems.(T) -> ChipItem
    ) {
        chips(values.data, idProvider, display)
    }

    /**
     * Adds the chips from the specified flow.
     */
    public fun <T> chips(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: ChipItems.(T) -> ChipItem
    ) {
        (MainScope() + itemStore.job).launch {
            values.collect { values ->
                itemStore.update(
                    values.map { value ->
                        ChipItems(idProvider(value)).run {
                            display.invoke(this, value)
                        }
                    }
                )
            }
        }
        storeItems = true
    }

    @Suppress("LongMethod", "ComplexMethod")
    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            root = div(
                baseClass = classes {
                    +ComponentType.ChipGroup
                    +("category".modifier() `when` hasTitle)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.ChipGroup)
                applyElement(this)
                applyEvents(this)

                div(baseClass = "chip-group".component("main")) {
                    var labelId: String? = null
                    if (hasTitle) {
                        labelId = Id.unique(ComponentType.ChipGroup.id, "label")
                        span(baseClass = "chip-group".component("label"), id = labelId) {
                            aria["hidden"] = true
                            applyTitle(this)
                        }
                    }
                    ul(
                        baseClass = "chip-group".component("list"),
                        scope = {
                            set(Scopes.CHIP_GROUP, true)
                        }
                    ) {
                        labelId?.let { aria["labelledby"] = it }
                        itemStore.data.map { items ->
                            headItems + items + tailItems
                        }.combine(expandedStore.data) { items, expanded ->
                            Pair(items, expanded)
                        }.render(into = this) { (items, expanded) ->
                            val visibleChips = if (expanded) items else items.take(limit)
                            visibleChips.forEach { item ->
                                li(baseClass = "chip-group".component("list-item")) {
                                    chip(item.title, item.baseClass, item.id) {
                                        item.context(this)
                                        closable(true)
                                        if (item.staticItem) {
                                            events {
                                                closes.map { item.id } handledBy { id ->
                                                    this@ChipGroup.headItems.removeAll { it.id == id }
                                                    this@ChipGroup.tailItems.removeAll { it.id == id }
                                                }
                                            }
                                        } else {
                                            events {
                                                closes.map { item.id } handledBy this@ChipGroup.itemStore.remove
                                            }
                                        }
                                    }
                                }
                            }
                            if (items.size > limit) {
                                li(baseClass = "chip-group".component("list-item")) {
                                    button(baseClass = classes("chip".component(), "overflow".modifier())) {
                                        span(baseClass = "chip".component("text")) {
                                            +(if (expanded) "Shoe less" else "${items.size - limit} more")
                                        }
                                        clicks handledBy expandedStore.toggle
                                    }
                                }
                            } else {
                                // reset to collapsed, so that "... more" is shown next time (items.size > limit)
                                expandedStore.collapse(Unit)
                            }
                        }
                    }
                }
                if (closable) {
                    div(baseClass = "chip-group".component("close")) {
                        pushButton(plain) {
                            icon("times-circle".fas())
                            aria["label"] = "Close chip group"
                            domNode.addEventListener(Events.click.name, this@ChipGroup.closeHandler)
                            clicks.map { it } handledBy this@ChipGroup.closeStore.update
                        }
                    }
                }
            }

            // Remove this chip group, after last chip has been removed
            (MainScope() + itemStore.job).launch {
                itemStore.remove.collect {
                    // The item is emitted before it is removed, so check for size == 1
                    if (itemStore.current.size == 1 && headItems.isEmpty() && tailItems.isEmpty()) {
                        root.domNode.removeFromParent()
                    }
                }
            }
        }
    }

    private fun removeFromParent(event: Event) {
        (event.target as Element).removeEventListener(Events.click.name, closeHandler)
        root.domNode.removeFromParent()
    }
}

// ------------------------------------------------------ item & store

/**
 * DSL scope class to create [ChipItem]s when using [ChipGroup.chips] functions.
 *
 * @sample org.patternfly.sample.ChipGroupSample.dynamicItems
 */
public class ChipItems(internal val id: String) {

    public fun chip(
        title: String? = null,
        baseClass: String? = null,
        id: String? = null,
        context: Chip.() -> Unit = {}
    ): ChipItem = ChipItem(false, title, baseClass, id ?: this.id, context)
}

/**
 * DSL helper class to hold data necessary to create [Chip] components when using [ChipGroup.chips] functions.
 */
public class ChipItem internal constructor(
    internal val staticItem: Boolean,
    internal val title: String?,
    internal val baseClass: String?,
    internal val id: String,
    internal val context: Chip.() -> Unit
)

internal class ChipItemStore : RootStore<List<ChipItem>>(emptyList()) {

    val remove: EmittingHandler<String, String> = handleAndEmit { items, id ->
        items.find { it.id == id }?.let { emit(it.id) }
        items.filterNot { it.id == id }
    }
}
