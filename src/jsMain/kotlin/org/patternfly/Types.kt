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
import org.patternfly.Modifier._2xl
import org.patternfly.Modifier._3xl
import org.patternfly.Modifier._4xl
import org.patternfly.Modifier.alignLeft
import org.patternfly.Modifier.alignRight
import org.patternfly.Modifier.end
import org.patternfly.Modifier.lg
import org.patternfly.Modifier.md
import org.patternfly.Modifier.start
import org.patternfly.Modifier.xl
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.get
import org.w3c.dom.set

// ------------------------------------------------------ types

const val COMPONENT_TYPE = "pfct"

typealias AsText<T> = (T) -> String

typealias ComponentDisplay<C, T> = (T) -> C.() -> Unit

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

enum class ComponentType(val id: String, internal val baseClass: String? = null) {
    Alert("at", "alert".component()),
    AlertGroup("ag", "alert-group".component()),
    Badge("bdg", "badge".component()),
    Brand("brd", "brand".component()),
    Button("btn", "button".component()),
    Card("crd", "card".component()),
    Chip("chp", "chip".component()),
    ChipGroup("cpg", "chip-group".component()),
    Content("cnt", "content".component()),
    DataList("dl", "data-list".component()),
    Drawer("dw", "drawer".component()),
    Dropdown("dd", "dropdown".component()),
    EmptyState("es", "empty-state".component()),
    Header("hdr", "page".component("header")),
    Icon("icn"),
    Main("mn", "page".component("main")),
    Navigation("nav", "nav".component()),
    NotificationBadge("nb", "button".component()),
    OptionsMenu("opt", "options-menu".component()),
    Page("pg", "page".component()),
    Pagination("pgn", "pagination".component()),
    Section("se", "page".component("main-section")),
    Select("sel", "select".component()),
    Sidebar("sb", "page".component("sidebar")),
    Switch("sw", "switch".component()),
    Title("tlt", "title".component());
}

enum class Align(val modifier: Modifier) {
    LEFT(alignLeft), RIGHT(alignRight)
}

enum class DividerVariant {
    HR, DIV, LI
}

enum class ExpansionMode {
    SINGLE, MULTI
}

enum class Orientation {
    HORIZONTAL, VERTICAL
}

enum class Position(val modifier: Modifier) {
    START(start), END(end)
}

enum class SelectionMode {
    NONE, SINGLE, MULTIPLE
}

enum class Severity(
    val modifier: Modifier?,
    val iconClass: String,
    val aria: String
) {
    DEFAULT(null, "bell".fas(), "Default alert"),
    INFO(Modifier.info, "info-circle".fas(), "Info alert"),
    SUCCESS(Modifier.success, "check-circle".fas(), "Success alert"),
    WARNING(Modifier.warning, "exclamation-triangle".fas(), "Warning alert"),
    DANGER(Modifier.danger, "exclamation-circle".fas(), "Danger alert");
}

enum class Size(val modifier: Modifier) {
    XL_4(_4xl),
    XL_3(_3xl),
    XL_2(_2xl),
    XL(xl),
    LG(lg),
    MD(md)
}
