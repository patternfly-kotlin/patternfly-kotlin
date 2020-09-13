package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfPagination(classes: String? = null, content: Pagination.() -> Unit = {}): Pagination =
    register(Pagination(classes), content)

// ------------------------------------------------------ tag

class Pagination internal constructor(classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes(ComponentType.Pagination, classes)) {

    val store: PageInfoStore = PageInfoStore()

    init {
        markAs(ComponentType.Pagination)
        div {

        }
    }
}

// ------------------------------------------------------ store

class PageInfoStore : RootStore<PageInfo>(PageInfo(DEFAULT_PAGE_SIZE, 0, 0))
