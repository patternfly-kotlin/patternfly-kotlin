package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.handledBy
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

/**
 * Creates an [Accordion] component.
 *
 * @param singleSelect whether only one item can be selected at a time
 * @param compact whether the AccordionItems use a fixed height
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.toggleGroup(
    singleSelect: Boolean = false,
    compact: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: ToggleGroup.() -> Unit = {}
) {
    ToggleGroup(singleSelect, compact).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [toggle group](https://www.patternfly.org/v4/components/toggle-group/design-guidelines) component.
 *
 * A toggle group is a group of controls that can be used to quickly switch between actions or states.
 */
public open class ToggleGroup(
    private var singleSelect: Boolean,
    private var compact: Boolean,
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var itemsInStore: Boolean = false
    private val itemStore: ToggleGroupItemStore = ToggleGroupItemStore()
    private val headItems: MutableList<ToggleGroupItem> = mutableListOf()
    private val tailItems: MutableList<ToggleGroupItem> = mutableListOf()
    private val singleIdSelection: RootStore<String?> = storeOf(null)
    private val multiIdSelection: ToggleGroupMultiSelectionStore = ToggleGroupMultiSelectionStore()
    private val disabledIds = object : RootStore<List<String>>(listOf()) {
        val disable: Handler<String> = handle { ids, id -> ids + id }
        val enable: Handler<String> = handle { ids, id -> ids - id }
    }

    /**
     * Adds a [ToggleGroupItem].
     */
    public fun item(
        id: String = Id.unique(ComponentType.ToggleGroup.id, "itm"),
        title: String? = null,
        context: StaticToggleGroupItem.() -> Unit = {}
    ) {
        val item = StaticToggleGroupItem(id, title).apply(context)
        (if (itemsInStore) tailItems else headItems).add(item)

        item.selectedFlag?.let { selected ->
            if (selected) {
                if (singleSelect) {
                    singleIdSelection.update(id)
                } else {
                    multiIdSelection.select(id to true)
                }
            }
        }
        item.disabledFlag?.let { disabled ->
            if (disabled) {
                disabledIds.disable(id)
            }
        }
        item.selectedFlow?.let { selected ->
            // setup one-way selection data binding: flow -> id
            if (singleSelect) {
                selected.filter { it }.map { id } handledBy singleIdSelection.update
                selected.filter { !it }.map { null } handledBy singleIdSelection.update
            } else {
                selected.map { id to it } handledBy multiIdSelection.select
            }
        }
        item.disabledFlow?.let { disabled ->
            // setup disabled one-way disabled data binding: flow -> id
            disabled.filter { it }.map { id } handledBy disabledIds.disable
            disabled.filter { !it }.map { id } handledBy disabledIds.enable
        }
    }

    /**
     * Adds the items from the specified store.
     */
    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        singleSelection: Store<T?> = storeOf(null),
        multiSelection: Store<List<T>> = storeOf(listOf()),
        disabled: Store<List<T>> = storeOf(listOf()),
        display: ToggleGroupItemScope.(T) -> ToggleGroupItem
    ) {
        storeItems(
            values = values.data,
            idProvider = idProvider,
            singleSelection = singleSelection,
            multiSelection = multiSelection,
            disabled = disabled,
            display = display
        )
    }

    /**
     * Adds the items from the specified flow.
     */
    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        singleSelection: Store<T?> = storeOf(null),
        multiSelection: Store<List<T>> = storeOf(listOf()),
        disabled: Store<List<T>> = storeOf(listOf()),
        display: ToggleGroupItemScope.(T) -> ToggleGroupItem
    ) {
        storeItems(
            values = values,
            idProvider = idProvider,
            singleSelection = singleSelection,
            multiSelection = multiSelection,
            disabled = disabled,
            display = display
        )
    }

    @Suppress("LongParameterList")
    private fun <T> storeItems(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String>,
        singleSelection: Store<T?>,
        multiSelection: Store<List<T>>,
        disabled: Store<List<T>>,
        display: ToggleGroupItemScope.(T) -> ToggleGroupItem
    ) {
        (MainScope() + itemStore.job).launch {
            values.collect { values ->
                val idToData = values.associateBy { idProvider(it) }
                itemStore.update(
                    values.map { value ->
                        ToggleGroupItemScope(idProvider(value)).run {
                            display.invoke(this, value)
                        }
                    }
                )
                // setup single selection two-way data bindings
                // id -> T
                singleIdSelection.data.map { idToData[it] } handledBy singleSelection.update
                // T -> id
                singleSelection.data.map { if (it != null) idProvider(it) else null } handledBy singleIdSelection.update

                // setup multi selection two-way data bindings
                // id -> T
                multiIdSelection.data.map { ids ->
                    idToData.filterKeys { it in ids }
                }.map { it.values.toList() } handledBy multiSelection.update
                // T -> id
                multiSelection.data.map { data -> data.map { idProvider(it) } } handledBy multiIdSelection.update

                // setup disabled two-way data bindings
                // id -> T
                disabledIds.data.map { ids ->
                    idToData.filterKeys { it in ids }
                }.map { it.values.toList() } handledBy disabled.update
                // T -> id
                disabled.data.map { data -> data.map { idProvider(it) } } handledBy disabledIds.update
            }
        }
        itemsInStore = true
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(
                baseClass = classes {
                    +ComponentType.ToggleGroup
                    +("compact".modifier() `when` compact)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.ToggleGroup)
                applyElement(this)
                applyEvents(this)

                itemStore.data.map { items ->
                    headItems + items + tailItems
                }.render(into = this) { items ->
                    items.forEach { item ->
                        renderItem(this, item)
                    }
                }
            }
        }
    }

    private fun renderItem(context: RenderContext, item: ToggleGroupItem): Div = with(context) {
        div(baseClass = "toggle-group".component("item")) {
            button(baseClass = "toggle-group".component("button")) {
                if (singleSelect) {
                    classMap(
                        singleIdSelection.data.filterNotNull().map {
                            mapOf("selected".modifier() to (item.id == it))
                        }
                    )
                } else {
                    classMap(
                        multiIdSelection.data.map {
                            mapOf("selected".modifier() to (it.contains(item.id)))
                        }
                    )
                }
                disabled(disabledIds.data.map { it.contains(item.id) })
                if (singleSelect) {
                    clicks.map { item.id } handledBy singleIdSelection.update
                } else {
                    clicks.map { item.id } handledBy multiIdSelection.toggle
                }

                if (item.iconFirst) {
                    renderIcon(this, item)
                    renderText(this, item)
                } else {
                    renderText(this, item)
                    renderIcon(this, item)
                }
            }
        }
    }

    private fun renderIcon(context: RenderContext, item: ToggleGroupItem) {
        with(context) {
            item.icon?.let { icn ->
                span(baseClass = "toggle-group".component("icon")) {
                    icn(this)
                }
            }
        }
    }

    private fun renderText(context: RenderContext, item: ToggleGroupItem) {
        with(context) {
            if (item.hasTitle) {
                span(baseClass = "toggle-group".component("text")) {
                    item.applyTitle(this)
                }
            }
        }
    }
}

