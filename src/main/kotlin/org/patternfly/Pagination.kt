package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.valuesAsNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariant.plain
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ factory

/**
 * Creates a new [Pagination] component.
 *
 * @param pageInfo the [PageInfo] instance
 * @param pageSizes the size of one page
 * @param compact whether to use a compact layout
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.pagination(
    pageInfo: PageInfo,
    pageSizes: IntArray = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Pagination.() -> Unit = {}
) {
    val store = PageInfoStore(pageInfo)
    Pagination(store, store.data, pageSizes, compact).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [pagination](https://www.patternfly.org/v4/components/pagination/design-guidelines) component.
 *
 * A pagination component gives users more navigational capability on pages with content views.
 *
 * @sample org.patternfly.sample.PaginationSample.pageInfo
 */
public open class Pagination(
    public val pageInfoHandler: PageInfoHandler,
    public val pageInfoFlow: Flow<PageInfo>,
    private val pageSizes: IntArray,
    private val compact: Boolean
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var disabled: Flow<Boolean> = flowOf(false)

    /**
     * Disables the component.
     */
    public fun disabled(value: Boolean) {
        disabled = flowOf(value)
    }

    /**
     * Disables the component based on the values in the specified [Flow].
     */
    public fun disabled(value: Flow<Boolean>) {
        disabled = value
    }

    @Suppress("LongMethod")
    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(
                baseClass = classes {
                    +ComponentType.Pagination
                    +("compact".modifier() `when` compact)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Pagination)
                applyElement(this)
                applyEvents(this)

                // to avoid having this@Pageination all over the place
                val dis = disabled
                val pif = pageInfoFlow
                val pih = pageInfoHandler
                val ps = pageSizes

                div(baseClass = "pagination".component("total-items")) {
                    pif.showRange().invoke(this)
                }
                optionsMenu(closeOnSelect = true) {
                    disabled(dis)
                    toggle {
                        text(variant = plain) {
                            pif.showRange().invoke(this)
                        }
                    }
                    ps.forEach { pageSize ->
                        item(pageSize.toString()) {
                            selected(pif.map { it.pageSize == pageSize })
                            events {
                                clicks.map { pageSize } handledBy pih.pageSize
                            }
                        }
                    }
                }
                nav(baseClass = "pagination".component("nav")) {
                    if (!compact) {
                        div(baseClass = classes("pagination".component("nav", "control"), "first".modifier())) {
                            pushButton(plain) {
                                aria["label"] = "Go to first page"
                                disabled(
                                    pif.map { it.firstPage }.combine(dis) { firstPage, disabled ->
                                        firstPage || disabled
                                    }
                                )
                                clicks handledBy pih.gotoFirstPage
                                icon("angle-double-left".fas())
                            }
                        }
                    }
                    div(baseClass = classes("pagination".component("nav", "control"), "prev".modifier())) {
                        pushButton(plain) {
                            aria["label"] = "Go to previous page"
                            disabled(
                                pif.map { it.firstPage }.combine(dis) { firstPage, disabled ->
                                    firstPage || disabled
                                }
                            )
                            clicks handledBy pih.gotoPreviousPage
                            icon("angle-left".fas())
                        }
                    }
                    if (!compact) {
                        div(baseClass = "pagination".component("nav", "page-select")) {
                            input(baseClass = "form-control".component()) {
                                aria["label"] = "Current page"
                                type("number")
                                min("1")
                                max(pif.map { it.pages.toString() })
                                disabled(
                                    pif.map { it.pages < 2 }.combine(dis) { onePage, disabled ->
                                        onePage || disabled
                                    }
                                )
                                value(pif.map { (if (it.total == 0) 0 else it.page + 1).toString() })
                                changes.valuesAsNumber()
                                    .map { it.toInt() - 1 } handledBy pih.gotoPage
                            }
                            span {
                                aria["hidden"] = true
                                +"of "
                                pif.map {
                                    if (it.total == 0) "0" else it.pages.toString()
                                }.asText()
                            }
                        }
                    }
                    div(baseClass = classes("pagination".component("nav", "control"), "next".modifier())) {
                        pushButton(plain) {
                            aria["label"] = "Go to next page"
                            disabled(
                                pif.map { it.lastPage }.combine(dis) { lastPage, disabled ->
                                    lastPage || disabled
                                }
                            )
                            clicks handledBy pih.gotoNextPage
                            icon("angle-right".fas())
                        }
                    }
                    if (!compact) {
                        div(baseClass = classes("pagination".component("nav", "control"), "last".modifier())) {
                            pushButton(plain) {
                                aria["label"] = "Go to last page"
                                disabled(
                                    pif.map { it.lastPage }.combine(dis) { lastPage, disabled ->
                                        lastPage || disabled
                                    }
                                )
                                clicks handledBy pih.gotoLastPage
                                icon("angle-double-right".fas())
                            }
                        }
                    }
                }
            }
        }
    }
}

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
