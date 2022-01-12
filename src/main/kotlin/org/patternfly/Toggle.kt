package org.patternfly

import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.Comment
import org.w3c.dom.Node

internal sealed interface ToggleKind

internal class TextToggleKind(
    val title: String?,
    val variant: ButtonVariant?,
    val context: Span.() -> Unit
) : ToggleKind

internal class PlainTextToggleKind(
    val title: String?,
    val context: Span.() -> Unit
) : ToggleKind

internal class IconToggleKind(
    val iconClass: String,
    val baseClass: String?,
    val id: String?,
    val context: Icon.() -> Unit
) : ToggleKind

internal class BadgeToggleKind(
    val count: Int,
    val min: Int,
    val max: Int,
    val read: Boolean,
    val baseClass: String?,
    val id: String?,
    val context: Badge.() -> Unit
) : ToggleKind

internal class DropdownBadge(kind: BadgeToggleKind) : Badge(
    count = kind.count,
    min = kind.min,
    max = kind.max,
    read = kind.read
) {
    override fun tail(context: RenderContext) {
        with(context) {
            span(baseClass = "dropdown".component("toggle", "icon")) {
                icon("caret-down".fas())
            }
        }
    }
}

internal class CheckboxToggleKind(
    val title: String?,
    val baseClass: String?,
    val id: String?,
    val context: Input.() -> Unit
) : ToggleKind

internal class ActionToggleKind(
    val title: String?,
    val variant: ButtonVariant?,
    val baseClass: String?,
    val id: String?,
    val context: Button.() -> Unit
) : ToggleKind

internal class ImageToggleKind(
    val title: String,
    val src: String,
    val baseClass: String?,
    val id: String?,
    val context: Img.() -> Unit
) : ToggleKind

/**
 * Common interface for [Dropdown] and [OptionsMenu] toggle variants.
 */
@Deprecated("Deprecated API")
public interface Toggle<T, out N : Node> : WithDomNode<N> {

    /**
     * Disables or enables the toggle.
     */
    public fun disabled(value: Boolean)

    /**
     * Disables or enables the toggle based on the values from the flow.
     */
    public fun disabled(value: Flow<Boolean>)
}

@Deprecated("Deprecated API")
internal class RecordingToggle<T> : Toggle<T, Comment> {

    private var recordedBoolean: Boolean? = null
    private var recordedFlow: Flow<Boolean>? = null
    override val domNode: Comment = document.createComment("noop toggle")

    override fun disabled(value: Boolean) {
        recordedBoolean = value
    }

    override fun disabled(value: Flow<Boolean>) {
        recordedFlow = value
    }

    internal fun <N : Node> playback(toggle: Toggle<T, N>) {
        recordedBoolean?.let { toggle.disabled(it) }
        recordedFlow?.let { toggle.disabled(it) }
    }
}
