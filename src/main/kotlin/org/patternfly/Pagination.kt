package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.Tag
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.valuesAsNumber
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.aria
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

// TODO Document me
// ------------------------------------------------------ dsl

public fun <T> RenderContext.pagination(
    store: ItemStore<T>,
    pageSizes: Array<Int> = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination = register(
    Pagination(store, store.data.map { it.pageInfo }, pageSizes, compact, id = id, baseClass = baseClass, job),
    content
)

public fun RenderContext.pagination(
    pageInfo: PageInfo = PageInfo(),
    pageSizes: Array<Int> = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination {
    val store = PageInfoStore(pageInfo)
    return register(Pagination(store, store.data, pageSizes, compact, id = id, baseClass = baseClass, job), content)
}

// ------------------------------------------------------ tag

public class Pagination internal constructor(
    public val pageInfoHandler: PageInfoHandler,
    public val pageInfoFlow: Flow<PageInfo>,
    pageSizes: Array<Int>,
    compact: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes {
        +ComponentType.Pagination
        +("compact".modifier() `when` compact)
        +baseClass
    }, job) {

    private val controlElements: MutableList<HTMLButtonElement> = mutableListOf()
    private var inputElement: HTMLInputElement? = null
    private val optionsMenu: OptionsMenu<Int>

    init {
        markAs(ComponentType.Pagination)
        div(baseClass = "pagination".component("total-items")) {
            this@Pagination.pageInfoFlow.showRange().invoke(this)
        }
        optionsMenu = optionsMenu(closeOnSelect = true) {
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
            store.selects.unwrap() handledBy this@Pagination.pageInfoHandler.pageSize
        }
        nav(baseClass = "pagination".component("nav")) {
            if (!compact) {
                div(baseClass = classes("pagination".component("nav", "control"), "first".modifier())) {
                    this@Pagination.controlElements.add(pushButton(plain) {
                        aria["label"] = "Go to first page"
                        disabled(this@Pagination.pageInfoFlow.map { it.firstPage })
                        clicks handledBy this@Pagination.pageInfoHandler.gotoFirstPage
                        icon("angle-double-left".fas())
                    }.domNode)
                }
            }
            div(baseClass = classes("pagination".component("nav", "control"), "prev".modifier())) {
                this@Pagination.controlElements.add(pushButton(plain) {
                    aria["label"] = "Go to previous page"
                    disabled(this@Pagination.pageInfoFlow.map { it.firstPage })
                    clicks handledBy this@Pagination.pageInfoHandler.gotoPreviousPage
                    icon("angle-left".fas())
                }.domNode)
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
                this@Pagination.controlElements.add(pushButton(plain) {
                    aria["label"] = "Go to next page"
                    disabled(this@Pagination.pageInfoFlow.map { it.lastPage })
                    clicks handledBy this@Pagination.pageInfoHandler.gotoNextPage
                    icon("angle-right".fas())
                }.domNode)
            }
            if (!compact) {
                div(baseClass = classes("pagination".component("nav", "control"), "last".modifier())) {
                    this@Pagination.controlElements.add(pushButton(plain) {
                        aria["label"] = "Go to last page"
                        disabled(this@Pagination.pageInfoFlow.map { it.lastPage })
                        clicks handledBy this@Pagination.pageInfoHandler.gotoLastPage
                        icon("angle-double-right".fas())
                    }.domNode)
                }
            }
        }
    }

    public fun disabled(value: Boolean) {
        optionsMenu.disabled(value)
        controlElements.forEach { it.disabled = value }
        inputElement?.let { it.disabled = value }
    }

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

public fun Flow<PageInfo>.showRange(): Tag<HTMLElement>.() -> Unit = {
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

public interface PageInfoHandler {

    public val gotoFirstPage: Handler<Unit>
    public val gotoPreviousPage: Handler<Unit>
    public val gotoNextPage: Handler<Unit>
    public val gotoLastPage: Handler<Unit>
    public val gotoPage: Handler<Int>
    public val pageSize: Handler<Int>
    public val total: Handler<Int>
    public val refresh: Handler<Unit>
}

public class PageInfoStore(pageInfo: PageInfo) : RootStore<PageInfo>(pageInfo), PageInfoHandler {

    override val gotoFirstPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoFirstPage() }
    override val gotoPreviousPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoPreviousPage() }
    override val gotoNextPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoNextPage() }
    override val gotoLastPage: Handler<Unit> = handle { pageInfo -> pageInfo.gotoLastPage() }
    override val gotoPage: Handler<Int> = handle { pageInfo, page -> pageInfo.gotoPage(page) }
    override val pageSize: Handler<Int> = handle { pageInfo, pageSize -> pageInfo.pageSize(pageSize) }
    override val total: Handler<Int> = handle { pageInfo, total -> pageInfo.total(total) }
    override val refresh: Handler<Unit> = handle { pageInfo -> pageInfo.refresh() }
}
