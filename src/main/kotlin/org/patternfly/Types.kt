package org.patternfly

import dev.fritz2.dom.TextNode
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.WithText
import dev.fritz2.dom.mountDomNode
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.get
import org.w3c.dom.set

public const val COMPONENT_TYPE: String = "pfct"

public typealias ComponentDisplay<C, T> = (T) -> C.() -> Unit
public typealias ComponentDisplay2<C, T> = C.(T) -> Unit

// ------------------------------------------------------ types

internal interface PatternFlyComponent<out E : HTMLElement> : WithDomNode<E> {

    fun markAs(componentType: ComponentType) {
        domNode.dataset[COMPONENT_TYPE] = componentType.id
        if (window.localStorage["ouia"].toString() == "true") {
            domNode.dataset["ouiaComponentType"] = componentType.name
        }
    }
}

// Delegates the text related methods to another element
internal interface WithTextDelegate<E : HTMLElement, D : HTMLElement> : WithText<E> {

    override fun Flow<String>.asText() {
        mountDomNode(job, delegate(), this.map { TextNode(it) })
    }

    override fun <T> Flow<T>.asText() {
        mountDomNode(job, delegate(), this.map { TextNode(it.toString()) })
    }

    override operator fun String.unaryPlus(): Node = delegate().appendChild(document.createTextNode(this))

    fun delegate(): D
}

// ------------------------------------------------------ enums

public enum class ComponentType(public val id: String, internal val baseClass: String? = null) {
    Alert("at", "alert".component()),
    AlertGroup("ag", "alert-group".component()),
    Badge("bdg", "badge".component()),
    Button("btn", "button".component()),
    Card("crd", "card".component()),
    CardView("cv"),
    Chip("chp", "chip".component()),
    ChipGroup("cpg", "chip-group".component()),
    DataList("dl", "data-list".component()),
    DataTable("dt", "table".component()),
    Drawer("dw", "drawer".component()),
    Dropdown("dd", "dropdown".component()),
    EmptyState("es", "empty-state".component()),
    Icon("icn"),
    InputGroup("ig", "input-group".component()),
    Main("mn", "page".component("main")),
    Navigation("nav", "nav".component()),
    NotificationBadge("nb", "button".component()),
    OptionsMenu("opt", "options-menu".component()),
    Page("pg", "page".component()),
    PageHeader("pgh", "page".component("header")),
    PageSidebar("pgs", "page".component("sidebar")),
    Pagination("pgn", "pagination".component()),
    Section("se", "page".component("main-section")),
    Select("sel", "select".component()),
    Switch("sw", "switch".component()),
    Tabs("tbs"),
    Title("tlt", "title".component()),
    Toolbar("tb", "toolbar".component()),
    TreeView("tv", "tree-view".component());
}

public enum class Align(public val modifier: String) {
    LEFT("align-left".modifier()), RIGHT("align-right".modifier())
}

public enum class ButtonVariation(internal val modifier: String) {
    control("control".modifier()),
    danger("danger".modifier()),
    `inline`("inline".modifier()),
    link("link".modifier()),
    plain("plain".modifier()),
    primary("primariy".modifier()),
    secondary("secondary".modifier()),
    tertiary("tertiary".modifier()),
    warning("warning".modifier()),
}

public enum class DividerVariant {
    HR, DIV, LI
}

public enum class ExpansionMode {
    SINGLE, MULTI
}

public enum class Orientation {
    HORIZONTAL, VERTICAL
}

public enum class IconPosition(public val modifier: String) {
    ICON_FIRST("start".modifier()),
    ICON_LAST("end".modifier())
}

public enum class SelectionMode {
    NONE, SINGLE, MULTIPLE
}

public enum class Severity(
    public val modifier: String?,
    public val iconClass: String,
) {
    DEFAULT(null, "bell".fas()),
    INFO("info".modifier(), "info-circle".fas()),
    SUCCESS("success".modifier(), "check-circle".fas()),
    WARNING("warning".modifier(), "exclamation-triangle".fas()),
    DANGER("danger".modifier(), "exclamation-circle".fas());
}

public enum class Size(public val modifier: String) {
    XL_4("4xl".modifier()),
    XL_3("3xl".modifier()),
    XL_2("2xl".modifier()),
    XL("xl".modifier()),
    LG("lg".modifier()),
    MD("md".modifier())
}

public enum class TriState { OFF, INDETERMINATE, ON }
