package org.patternfly

import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.Tag
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.valuesAsNumber
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.plain
import org.patternfly.ItemSelection.SINGLE
import org.patternfly.dom.aria
import org.patternfly.dom.plusAssign
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

// ------------------------------------------------------ dsl

/**
 * Creates a new pagination component based on the specified [ItemsStore]. Use this function to bind the pagination component to an [ItemsStore].
 *
 * @param store the item store
 * @param pageSizes the size of one page
 * @param compact whether to use a compact layout
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.PaginationSample.itemStore
 */
public fun <T> RenderContext.pagination(
    store: ItemsStore<T>,
    pageSizes: IntArray = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination = register(
    Pagination(store, store.data.map { it.pageInfo }, pageSizes, compact, id = id, baseClass = baseClass, job),
    content
)

/**
 * Creates a new pagination component based on the specified [PageInfo] instance. Use this function to bind the pagination component to a [PageInfo] instance.
 *
 * @param pageInfo the [PageInfo] instance
 * @param pageSizes the size of one page
 * @param compact whether to use a compact layout
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.PaginationSample.pageInfo
 */
public fun RenderContext.pagination(
    pageInfo: PageInfo = PageInfo(),
    pageSizes: IntArray = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination {
    val store = PageInfoStore(pageInfo)
    return register(Pagination(store, store.data, pageSizes, compact, id = id, baseClass = baseClass, job), content)
}

/**
 * Creates a new pagination component inside the specified [ToolbarItem] based on the specified [ItemsStore].
 *
 * @receiver the toolbar item this pagination component is part of
 *
 * @param store the item store
 * @param pageSizes the size of one page
 * @param compact whether to use a compact layout
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.PaginationSample.toolbar
 */
public fun <T> ToolbarItem.pagination(
    store: ItemsStore<T>,
    pageSizes: IntArray = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination {
    this.domNode.classList += "pagination".modifier()
    return register(
        Pagination(
            store,
            store.data.map { it.pageInfo },
            pageSizes,
            compact,
            id = id,
            baseClass = baseClass,
            job
        ),
        content
    )
}

// ------------------------------------------------------ tag

/**
 * PatternFly [pagination](https://www.patternfly.org/v4/components/pagination/design-guidelines) component.
 *
 * A pagination component gives users more navigational capability on pages with content views.
 *
 * Usually a pagination component is part of a toolbar and is bound to an [ItemsStore].
 *
 * @sample org.patternfly.sample.PaginationSample.toolbar
 */
public class Pagination internal constructor(
    public val pageInfoHandler: PageInfoHandler,
    public val pageInfoFlow: Flow<PageInfo>,
    pageSizes: IntArray,
    compact: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>,
    Div(
        id = id,
        baseClass = classes {
            +ComponentType.Pagination
            +("compact".modifier() `when` compact)
            +baseClass
        },
        job
    ) {

    private val controlElements: MutableList<HTMLButtonElement> = mutableListOf()
    private var inputElement: HTMLInputElement? = null
    private val optionsMenu: OptionsMenu<Int>

    init {
        markAs(ComponentType.Pagination)
        div(baseClass = "pagination".component("total-items")) {
            this@Pagination.pageInfoFlow.showRange().invoke(this)
        }
        optionsMenu = optionsMenu(itemSelection = SINGLE, closeOnSelect = true) {
            textToggle(plain = true) {
                this@Pagination.pageInfoFlow.showRange().invoke(this)
            }
            display { +"$it per page" }
            items {
                pageSizes.forEachIndexed { index, pageSize ->
                    item(pageSize) {
                        selected = index == 0
                    }
                }
            }
            store.singleSelection.filterNotNull().unwrap() handledBy this@Pagination.pageInfoHandler.pageSize
        }
        nav(baseClass = "pagination".component("nav")) {
            if (!compact) {
                div(baseClass = classes("pagination".component("nav", "control"), "first".modifier())) {
                    this@Pagination.controlElements.add(
                        pushButton(plain) {
                            aria["label"] = "Go to first page"
                            disabled(this@Pagination.pageInfoFlow.map { it.firstPage })
                            clicks handledBy this@Pagination.pageInfoHandler.gotoFirstPage
                            icon("angle-double-left".fas())
                        }.domNode
                    )
                }
            }
            div(baseClass = classes("pagination".component("nav", "control"), "prev".modifier())) {
                this@Pagination.controlElements.add(
                    pushButton(plain) {
                        aria["label"] = "Go to previous page"
                        disabled(this@Pagination.pageInfoFlow.map { it.firstPage })
                        clicks handledBy this@Pagination.pageInfoHandler.gotoPreviousPage
                        icon("angle-left".fas())
                    }.domNode
                )
            }
            if (!compact) {
                div(baseClass = "pagination".component("nav", "page-select")) {
                    this@Pagination.inputElement = input(baseClass = "form-control".component()) {
                        aria["label"] = "Current page"
                        type("number")
                        min("1")
                        max(this@Pagination.pageInfoFlow.map { it.pages.toString() })
                        disabled(this@Pagination.pageInfoFlow.map { it.pages < 2 })
                        value(this@Pagination.pageInfoFlow.map { (if (it.total == 0) 0 else it.page + 1).toString() })
                        changes.valuesAsNumber()
                            .map { it.toInt() - 1 } handledBy this@Pagination.pageInfoHandler.gotoPage
                    }.domNode
                    span {
                        aria["hidden"] = true
                        +"of "
                        this@Pagination.pageInfoFlow.map {
                            if (it.total == 0) "0" else it.pages.toString()
                        }.asText()
                    }
                }
            }
            div(baseClass = classes("pagination".component("nav", "control"), "next".modifier())) {
                this@Pagination.controlElements.add(
                    pushButton(plain) {
                        aria["label"] = "Go to next page"
                        disabled(this@Pagination.pageInfoFlow.map { it.lastPage })
                        clicks handledBy this@Pagination.pageInfoHandler.gotoNextPage
                        icon("angle-right".fas())
                    }.domNode
                )
            }
            if (!compact) {
                div(baseClass = classes("pagination".component("nav", "control"), "last".modifier())) {
                    this@Pagination.controlElements.add(
                        pushButton(plain) {
                            aria["label"] = "Go to last page"
                            disabled(this@Pagination.pageInfoFlow.map { it.lastPage })
                            clicks handledBy this@Pagination.pageInfoHandler.gotoLastPage
                            icon("angle-double-right".fas())
                        }.domNode
                    )
                }
            }
        }
    }

    /**
     * Disables / enabled this pagination instance.
     */
    public fun disabled(value: Boolean) {
        optionsMenu.disabled(value)
        controlElements.forEach { it.disabled = value }
        inputElement?.let { it.disabled = value }
    }

    /**
     * Disables / enabled this pagination instance.
     */
    public fun disabled(value: Flow<Boolean>) {
        optionsMenu.disabled(value)
        mountSingle(job, value) { v, _ ->
            if (v) {
                controlElements.forEach { it.disabled = true }
                inputElement?.let { it.disabled = true }
            } else {
                pageInfoHandler.refresh(Unit)
            }
        }
    }
}

// ------------------------------------------------------ store

internal fun Flow<PageInfo>.showRange(): Tag<HTMLElement>.() -> Unit = {
    b {
        this@showRange.map { if (it.total == 0) "0" else it.range.first.toString() }.asText()
        +" - "
        this@showRange.map { it.range.last.toString() }.asText()
    }
    domNode.appendChild(TextNode(" of ").domNode)
    b {
        this@showRange.map { it.total.toString() }.asText()
    }
}
