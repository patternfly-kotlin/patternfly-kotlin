package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.TextElement
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

/**
 * Creates the [PageHeader] component inside the [Page] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Page.pageHeader(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeader.() -> Unit = {}
): PageHeader = register(PageHeader(this, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [PageHeaderTools] component inside the [PageHeader] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun PageHeader.pageHeaderTools(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeaderTools.() -> Unit = {}
): PageHeaderTools = register(PageHeaderTools(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [PageHeaderToolsGroup] component inside the [PageHeaderTools] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun PageHeaderTools.pageHeaderToolsGroup(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeaderToolsGroup.() -> Unit = {}
): PageHeaderToolsGroup = register(PageHeaderToolsGroup(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [PageHeaderToolsItem] component inside a [PageHeaderToolsGroup] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun PageHeaderToolsGroup.pageHeaderToolsItem(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeaderToolsItem.() -> Unit = {}
): PageHeaderToolsItem = register(PageHeaderToolsItem(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [PageHeaderToolsItem] component inside the [PageHeaderTools] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun PageHeaderTools.pageHeaderToolsItem(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeaderToolsItem.() -> Unit = {}
): PageHeaderToolsItem = register(PageHeaderToolsItem(id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * Page header or [masthead](https://www.patternfly.org/v4/components/page/design-guidelines/#masthead) component.
 */
public class PageHeader internal constructor(internal val page: Page, id: String?, baseClass: String?, job: Job) :
    PatternFlyElement<HTMLElement>,
    TextElement("header", id = id, baseClass = classes(ComponentType.PageHeader, baseClass), job, Scope()) {

    init {
        markAs(ComponentType.PageHeader)
        attr("role", "banner")
    }
}

/**
 * Page header tools component.
 */
public class PageHeaderTools internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("page".component("header", "tools"), baseClass), job, Scope())

/**
 * Page header tools group component.
 */
public class PageHeaderToolsGroup internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("page".component("header", "tools", "group"), baseClass), job, Scope())

/**
 * Page header tools item component.
 */
public class PageHeaderToolsItem internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("page".component("header", "tools", "item"), baseClass), job, Scope())
