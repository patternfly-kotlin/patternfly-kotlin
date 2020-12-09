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
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
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

@Deprecated("Please use ComponentDisplay", replaceWith = ReplaceWith("ComponentDisplay"))
public typealias OldComponentDisplay<C, T> = (T) -> C.() -> Unit

// ------------------------------------------------------ types

internal fun <E : HTMLElement> PatternFlyComponent<E>.markAs(componentType: ComponentType) {
    domNode.dataset[COMPONENT_TYPE] = componentType.id
    if (window.localStorage["ouia"].toString() == "true") {
        domNode.dataset["ouiaComponentType"] = componentType.name
    }
}

internal interface PatternFlyComponent<out E : HTMLElement> : WithDomNode<E>

/**
 * Interface meant to be implemented by components which want to have an easy access to an item ID based on [IdProvider]. These components can for example use [itemId] to set the ID attribute on their DOM element.
 *
 * This interface is implemented by most of the components which are part of [CardView], [DataList] and [DataTable]. These implementations use the ID provider of the [ItemStore]: [ItemStore.idProvider].
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

internal enum class ComponentType(val id: String, internal val baseClass: String? = null) {
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

/**
 * Alignment modifier for [Dropdown]s and [OptionsMenu]s.
 */
public enum class Align(public val modifier: String) {
    LEFT("align-left".modifier()), RIGHT("align-right".modifier())
}

/**
 * Enum for the [DataTable] selection mode.
 */
public enum class DataTableSelection {

    /**
     * Multiple rows can be selected using checkboxes. The table header contains an additional checkbox to select all rows.
     */
    MULTIPLE_ALL,

    /**
     * Multiple rows can be selected using checkboxes.
     */
    MULTIPLE,

    /**
     * Only one row can be selected at a time using radio buttons.
     */
    SINGLE
}

/**
 * Visual modifiers for [PushButton]s and [LinkButton]s.
 */
@Suppress("EnumEntryName")
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
 * Visual modifier for [divider]s.
 */
public enum class DividerVariant {
    HR, DIV, LI
}

/**
 * Flag used for [Navigation] component.
 */
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
    MD("md".modifier())
}

/**
 * Enum for the checkbox state including the [ideterminate](http://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/checkbox#indeterminate) state.
 */
public enum class TriState(internal val checked: Boolean, internal val indeterminate: Boolean) {
    OFF(false, false),
    INDETERMINATE(false, true),
    ON(true, false)
}
