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
 * @param limit the maximum number of labels shown at once
 * @param vertical whether to render the labels vertically
 * @param compact whether to use compact style
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.labelGroup(
    limit: Int = 3,
    vertical: Boolean = false,
    compact: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: LabelGroup.() -> Unit = {}
) {
    LabelGroup(limit, vertical, compact).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [label group](https://www.patternfly.org/v4/components/label-group/design-guidelines) component.
 *
 * Use a label group when you have multiple [label]s to display at once. Label groups can be oriented either horizontally or vertically and can optionally be named and dismissable.
 *
 * @sample org.patternfly.sample.LabelGroupSample.staticItems
 * @sample org.patternfly.sample.LabelGroupSample.dynamicItems
 */
public open class LabelGroup(private var limit: Int, private val vertical: Boolean, private val compact: Boolean) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithExpandedStore by ExpandedStoreMixin(),
    WithTitle by TitleMixin() {

    private var closable: Boolean = false
    private var itemsInStore: Boolean = false
    private val itemStore: LabelItemStore = LabelItemStore()
    private val headItems: MutableList<LabelItem> = mutableListOf()
    private val tailItems: MutableList<LabelItem> = mutableListOf()
    private val closeStore: RootStore<MouseEvent> = storeOf(MouseEvent(""))
    private val closeHandler: (Event) -> Unit = ::removeFromParent
    private lateinit var root: Tag<HTMLElement>

    /**
     * [Flow] for the close events of this label group.
     *
     * @sample org.patternfly.sample.LabelGroupSample.close
     */
    public val closes: Flow<MouseEvent> = closeStore.data.drop(1)

    /**
     * Sets the maximum number of labels shown at once.
     */
    public fun limit(limit: Int) {
        this.limit = limit
    }

    /**
     * Whether this label group can be closed.
     */
    public fun closable(closable: Boolean) {
        this.closable = closable
    }

    /**
     * Adds a [Label] to this label group.
     */
    public fun label(
        color: Color,
        title: String? = null,
        outline: Boolean = false,
        baseClass: String? = null,
        id: String? = null,
        context: Label.() -> Unit = {}
    ) {
        val items = if (itemsInStore) tailItems else headItems
        items.add(
            LabelItem(
                staticItem = true,
                color = color,
                title = title,
                outline = outline,
                compact = compact,
                baseClass = baseClass,
                id = id ?: Id.unique(ComponentType.LabelGroup.id, ComponentType.Label.id),
                context = context
            )
        )
    }

    /**
     * Adds the labels from the specified store.
     */
    public fun <T> labels(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: LabelItems.(T) -> LabelItem
    ) {
        labels(values.data, idProvider, display)
    }

    /**
     * Adds the labels from the specified flow.
     */
    public fun <T> labels(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: LabelItems.(T) -> LabelItem
    ) {
        (MainScope() + itemStore.job).launch {
            values.collect { values ->
                itemStore.update(
                    values.map { value ->
                        LabelItems(idProvider(value)).run {
                            display.invoke(this, value)
                        }
                    }
                )
            }
        }
        itemsInStore = true
    }

    @Suppress("LongMethod", "ComplexMethod")
    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            root = div(
                baseClass = classes {
                    +ComponentType.LabelGroup
                    +("category".modifier() `when` hasTitle)
                    +("vertical".modifier() `when` vertical)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.LabelGroup)
                applyElement(this)
                applyEvents(this)

                div(baseClass = "label-group".component("main")) {
                    var labelId: String? = null
                    if (hasTitle) {
                        labelId = Id.unique(ComponentType.LabelGroup.id, "label")
                        span(baseClass = "label-group".component("label"), id = labelId) {
                            aria["hidden"] = true
                            applyTitle(this)
                        }
                    }
                    ul(
                        baseClass = "label-group".component("list"),
                        scope = {
                            set(Scopes.LABEL_GROUP, true)
                        }
                    ) {
                        labelId?.let { aria["labelledby"] = it }
                        itemStore.data.map { items ->
                            headItems + items + tailItems
                        }.combine(expandedStore.data) { items, expanded ->
                            Pair(items, expanded)
                        }.render(into = this) { (items, expanded) ->
                            val visibleLabels = if (expanded) items else items.take(limit)
                            visibleLabels.forEach { item ->
                                li(baseClass = "label-group".component("list-item")) {
                                    label(
                                        color = item.color,
                                        title = item.title,
                                        outline = item.outline,
                                        compact = item.compact,
                                        baseClass = item.baseClass,
                                        id = item.id
                                    ) {
                                        item.context(this)
                                        if (this.closable) {
                                            if (item.staticItem) {
                                                events {
                                                    closes.map { item.id } handledBy { id ->
                                                        this@LabelGroup.headItems.removeAll { it.id == id }
                                                        this@LabelGroup.tailItems.removeAll { it.id == id }
                                                    }
                                                }
                                            } else {
                                                events {
                                                    closes.map { item.id } handledBy this@LabelGroup.itemStore.remove
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (items.size > limit) {
                                li(baseClass = "label-group".component("list-item")) {
                                    button(baseClass = classes {
                                        +"label".component()
                                        +"overflow".modifier()
                                        +("compact".modifier() `when` compact)
                                    }) {
                                        span(baseClass = "label".component("content")) {
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
                    div(baseClass = "label-group".component("close")) {
                        pushButton(plain) {
                            icon("times-circle".fas())
                            aria["label"] = "Close label group"
                            domNode.addEventListener(Events.click.name, this@LabelGroup.closeHandler)
                            clicks.map { it } handledBy this@LabelGroup.closeStore.update
                        }
                    }
                }
            }

            // Remove this label group, after last label has been removed
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
 * DSL scope class to create [LabelItem]s when using [LabelGroup.labels] functions.
 *
 * @sample org.patternfly.sample.LabelGroupSample.dynamicItems
 */
public class LabelItems(internal val id: String) {

    public fun label(
        color: Color,
        title: String? = null,
        outline: Boolean = false,
        compact: Boolean = false,
        baseClass: String? = null,
        id: String? = null,
        context: Label.() -> Unit = {}
    ): LabelItem = LabelItem(false, color, title, outline, compact, baseClass, id ?: this.id, context)
}

/**
 * DSL helper class to hold data necessary to create [Label] components when using [LabelGroup.labels] functions.
 */
@Suppress("LongParameterList")
public class LabelItem internal constructor(
    internal val staticItem: Boolean,
    internal val color: Color,
    internal val title: String?,
    internal val outline: Boolean,
    internal val compact: Boolean,
    internal val baseClass: String?,
    internal val id: String,
    internal val context: Label.() -> Unit
)

internal class LabelItemStore : RootStore<List<LabelItem>>(emptyList()) {

    val remove: EmittingHandler<String, String> = handleAndEmit { items, id ->
        items.find { it.id == id }?.let { emit(it.id) }
        items.filterNot { it.id == id }
    }
}
