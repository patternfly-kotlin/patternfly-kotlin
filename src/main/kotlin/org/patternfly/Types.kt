package org.patternfly

import dev.fritz2.binding.SingleMountPoint
import dev.fritz2.dom.DomMountPoint
import dev.fritz2.dom.DomMountPointPreserveOrder
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.WithText
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.get
import org.w3c.dom.set

public const val COMPONENT_TYPE: String = "pfct"

public typealias ComponentDisplay<C, T> = (T) -> C.() -> Unit

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
@Deprecated("Find a replacement!")
internal interface WithTextDelegate<E : HTMLElement, D : HTMLElement> : WithText<E> {

    override fun text(value: String): Node = appendText(value)

    override operator fun String.unaryPlus(): Node = appendText(this)

    override fun Flow<String>.bind(preserveOrder: Boolean): SingleMountPoint<WithDomNode<Text>> {
        val upstream = this.map {
            TextNode(it)
        }.distinctUntilChanged()

        return if (preserveOrder) DomMountPointPreserveOrder(upstream, delegate())
        else DomMountPoint(upstream, delegate())
    }

    fun delegate(): D

    fun appendText(text: String): Node = delegate().appendChild(TextNode(text).domNode)
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
    Content("cnt", "content".component()),
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
