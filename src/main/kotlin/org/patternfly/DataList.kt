package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.states
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLUListElement

// TODO draggable rows
//  breakpoints
// ------------------------------------------------------ dsl

/**
 * Creates a [DataList] component.
 *
 * @param store the item store
 * @param selectableRows whether the rows are selectable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.DataListSample.dataList
 */
public fun <T> RenderContext.dataList(
    store: SelectableItemPageContents<T> = ItemsStore(),
    selectableRows: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: DataList<T>.() -> Unit = {}
): DataList<T> = register(DataList(store, selectableRows, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListAction] component inside the [DataListRow] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListRow<T>.dataListAction(
    id: String? = null,
    baseClass: String? = null,
    content: DataListAction.() -> Unit = {}
): DataListAction = register(DataListAction(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DataListCell] component inside the [DataListContent] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListContent<T>.dataListCell(
    id: String? = null,
    baseClass: String? = null,
    content: DataListCell<T>.() -> Unit = {}
): DataListCell<T> = register(DataListCell(itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListCheckbox] component inside the [DataListControl] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListControl<T>.dataListCheckbox(
    id: String? = null,
    baseClass: String? = null,
    content: DataListCheckbox<T>.() -> Unit = {}
): DataListCheckbox<T> =
    register(DataListCheckbox(this.itemsStore, this.item, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListContent] component inside the [DataListRow] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListRow<T>.dataListContent(
    id: String? = null,
    baseClass: String? = null,
    content: DataListContent<T>.() -> Unit = {}
): DataListContent<T> = register(DataListContent(itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListControl] component inside the [DataListRow] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListRow<T>.dataListControl(
    id: String? = null,
    baseClass: String? = null,
    content: DataListControl<T>.() -> Unit = {}
): DataListControl<T> = register(
    DataListControl(this.itemsStore, this.item, this.dataListItem, id = id, baseClass = baseClass, job),
    content
)

/**
 * Creates the [DataListExpandableContent] component inside the [DataListItem] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the expandable content
 */
public fun <T> DataListItem<T>.dataListExpandableContent(
    id: String? = Id.unique(ComponentType.DataList.id, "ec"),
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): DataListExpandableContent<T> =
    register(DataListExpandableContent(this, id = id, baseClass = baseClass, job, content), {})

/**
 * Creates the [DataListItem] component. All other components are nested inside this component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataList<T>.dataListItem(
    item: T,
    id: String? = "${itemsStore.idProvider(item)}-row",
    baseClass: String? = null,
    content: DataListItem<T>.() -> Unit = {}
): DataListItem<T> = register(DataListItem(this.itemsStore, item, this, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListRow] component inside the [DataListItem] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListItem<T>.dataListRow(
    id: String? = null,
    baseClass: String? = null,
    content: DataListRow<T>.() -> Unit = {}
): DataListRow<T> =
    register(DataListRow(this.itemsStore, this.item, this, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListToggle] component inside the [DataListControl] component.
 *
 * If you use this component, don't forget to also use [dataListExpandableContent] to add a [DataListExpandableContent] component to the [DataListItem] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListControl<T>.dataListToggle(
    id: String? = Id.unique(ComponentType.DataList.id, "tgl"),
    baseClass: String? = null,
    content: DataListToggle<T>.() -> Unit = {}
): DataListToggle<T> =
    register(
        DataListToggle(this.itemsStore, this.item, this.dataListItem, id = id, baseClass = baseClass, job),
        content
    )

// ------------------------------------------------------ tag

/**
 * PatternFly [data list](https://www.patternfly.org/v4/components/data-list/design-guidelines) component.
 *
 * A data list is used to display large data sets when you need a flexible layout or need to include interactive content like charts. The data list uses a [display] function to render the items in an [ItemsStore] as [DataListItem]s.
 *
 * One of the tags used in the [display] function should assign an [element ID][org.w3c.dom.Element.id] based on [ItemsStore.idProvider]. This ID is referenced by various [ARIA labelledby](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-labelledby_attribute) attributes. Since most of the data list components implement [WithIdProvider], this can be easily done using [org.patternfly.WithIdProvider.itemId].
 *
 * @param T the type which is used for the [DataListItem]s in this data list.
 *
 * @sample org.patternfly.sample.DataListSample.dataList
 */
public class DataList<T> internal constructor(
    internal val itemsStore: SelectableItemPageContents<T>,
    internal val selectableRows: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLUListElement>, Ul(id = id, baseClass = classes(ComponentType.DataList, baseClass), job) {

    init {
        markAs(ComponentType.DataList)
        attr("role", "list")
    }

    /**
     * Defines how to display the items in the [ItemsStore] as [DataListItem]s.
     */
    public fun display(display: (T) -> DataListItem<T>) {
        itemsStore.page.renderEach({ itemsStore.idProvider(it) }) { item -> display(item) }
    }
}

/**
 * A component to group the actions in a [DataListRow] component.
 */
public class DataListAction internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("data-list".component("item-action"), baseClass), job)

/**
 * A cell in a [DataListContent] component. Cells are usually used to display properties of the items.
 */
public class DataListCell<T> internal constructor(
    itemsStore: ItemPageContents<T>,
    id: String?,
    baseClass: String?,
    job: Job
) :
    WithIdProvider<T> by itemsStore,
    Div(id = id, baseClass = classes("data-list".component("cell"), baseClass), job)

/**
 * Checkbox to (de)select a data item. The checkbox is bound to the selection state of the [ItemsStore].
 *
 * You can use the [ItemsStore] to track the selection of an item.
 *
 * @sample org.patternfly.sample.DataListSample.selects
 */
public class DataListCheckbox<T> internal constructor(
    private val itemsStore: SelectableItemPageContents<T>,
    private val item: T,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(id = id, baseClass = classes("data-list".component("check"), baseClass), job) {

    init {
        input {
            val inputId = Id.unique(ComponentType.DataList.id, "chk")
            name(inputId)
            type("checkbox")
            checked(this@DataListCheckbox.itemsStore.isSelected(this@DataListCheckbox.item))
            aria["invalid"] = false
            aria["labelledby"] = this@DataListCheckbox.itemsStore.idProvider(this@DataListCheckbox.item)
            changes.states()
                .map { (this@DataListCheckbox.item to it) } handledBy this@DataListCheckbox.itemsStore.select
        }
    }
}

/**
 * Component to group [DataListCell]s components inside a [DataListRow] component.
 */
public class DataListContent<T> internal constructor(
    internal val itemsStore: ItemPageContents<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(id = id, baseClass = classes("data-list".component("item-content"), baseClass), job)

/**
 * Component for controls of a [DataListRow] component. Use this class to add a [DataListToggle] or a [DataListCheckbox] component.
 */
public class DataListControl<T> internal constructor(
    internal val itemsStore: SelectableItemPageContents<T>,
    internal val item: T,
    internal val dataListItem: DataListItem<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(id = id, baseClass = classes("data-list".component("item-control"), baseClass), job)

/**
 * Component for the expandable content inside a [DataListItem] component.
 */
public class DataListExpandableContent<T> internal constructor(
    dataListItem: DataListItem<T>,
    id: String?,
    baseClass: String?,
    job: Job,
    content: Div.() -> Unit
) : TextElement(
    "section",
    id = id,
    baseClass = classes("data-list".component("expandable-content"), baseClass),
    job
) {

    init {
        domNode.hidden = true // tp prevent flickering during updates
        if (dataListItem.toggleButton != null && id != null) {
            dataListItem.toggleButton!!.aria["controls"] = id
        }
        attr("hidden", dataListItem.expanded.data.map { !it })
        div(baseClass = "data-list".component("expandable-content", "body")) {
            content(this)
        }
    }
}

/**
 * Component for an item in a [DataList]. All other components are nested inside this component.
 *
 * The data list is very flexible when it comes to displaying the items in the [ItemsStore]. You should at least add a [DataListRow] for each item you want to render. If you want to use controls like a checkbox and / or a toggle, add them inside a [DataListControl] component. The actual content should be added inside multiple [DataListCell]s inside a [DataListContent] component. If you want to add actions like [PushButton]s or [Dropdown]s, add them inside a [DataListAction] component. Finally the expandable content goes inside a [DataListExpandableContent] component.
 *
 * ```
 * ┏━━━━━━━━━━━ dataList: DataListItem ━━━━━━━━━━━┓
 * ┃                                              ┃
 * ┃ ┌──────── dataListRow: DataListRow ────────┐ ┃
 * ┃ │                                          │ ┃
 * ┃ │ ┌── dataListControl: DataListControl ──┐ │ ┃
 * ┃ │ │ ┌──────────────────────────────────┐ │ │ ┃
 * ┃ │ │ │  dataListToggle: DataListToggle  │ │ │ ┃
 * ┃ │ │ └──────────────────────────────────┘ │ │ ┃
 * ┃ │ │ ┌──────────────────────────────────┐ │ │ ┃
 * ┃ │ │ │dataListCheckbox: DataListCheckbox│ │ │ ┃
 * ┃ │ │ └──────────────────────────────────┘ │ │ ┃
 * ┃ │ └──────────────────────────────────────┘ │ ┃
 * ┃ │                                          │ ┃
 * ┃ │ ┌── dataListContent: DataListContent ──┐ │ ┃
 * ┃ │ │ ┌──────────────────────────────────┐ │ │ ┃
 * ┃ │ │ │    dataListCell: DataListCell    │ │ │ ┃
 * ┃ │ │ └──────────────────────────────────┘ │ │ ┃
 * ┃ │ └──────────────────────────────────────┘ │ ┃
 * ┃ │                                          │ ┃
 * ┃ │ ┌─── dataListAction: DataListAction ───┐ │ ┃
 * ┃ │ │ ┌──────────────────────────────────┐ │ │ ┃
 * ┃ │ │ │  control components like buttons │ │ │ ┃
 * ┃ │ │ │           or dropdowns           │ │ │ ┃
 * ┃ │ │ └──────────────────────────────────┘ │ │ ┃
 * ┃ │ └──────────────────────────────────────┘ │ ┃
 * ┃ └──────────────────────────────────────────┘ ┃
 * ┃ ┌──────────────────────────────────────────┐ ┃
 * ┃ │        dataListExpandableContent:        │ ┃
 * ┃ │        DataListExpandableContent         │ ┃
 * ┃ └──────────────────────────────────────────┘ ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * ```
 */
public class DataListItem<T> internal constructor(
    internal val itemsStore: SelectableItemPageContents<T>,
    internal val item: T,
    dataList: DataList<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Li(
        id = id,
        baseClass = classes {
            +"data-list".component("item")
            +("selectable".modifier() `when` dataList.selectableRows)
            +baseClass
        },
        job
    ) {

    /**
     * Manages the expanded state of the [DataListExpandableContent]. Use this property if you want to track the collapse / expand state.
     *
     * @sample org.patternfly.sample.DataListSample.expanded
     */
    public val expanded: ExpandedStore = ExpandedStore()

    internal var toggleButton: HTMLButtonElement? = null

    init {
        aria["labelledby"] = itemsStore.idProvider(item)
        if (dataList.selectableRows) {
            classMap(
                expanded.data.combine(itemsStore.isSelected(item)) { expanded, selected ->
                    expanded to selected
                }.map { (expanded, selected) ->
                    mapOf(
                        "expanded".modifier() to expanded,
                        "selected".modifier() to selected
                    )
                }
            )
            clicks.map { item } handledBy itemsStore.selectOnly
        } else {
            classMap(expanded.data.map { mapOf("expanded".modifier() to it) })
        }
    }
}

/**
 * Component for the main data of a [DataListItem] component (except content for [DataListExpandableContent]).
 */
public class DataListRow<T> internal constructor(
    internal val itemsStore: SelectableItemPageContents<T>,
    internal val item: T,
    internal val dataListItem: DataListItem<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(id = id, baseClass = classes("data-list".component("item-row"), baseClass), job)

/**
 * Toggle to expand / collapse the [DataListExpandableContent].
 *
 * If you use this component, don't forget to also add a [DataListExpandableContent] component to the [DataListItem] component.
 */
public class DataListToggle<T> internal constructor(
    private val itemsStore: ItemPageContents<T>,
    private val item: T,
    private val dataListItem: DataListItem<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(baseClass = classes("data-list".component("toggle"), baseClass), job = job) {

    init {
        clickButton(plain) {
            this@DataListToggle.dataListItem.toggleButton = domNode
            aria["label"] = "Details"
            aria["labelledby"] = "$id ${this@DataListToggle.itemsStore.idProvider(this@DataListToggle.item)}"
            aria["expanded"] = this@DataListToggle.dataListItem.expanded.data.map { it.toString() }
            div(baseClass = "data-list".component("toggle", "icon")) {
                icon("angle-right".fas())
            }
        } handledBy this@DataListToggle.dataListItem.expanded.toggle
    }
}
