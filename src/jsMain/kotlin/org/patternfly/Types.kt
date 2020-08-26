package org.patternfly

import org.patternfly.Modifier._2xl
import org.patternfly.Modifier._3xl
import org.patternfly.Modifier._4xl
import org.patternfly.Modifier.end
import org.patternfly.Modifier.left
import org.patternfly.Modifier.lg
import org.patternfly.Modifier.md
import org.patternfly.Modifier.right
import org.patternfly.Modifier.start
import org.patternfly.Modifier.xl

typealias AsText<T> = (T) -> String

enum class ComponentType(val id: String) {
    Accordion("acc"),
    Alert("at"),
    AlertGroup("ag"),
    Badge("bdg"),
    Button("btn"),
    Content("cnt"),
    DataList("dl"),
    Drawer("dw"),
    Dropdown("dd"),
    EmptyState("es"),
    Header("hdr"),
    Icon("icn"),
    Main("mn"),
    Navigation("nav"),
    NotificationBadge("nb"),
    NotificationDrawer("nd"),
    Page("pg"),
    Section("se"),
    Sidebar("sb"),
    Switch("sw");
}

enum class Align(val modifier: Modifier) {
    LEFT(left), RIGHT(right)
}

enum class Direction {
    RIGHT, UP
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