// ------------------------------------------------------ item & store

/**
 * DSL scope class to create [ToggleGroupItem]s when using [ToggleGroup.items] functions.
 */
public class ToggleGroupItemScope internal constructor(internal var id: String) {

    /**
     * Creates and returns a new [AccordionItem].
     */
    public fun item(title: String? = null, context: ToggleGroupItem.() -> Unit = {}): ToggleGroupItem =
        ToggleGroupItem(id, title).apply(context)
}

public open class ToggleGroupItem internal constructor(internal val id: String, title: String?) :
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal var icon: (RenderContext.() -> Unit)? = null
    internal var iconFirst: Boolean = false

    init {
        title?.let { this.title(it) }
    }

    /**
     * Sets the render function for the icon of the toggle group item.
     */
    public fun icon(iconClass: String = "", context: Icon.() -> Unit = {}) {
        iconFirst = !hasTitle
        this.icon = {
            icon(iconClass = iconClass) {
                context(this)
            }
        }
    }
}

public class StaticToggleGroupItem internal constructor(id: String, title: String?) : ToggleGroupItem(id, title) {

    internal var selectedFlag: Boolean? = null
    internal var disabledFlag: Boolean? = null
    internal var selectedFlow: Flow<Boolean>? = null
    internal var disabledFlow: Flow<Boolean>? = null

    public fun selected(value: Boolean) {
        this.selectedFlag = value
    }

    public fun selected(value: Flow<Boolean>) {
        this.selectedFlow = value
    }

    public fun disabled(value: Boolean) {
        this.disabledFlag = value
    }

    public fun disabled(value: Flow<Boolean>) {
        this.disabledFlow = value
    }
}

internal class ToggleGroupItemStore : RootStore<List<ToggleGroupItem>>(emptyList())

internal class ToggleGroupMultiSelectionStore : RootStore<List<String>>(emptyList()) {

    val select: Handler<Pair<String, Boolean>> = handle { ids, (id, select) ->
        if (select) ids + id else ids - id
    }

    val toggle: Handler<String> = handle { ids, id ->
        if (ids.contains(id)) ids - id else ids + id
    }
}
