package org.patternfly

import dev.fritz2.dom.TextNode
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.WithText
import dev.fritz2.dom.mountDomNode
import dev.fritz2.lenses.IdProvider
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.patternfly.Size.LG
import org.patternfly.Size.MD
import org.patternfly.Size.XL
import org.patternfly.Size.XL_2
import org.patternfly.dom.By
import org.patternfly.dom.querySelector
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.ParentNode
import org.w3c.dom.get
import org.w3c.dom.set

private const val COMPONENT_TYPE: String = "pfct"

/**
 * Generic display function for components.
 *
 * @param C the component type such as [Button][dev.fritz2.dom.html.Button], [Div][dev.fritz2.dom.html.Div] or [Td][dev.fritz2.dom.html.Td]
 * @param T the payload type which should be rendered inside the component type
 */
public typealias ComponentDisplay<C, T> = C.(T) -> Unit

// ------------------------------------------------------ types

internal fun <E : HTMLElement> PatternFlyComponent<E>.markAs(componentType: ComponentType) {
    domNode.dataset[COMPONENT_TYPE] = componentType.id
    if (window.localStorage["ouia"].toString() == "true") {
        domNode.dataset["ouiaComponentType"] = componentType.name
    }
}

/**
 * Marker interface implemented by all PatternFly components.
 */
public interface PatternFlyComponent<out E : HTMLElement> : WithDomNode<E>

/**
 * Interface meant to be implemented by components which want to have an easy access to an item ID based on [IdProvider]. These components can for example use [itemId] to set the ID attribute on their DOM element.
 *
 * This interface is implemented by most of the components which are part of [CardView], [DataList] and [DataTable]. These implementations use the ID provider of the [ItemsStore]: [ItemsStore.idProvider].
 *
 * @sample org.patternfly.sample.WithIdProviderSample.useItemId
 */
public interface WithIdProvider<T> {

    /**
     * The [IdProvider] used by this interface.
     */
    public val idProvider: IdProvider<T, String>

    /**
     * Provides an easy access to the item ID. Shortcut for `idProvider.invoke(item)`.
     */
    public fun itemId(item: T): String = idProvider(item)
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
    DataList("dl", "data-list".component()),
    DataTable("dt", "table".component()),
    Drawer("dw", "drawer".component()),
    Dropdown("dd", "dropdown".component()),
    EmptyState("es", "empty-state".component()),
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
 * Alignment modifier for [Dropdown]s and [OptionsMenu]s.
 */
public enum class Align(public val modifier: String) {
    LEFT("align-left".modifier()), RIGHT("align-right".modifier())
}

/**
 * Visual modifiers for [PushButton]s and [LinkButton]s.
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class ButtonVariation(internal val modifier: String) {
    control("control".modifier()),
    danger("danger".modifier()),
    `inline`("inline".modifier()),
    link("link".modifier()),
    plain("plain".modifier()),
    primary("primary".modifier()),
    secondary("secondary".modifier()),
    tertiary("tertiary".modifier()),
    warning("warning".modifier()),
}

/**
 * Enum for the [DataTable] selection mode.
 */
public enum class DataTableSelection {

    /**
     * Only one row can be selected at a time using radio buttons.
     */
    SINGLE,

    /**
     * Multiple rows can be selected using checkboxes.
     */
    MULTIPLE,

    /**
     * Multiple rows can be selected using checkboxes. The table header contains an additional checkbox to select all rows.
     */
    MULTIPLE_ALL
}

/**
 * Visual modifier for [divider]s.
 */
public enum class DividerVariant {
    HR, DIV, LI
}

/**
 * Modifiers for the [DrawerPanel] position.
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class DrawerPanelPosition(internal val modifier: String) {
    LEFT("panel-left".modifier()),
    RIGHT(""),
    BOTTOM("panel-bottom".modifier()),
}

/**
 * Heading level used for the [Title] component.
 */
@Suppress("MagicNumber")
public enum class Level(public val level: Int, public val size: Size) {
    H1(1, XL_2),
    H2(2, XL),
    H3(3, LG),
    H4(4, MD),
    H5(5, MD),
    H6(6, MD),
}

/**
 * Flag used for [Navigation] component.
 */
public enum class Orientation {
    HORIZONTAL, VERTICAL
}

/**
 * Enum used in [buttonIcon] to specify the position of the icon in buttons when used together with text.
 */
public enum class IconPosition(public val modifier: String) {
    ICON_FIRST("start".modifier()),
    ICON_LAST("end".modifier())
}

/**
 * Enum for the level in [Alert]s and [Notification]s.
 */
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

/**
 * Size modifier used in various components.
 */
public enum class Size(public val modifier: String) {
    XL_4("4xl".modifier()),
    XL_3("3xl".modifier()),
    XL_2("2xl".modifier()),
    XL("xl".modifier()),
    LG("lg".modifier()),
    MD("md".modifier()),
    SM("sm".modifier()),
    XS("xs".modifier())
}

/**
 * Sticky modifier for [Page] components.
 */
public enum class Sticky(public val modifier: String) {
    TOP("sticky-top".modifier()),
    BOTTOM("sticky-bottom".modifier())
}

/**
 * Enum for the checkbox state including the [ideterminate](http://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/checkbox#indeterminate) state.
 */
public enum class TriState(internal val checked: Boolean, internal val indeterminate: Boolean) {
    OFF(false, false),
    INDETERMINATE(false, true),
    ON(true, false)
}

/**
 * Width modifier for [Skeleton] components.
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class Width(public val modifier: String) {
    SM("sm".width()),
    MD("md".width()),
    LG("lg".width()),
    _25("25".width()),
    _33("33".width()),
    _50("50".width()),
    _66("66".width()),
    _75("75".width()),
}

/**
 * Width modifier for [Skeleton] components.
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class Height(public val modifier: String) {
    SM("sm".width()),
    MD("md".width()),
    LG("lg".width()),
    _25("25".height()),
    _33("33".height()),
    _50("50".height()),
    _66("66".height()),
    _75("75".height()),
    _100("100".height()),
}

/**
 * FontSize modifier for [Skeleton] components.
 */
public enum class FontSize(public val modifier: String) {
    XL_4("4xl".text()),
    XL_3("3xl".text()),
    XL_2("2xl".text()),
    XL("xl".text()),
    LG("lg".text()),
    MD("md".text()),
    SM("sm".text()),
    XS("xs".text())
}

/**
 * Shape modifier for [Skeleton] components.
 */
public enum class Shape(public val modifier: String) {
    CIRCLE("circle".modifier()),
    SQUARE("square".modifier()),
}
