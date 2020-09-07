package org.patternfly

import dev.fritz2.dom.WithDomNode
import kotlinx.browser.window
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
