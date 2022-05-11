package org.patternfly

import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.WithText
import dev.fritz2.lenses.IdProvider
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

/**
 * Generic display function for components.
 *
 * @param C the component type such as [Button][dev.fritz2.dom.html.Button], [Div][dev.fritz2.dom.html.Div] or [Td][dev.fritz2.dom.html.Td]
 * @param T the payload type which should be rendered inside the component type
 */
@Deprecated("Should no longer be necessary when using PatternFlyComponent<T>")
public typealias ComponentDisplay<C, T> = C.(T) -> Unit

// ------------------------------------------------------ types

/**
 * Marker interface implemented by all PatternFly components.
 */
@Deprecated("Replace with PatternFlyComponent<T>")
public interface PatternFlyElement<out E : HTMLElement> : WithDomNode<E>

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

/**
 * Interface meant to be implemented by components which want to overwrite the default [String.unaryPlus] implementation.
 *
 * Note about the current implementation:
 *  It delegates the text to the provided element.
 *  Be aware that the text will be appended directly to the root of the provided Element.
 *
 * @param E component root Element
 * @param D delegated root Element
 */
internal interface WithTextDelegate<E : HTMLElement, D : HTMLElement> : WithText<E> {

    fun Flow<String>.asText() {
//        mountDomNode(job, delegate(), this.map { TextNode(it) })
    }

    fun <T> Flow<T>.asText() {
//        mountDomNode(job, delegate(), this.map { TextNode(it.toString()) })
    }

    override operator fun String.unaryPlus(): Node = delegate().appendChild(document.createTextNode(this))

    /**
     * Provide the desired Element
     */
    fun delegate(): D
}

// ------------------------------------------------------ enums (a-z)

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
 * Flag used for various components.
 */
public enum class Orientation {
    HORIZONTAL, VERTICAL
}

/**
 * Enum for the level in [Alert]s and [NotificationAlert]s.
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
