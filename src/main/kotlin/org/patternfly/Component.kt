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

@DslMarker
public annotation class ComponentMarker

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
    ActionList("al", "action-list".component()),
    Alert("at", "alert".component()),
    AlertGroup("ag", "alert-group".component()),
    Avatar("av", "avatar".component()),
    Badge("bdg", "badge".component()),
    Brand("bnd", "brand".component()),
    Breadcrumb("bc", "breadcrumb".component()),
    Button("btn", "button".component()),
    Card("crd", "card".component()),
    CardView("cv"),
    Checkbox("chb", "check".component()),
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
    Label("lbl", "label".component()),
    LabelGroup("lbg", "label-group".component()),
    List("lst", "list".component()),
    Masthead("mh", "masthead".component()),
    Menu("mu", "menu".component()),
    Navigation("nav", "nav".component()),
    NotificationBadge("nb", "button".component()),
    OptionsMenu("opt", "options-menu".component()),
    Page("pg", "page".component()),
    Pagination("pgn", "pagination".component()),
    Select("sel", "select".component()),
    Sidebar("sb", "page".component("sidebar")),
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
@ComponentMarker
internal interface PatternFlyComponent<T> {
    fun render(
        context: RenderContext,
        baseClass: String?,
        id: String?,
    ): T
}

/**
 * Helper class to compose nested components inside [PatternFlyComponent]s.
 */
public open class SubComponent<T>(
    internal val baseClass: String?,
    internal val id: String?,
    internal val context: T.() -> Unit
)
