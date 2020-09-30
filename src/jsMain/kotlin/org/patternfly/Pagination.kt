package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SingleMountPoint
import dev.fritz2.binding.action
import dev.fritz2.binding.const
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.Tag
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.valuesAsNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfPagination(
    store: ItemStore<T>,
    pageSizes: Array<Int> = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    id: String? = null,
    classes: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination =
    register(Pagination(store, store.data.map { it.pageInfo }, pageSizes, compact, id = id, classes = classes), content)

fun HtmlElements.pfPagination(
    pageInfo: PageInfo = PageInfo(),
    pageSizes: Array<Int> = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    id: String? = null,
    classes: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination {
    val store = PageInfoStore(pageInfo)
    return register(Pagination(store, store.data, pageSizes, compact, id = id, classes = classes), content)
}

// ------------------------------------------------------ tag

class Pagination internal constructor(
    val pageInfoHandler: PageInfoHandler,
    val pageInfoFlow: Flow<PageInfo>,
    pageSizes: Array<Int>,
    compact: Boolean,
    id: String?,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes {
        +ComponentType.Pagination
        +("compact".modifier() `when` compact)
        +classes
    }) {

    private val controlElements: MutableList<HTMLButtonElement> = mutableListOf()
    private var inputElement: HTMLInputElement? = null
    private val optionsMenu: OptionsMenu<Int>

    init {
        markAs(ComponentType.Pagination)
        div(baseClass = "pagination".component("total-items")) {
            this@Pagination.pageInfoFlow.showRange().invoke(this)
        }
        optionsMenu = pfOptionsMenu {
            display = {
                { +"${it.item} per page" }
            }
            pfOptionsMenuTogglePlain {
                content = { this@Pagination.pageInfoFlow.showRange().invoke(this) }
            }
            pfOptionsMenuItems {
                pageSizes.forEachIndexed { index, pageSize ->
                    pfItem(pageSize) {
                        selected = index == 0
                    }
                }
            }
            store.selection.map { Unit } handledBy ces.collapse
            store.selection.unwrap().map { it.first() } handledBy this@Pagination.pageInfoHandler.pageSize
        }
        nav(baseClass = "pagination".component("nav")) {
            if (!compact) {
                div(baseClass = classes("pagination".component("nav", "control"), "first".modifier())) {
                    this@Pagination.controlElements.add(pfButton("plain".modifier()) {
                        aria["label"] = "Go to first page"
                        disabled = this@Pagination.pageInfoFlow.map { it.firstPage }
                        clicks handledBy this@Pagination.pageInfoHandler.gotoFirstPage
                        pfIcon("angle-double-left".fas())
                    }.domNode)
                }
            }
            div(baseClass = classes("pagination".component("nav", "control"), "prev".modifier())) {
                this@Pagination.controlElements.add(pfButton("plain".modifier()) {
                    aria["label"] = "Go to previous page"
                    disabled = this@Pagination.pageInfoFlow.map { it.firstPage }
                    clicks handledBy this@Pagination.pageInfoHandler.gotoPreviousPage
                    pfIcon("angle-left".fas())
                }.domNode)
            }
            if (!compact) {
                div(baseClass = "pagination".component("nav", "page-select")) {
                    this@Pagination.inputElement = input(baseClass = "form-control".component()) {
                        aria["label"] = "Current page"
                        type = const("number")
                        min = const("1")
                        max = this@Pagination.pageInfoFlow.map { it.pages.toString() }
                        disabled = this@Pagination.pageInfoFlow.map { it.pages < 2 }
                        value = this@Pagination.pageInfoFlow.map { (if (it.total == 0) 0 else it.page + 1).toString() }
                        changes.valuesAsNumber()
                            .map { it.toInt() - 1 } handledBy this@Pagination.pageInfoHandler.gotoPage
                    }.domNode
                    span {
                        aria["hidden"] = true
                        +"of "
                        this@Pagination.pageInfoFlow.map {
                            if (it.total == 0) "0" else it.pages.toString()
                        }.bind(true)
                    }
                }
            }
            div(baseClass = classes("pagination".component("nav", "control"), "next".modifier())) {
                this@Pagination.controlElements.add(pfButton("plain".modifier()) {
                    aria["label"] = "Go to next page"
                    disabled = this@Pagination.pageInfoFlow.map { it.lastPage }
                    clicks handledBy this@Pagination.pageInfoHandler.gotoNextPage
                    pfIcon("angle-right".fas())
                }.domNode)
            }
            if (!compact) {
                div(baseClass = classes("pagination".component("nav", "control"), "last".modifier())) {
                    this@Pagination.controlElements.add(pfButton("plain".modifier()) {
                        aria["label"] = "Go to last page"
                        disabled = this@Pagination.pageInfoFlow.map { it.lastPage }
                        clicks handledBy this@Pagination.pageInfoHandler.gotoLastPage
                        pfIcon("angle-double-right".fas())
                    }.domNode)
                }
            }
        }
    }

    var disabled: Flow<Boolean>
        get() = throw NotImplementedError()
        set(flow) {
            object : SingleMountPoint<Boolean>(flow) {
                override fun set(value: Boolean, last: Boolean?) {
                    optionsMenu.toggle.disabled = const(value)
                    if (value) {
                        controlElements.forEach { it.disabled = true }
                        inputElement?.let { it.disabled = true }
                    } else {
                        action() handledBy pageInfoHandler.refresh
                    }
                }
            }
        }
}

// ------------------------------------------------------ store

fun Flow<PageInfo>.showRange(): Tag<HTMLElement>.() -> Unit = {
    b {
        this@showRange.map { if (it.total == 0) "0" else it.range.first.toString() }.bind(true)
        +" - "
        this@showRange.map { it.range.last.toString() }.bind(true)
    }
    domNode.appendChild(TextNode(" of ").domNode)
    b {
        this@showRange.map { it.total.toString() }.bind()
    }
}

interface PageInfoHandler {

    val gotoFirstPage: Handler<Unit>
    val gotoPreviousPage: Handler<Unit>
    val gotoNextPage: Handler<Unit>
    val gotoLastPage: Handler<Unit>
    val gotoPage: Handler<Int>
    val pageSize: Handler<Int>
    val total: Handler<Int>
    val refresh: Handler<Unit>
}

class PageInfoStore(pageInfo: PageInfo) : RootStore<PageInfo>(pageInfo), PageInfoHandler {

    override val gotoFirstPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoFirstPage() }
    override val gotoPreviousPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoPreviousPage() }
    override val gotoNextPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoNextPage() }
    override val gotoLastPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoLastPage() }
    override val gotoPage: Handler<Int> = handle { pageInfo, page -> pageInfo.gotoPage(page) }
    override val pageSize: Handler<Int> = handle { pageInfo, pageSize -> pageInfo.pageSize(pageSize) }
    override val total: Handler<Int> = handle { pageInfo, total -> pageInfo.total(total) }
    override val refresh: Handler<Unit> = handle { pageInfo -> pageInfo.refresh() }
}
