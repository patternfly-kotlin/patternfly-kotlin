package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import kotlinx.browser.window
import org.patternfly.dom.By
import org.patternfly.dom.querySelector
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode
import org.w3c.dom.get
import org.w3c.dom.set

private const val COMPONENT_TYPE: String = "pfct"

internal fun Tag<HTMLElement>.markAs(componentType: ComponentType) {
    domNode.dataset[COMPONENT_TYPE] = componentType.id
    if (window.localStorage["ouia"].toString() == "true") {
        domNode.dataset["ouiaComponentType"] = componentType.name
    }
}

internal fun ParentNode.querySelector(componentType: ComponentType): Element? = this.querySelector(
    By.data(COMPONENT_TYPE, componentType.id)
)

@Suppress("EnumNaming")
internal enum class ComponentType(val id: String, internal val baseClass: String? = null) {
    Accordion("ac", "accordion".component()),
    Alert("at", "alert".component()),
    AlertGroup("ag", "alert-group".component()),
    Avatar("av", "avatar".component()),
    Badge("bdg", "badge".component()),
    Breadcrumb("bc", "breadcrumb".component()),
    Button("btn", "button".component()),
    Card("crd", "card".component()),
    CardView("cv"),
    Chip("chp", "chip".component()),
    ChipGroup("cpg", "chip-group".component()),
    ContextSelector("cs", "context-selector".component()),
    DataList("dl", "data-list".component()),
    DataTable("dt", "table".component()),
    Drawer("dw", "drawer".component()),
    Dropdown("dd", "dropdown".component()),
    EmptyState("es", "empty-state".component()),
    Form("frm", "form".component()),
    Icon("icn"),
    InputGroup("ig", "input-group".component()),
    Menu("mu", "menu".component()),
    Navigation("nav", "nav".component()),
    NotificationBadge("nb", "button".component()),
    OptionsMenu("opt", "options-menu".component()),
    Page("pg", "page".component()),
    PageHeader("pgh", "page".component("header")),
    PageMain("mn", "page".component("main")),
    PageSidebar("pgs", "page".component("sidebar")),
    Pagination("pgn", "pagination".component()),
    Select("sel", "select".component()),
    Skeleton("sk", "skeleton".component()),
    Spinner("sp", "spinner".component()),
    Switch("sw", "switch".component()),
    Tabs("tbs"),
    TextContent("tc", "content".component()),
    Title("tlt", "title".component()),
    Toolbar("tb", "toolbar".component()),
    TreeView("tv", "tree-view".component());
}

/**
 * Marker interface for PatternFly components.
 */
internal interface PatternFlyComponent<T> {
    fun render(
        context: RenderContext,
        baseClass: String?,
        id: String?,
    ): T
}
