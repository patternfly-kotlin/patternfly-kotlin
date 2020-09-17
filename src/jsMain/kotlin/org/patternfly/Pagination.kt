package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
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

fun HtmlElements.pfPagination(
    pageInfo: PageInfo = PageInfo(),
    pageSizes: Array<Int> = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    classes: String? = null, content: Pagination.() -> Unit = {}
): Pagination = register(Pagination(pageInfo, pageSizes, compact, classes), content)

// ------------------------------------------------------ tag

class Pagination internal constructor(
    pageInfo: PageInfo,
    pageSizes: Array<Int>,
    compact: Boolean,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>,
    Div(baseClass = classes {
        +ComponentType.Pagination
        +("compact".modifier() `when` compact)
        +classes
    }) {

    private val controlElements: MutableList<HTMLButtonElement> = mutableListOf()
    private var inputElement: HTMLInputElement? = null
    private val optionsMenu: OptionsMenu<Int>
    val store: PageInfoStore = PageInfoStore(pageInfo)

    init {
        markAs(ComponentType.Pagination)
        div(baseClass = "pagination".component("total-items")) {
            this@Pagination.store.showRange().invoke(this)
        }
        optionsMenu = pfOptionsMenu {
            display = {
                { +"${it.item} per page" }
            }
            pfOptionsMenuTogglePlain {
                content = { this@Pagination.store.showRange().invoke(this) }
            }
            pfOptionsMenuItems {
                pageSizes.forEachIndexed { index, pageSize ->
                    pfItem(pageSize) {
                        selected = index == 0
                    }
                }
            }
            store.selection.map { Unit } handledBy ces.collapse
            store.selection.unwrap().map { it.first() } handledBy this@Pagination.store.pageSize
        }
        nav(baseClass = "pagination".component("nav")) {
            if (!compact) {
                div(baseClass = classes("pagination".component("nav", "control"), "first".modifier())) {
                    this@Pagination.controlElements.add(pfButton("plain".modifier()) {
                        aria["label"] = "Go to first page"
                        disabled = this@Pagination.store.data.map { it.firstPage }
                        clicks handledBy this@Pagination.store.gotoFirstPage
                        pfIcon("angle-double-left".fas())
                    }.domNode)
                }
            }
            div(baseClass = classes("pagination".component("nav", "control"), "prev".modifier())) {
                this@Pagination.controlElements.add(pfButton("plain".modifier()) {
                    aria["label"] = "Go to previous page"
                    disabled = this@Pagination.store.data.map { it.firstPage }
                    clicks handledBy this@Pagination.store.gotoPreviousPage
                    pfIcon("angle-left".fas())
                }.domNode)
            }
            if (!compact) {
                div(baseClass = "pagination".component("nav", "page-select")) {
                    this@Pagination.inputElement = input(baseClass = "form-control".component()) {
                        aria["label"] = "Current page"
                        type = const("number")
                        min = const("1")
                        max = this@Pagination.store.data.map { it.pages.toString() }
                        disabled = this@Pagination.store.data.map { it.pages < 2 }
                        value = this@Pagination.store.data.map { (if (it.total == 0) 0 else it.page + 1).toString() }
                        changes.valuesAsNumber().map { it.toInt() - 1 } handledBy this@Pagination.store.gotoPage
                    }.domNode
                    span {
                        aria["hidden"] = true
                        +"of "
                        this@Pagination.store.data.map {
                            if (it.total == 0) "0" else it.pages.toString()
                        }.bind(true)
                    }
                }
            }
            div(baseClass = classes("pagination".component("nav", "control"), "next".modifier())) {
                this@Pagination.controlElements.add(pfButton("plain".modifier()) {
                    aria["label"] = "Go to next page"
                    disabled = this@Pagination.store.data.map { it.lastPagePage }
                    clicks handledBy this@Pagination.store.gotoNextPage
                    pfIcon("angle-right".fas())
                }.domNode)
            }
            if (!compact) {
                div(baseClass = classes("pagination".component("nav", "control"), "last".modifier())) {
                    this@Pagination.controlElements.add(pfButton("plain".modifier()) {
                        aria["label"] = "Go to last page"
                        disabled = this@Pagination.store.data.map { it.lastPagePage }
                        clicks handledBy this@Pagination.store.gotoLastPage
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
                        action() handledBy store.refresh
                    }
                }
            }
        }
}

// ------------------------------------------------------ store

class PageInfoStore(pageInfo: PageInfo) : RootStore<PageInfo>(pageInfo) {

    val gotoFirstPage: SimpleHandler<Unit> = handle { pageInfo -> pageInfo.gotoFirstPage() }
    val gotoPreviousPage: SimpleHandler<Unit> = handle { pageInfo -> pageInfo.gotoPreviousPage() }
    val gotoNextPage: SimpleHandler<Unit> = handle { pageInfo -> pageInfo.gotoNextPage() }
    val gotoLastPage: SimpleHandler<Unit> = handle { pageInfo -> pageInfo.gotoLastPage() }
    val gotoPage: SimpleHandler<Int> = handle { pageInfo, page -> pageInfo.gotoPage(page) }
    val pageSize: SimpleHandler<Int> = handle { pageInfo, pageSize -> pageInfo.pageSize(pageSize) }
    val total: SimpleHandler<Int> = handle { pageInfo, total -> pageInfo.total(total) }
    val refresh: SimpleHandler<Unit> = handle { pageInfo-> pageInfo.refresh() }

    internal fun showRange(): Tag<HTMLElement>.() -> Unit = {
        b {
            data.map { if (it.total == 0) "0" else it.range.first.toString() }.bind(true)
            +" - "
            data.map { it.range.last.toString() }.bind(true)
        }
        domNode.appendChild(TextNode(" of ").domNode)
        b {
            data.map { it.total.toString() }.bind()
        }
    }
}
