package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.states
import dev.fritz2.elemento.By
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.closest
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
 */
public fun <T> RenderContext.dataList(
    store: ItemStore<T> = ItemStore(),
    selectableRows: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: DataList<T>.() -> Unit = {}
): DataList<T> = register(DataList(store, selectableRows, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListAction] container in the [DataListRow].
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
 * Creates a [DataListCell] inside the [DataListContent] container.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListContent<T>.dataListCell(
    id: String? = null,
    baseClass: String? = null,
    content: DataListCell<T>.() -> Unit = {}
): DataListCell<T> = register(DataListCell(itemStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DataListCheck] inside the [DataListControl] container.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListControl<T>.dataListCheck(
    id: String? = null,
    baseClass: String? = null,
    content: DataListCheck<T>.() -> Unit = {}
): DataListCheck<T> = register(DataListCheck(this.itemStore, this.item, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListContent] container inside the [DataListRow].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListRow<T>.dataListContent(
    id: String? = null,
    baseClass: String? = null,
    content: DataListContent<T>.() -> Unit = {}
): DataListContent<T> = register(DataListContent(itemStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListControl] container inside the [DataListRow].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListRow<T>.dataListControl(
    id: String? = null,
    baseClass: String? = null,
    content: DataListControl<T>.() -> Unit = {}
): DataListControl<T> =
    register(
        DataListControl(this.itemStore, this.item, this.dataListItem, id = id, baseClass = baseClass, job),
        content
    )

/**
 * Creates the [DataListExpandableContent] container inside the [DataListItem].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the expandable content container
 */
public fun <T> DataListItem<T>.dataListExpandableContent(
    id: String? = Id.unique(ComponentType.DataList.id, "ec"),
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): DataListExpandableContent<T> =
    register(DataListExpandableContent(this, id = id, baseClass = baseClass, job, content), {})

/**
 * Creates the [DataListItem] container. All other elements are nested inside this container.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataList<T>.dataListItem(
    item: T,
    id: String? = "${itemStore.idProvider(item)}-row",
    baseClass: String? = null,
    content: DataListItem<T>.() -> Unit = {}
): DataListItem<T> = register(DataListItem(this.itemStore, item, this, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListRow] inside the [DataListItem] container.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataListItem<T>.dataListRow(
    id: String? = null,
    baseClass: String? = null,
    content: DataListRow<T>.() -> Unit = {}
): DataListRow<T> = register(DataListRow(this.itemStore, this.item, this, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [DataListToggle] to to expand / collapse the [DataListExpandableContent].
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
    register(DataListToggle(this.itemStore, this.item, this.dataListItem, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [data list](https://www.patternfly.org/v4/components/data-list/design-guidelines) component.
 *
 * A data list is used to display large data sets when you need a flexible layout or need to include interactive content like charts. The data list uses a [display] function to render the items in an [ItemStore] as [DataListItem]s.
 *
 * One of the elements in the [display] should use the [ItemStore.idProvider] to assign an element ID. This ID is referenced by various [ARIA labelledby](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-labelledby_attribute) attributes.
 *
 * @param T the type which is used for the [DataListItem]s in this data list.
 *
 * @sample DataListSamples.dataList
 */
public class DataList<T> internal constructor(
    internal val itemStore: ItemStore<T>,
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
     * Defines how to display the items in the [ItemStore] as [DataListItem]s. Call this function *before* populating the store.
     */
    public fun display(display: (T) -> DataListItem<T>) {
        itemStore.visible.renderEach({ itemStore.idProvider(it) }, { item -> display(item) })
    }
}

/**
 * A container to group the actions in a [DataListRow].
 */
public class DataListAction internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("data-list".component("item-action"), baseClass), job)

/**
 * A cell in a [DataListContent] container. Cells are usually used to display properties of the items.
 */
public class DataListCell<T> internal constructor(itemStore: ItemStore<T>, id: String?, baseClass: String?, job: Job) :
    WithIdProvider<T> by itemStore,
    Div(id = id, baseClass = classes("data-list".component("cell"), baseClass), job) {

    init {
        domNode.closest(By.classname("data-list".component("item")))?.let {
            attr("rowId", it.id)
        }
    }
}

/**
 * Checkbox to (de)select a data item. The checkbox is bound to the selection state of the [ItemStore].
 *
 * You can use the [ItemStore] to track the selection of an item.
 *
 * @sample DataListSamples.selects
 */
public class DataListCheck<T> internal constructor(
    private val itemStore: ItemStore<T>,
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
            checked(this@DataListCheck.itemStore.data.map { it.isSelected(this@DataListCheck.item) })
            aria["invalid"] = false
            aria["labelledby"] = this@DataListCheck.itemStore.idProvider(this@DataListCheck.item)
            changes.states().map { (this@DataListCheck.item to it) } handledBy this@DataListCheck.itemStore.select
        }
    }
}

/**
 * Container for [DataListCell]s in a [DataListRow].
 */
public class DataListContent<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemStore,
    Div(id = id, baseClass = classes("data-list".component("item-content"), baseClass), job) {

    init {
        domNode.closest(By.classname("data-list".component("item")))?.let {
            attr("rowId", it.id)
        }
    }
}

/**
 * Container for controls of a [DataListRow]. Use this class to add a [DataListToggle] or a [DataListCheck].
 */
public class DataListControl<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    internal val dataListItem: DataListItem<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(id = id, baseClass = classes("data-list".component("item-control"), baseClass), job)

/**
 * Container for expandable content inside a [DataListItem].
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
        attr("hidden", dataListItem.ces.data.map { !it })
        div(baseClass = "data-list".component("expandable-content", "body")) {
            content(this)
        }
    }
}

/**
 * Container for an item in a [DataList]. All other elements are nested inside this container.
 */
public class DataListItem<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    dataList: DataList<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemStore,
    Li(id = id, baseClass = classes {
        +"data-list".component("item")
        +("selectable".modifier() `when` dataList.selectableRows)
        +baseClass
    }, job) {

    /**
     * Manages the **c**ollapse / **e**xpand **s**tate of the [DataListExpandableContent]. Use this property if you want to track the collapse / expand state.
     *
     * @sample DataListSamples.collapseExpandHandler
     */
    public val ces: CollapseExpandStore = CollapseExpandStore()

    internal var toggleButton: HTMLButtonElement? = null

    init {
        aria["labelledby"] = itemStore.idProvider(item)
        if (dataList.selectableRows) {
            classMap(ces.data
                .combine(itemStore.data.map { it.isSelected(item) }) { expanded, selected ->
                    expanded to selected
                }
                .map { (expanded, selected) ->
                    mapOf(
                        "expanded".modifier() to expanded,
                        "selected".modifier() to selected
                    )
                }
            )
            clicks.map { item } handledBy itemStore.selectItem
        } else {
            classMap(ces.data.map { mapOf("expanded".modifier() to it) })
        }
    }
}

/**
 * Container for the main data of an [DataListItem] (except content for [DataListExpandableContent]).
 */
public class DataListRow<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    internal val dataListItem: DataListItem<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemStore,
    Div(id = id, baseClass = classes("data-list".component("item-row"), baseClass), job) {

    init {
        domNode.closest(By.classname("data-list".component("item")))?.let {
            attr("rowId", it.id)
        }
    }
}

/**
 * Toggle to expand / collapse the [DataListExpandableContent].
 */
public class DataListToggle<T> internal constructor(
    private val itemStore: ItemStore<T>,
    private val item: T,
    private val dataListItem: DataListItem<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(baseClass = classes("data-list".component("toggle"), baseClass), job = job) {

    init {
        button(id = id, baseClass = "plain".modifier()) {
            this@DataListToggle.dataListItem.toggleButton = domNode
            aria["label"] = "Details"
            aria["labelledby"] = "$id ${this@DataListToggle.itemStore.idProvider(this@DataListToggle.item)}"
            aria["expanded"] = this@DataListToggle.dataListItem.ces.data.map { it.toString() }
            div(baseClass = "data-list".component("toggle", "icon")) {
                icon("angle-right".fas())
            }
            clicks handledBy this@DataListToggle.dataListItem.ces.toggle
        }
    }
}

