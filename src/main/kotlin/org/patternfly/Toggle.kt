package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id
import org.w3c.dom.HTMLElement

internal sealed interface ToggleKind

internal class TextToggleKind(
    val title: String?,
    val variant: ButtonVariant?,
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
 * A basic toggle.
 */
@Suppress("TooManyFunctions")
public open class Toggle internal constructor(
    componentType: ComponentType,
    private val componentBaseClass: String,
    private var kind: ToggleKind,
    private val expandedStore: ExpandedStore
) {

    internal val id: String = Id.unique(componentType.id, "tgl")
    internal var disabled: Flow<Boolean> = flowOf(false)

    /**
     * A (plain) text toggle.
     */
    public fun text(
        title: String? = null,
        variant: ButtonVariant? = null,
        context: Span.() -> Unit = {}
    ) {
        kind = TextToggleKind(title = title, variant = variant, context = context)
    }

    /**
     * An icon toggle.
     */
    public fun icon(
        iconClass: String = "",
        baseClass: String? = null,
        id: String? = null,
        context: Icon.() -> Unit = {}
    ) {
        kind = IconToggleKind(iconClass = iconClass, baseClass = baseClass, id = id, context = context)
    }

    /**
     * An icon toggle with a predefined "kebab" icon.
     */
    public fun kebab(
        baseClass: String? = null,
        id: String? = null,
        context: Icon.() -> Unit = {}
    ) {
        icon(iconClass = "ellipsis-v".fas(), baseClass = baseClass, id = id, context = context)
    }

    /**
     * A badge toggle.
     */
    public fun badge(
        count: Int = 0,
        min: Int = Badge.BADGE_MIN,
        max: Int = Badge.BADGE_MAX,
        read: Boolean = false,
        baseClass: String? = null,
        id: String? = null,
        context: Badge.() -> Unit = {}
    ) {
        kind = BadgeToggleKind(
            count = count,
            min = min,
            max = max,
            read = read,
            baseClass = baseClass,
            id = id,
            context = context
        )
    }

    /**
     * A checkbox toggle.
     */
    public fun checkbox(
        title: String? = null,
        baseClass: String? = null,
        id: String? = null,
        context: Input.() -> Unit = {}
    ) {
        kind = CheckboxToggleKind(title = title, baseClass = baseClass, id = id, context = context)
    }

    /**
     * An action toggle.
     */
    public fun action(
        title: String? = null,
        variant: ButtonVariant? = null,
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit = {}
    ) {
        kind = ActionToggleKind(title = title, variant = variant, baseClass = baseClass, id = id, context)
    }

    /**
     * An image toggle.
     */
    public fun img(
        title: String = "",
        src: String = "",
        baseClass: String? = null,
        id: String? = null,
        context: Img.() -> Unit = {}
    ) {
        kind = ImageToggleKind(title = title, src = src, baseClass = baseClass, id = id, context = context)
    }

    internal fun render(tag: Tag<HTMLElement>) {
        when (val immutableKind = kind) {
            is TextToggleKind -> renderTextToggle(tag, immutableKind)
            is IconToggleKind -> renderIconToggle(tag, immutableKind)
            is BadgeToggleKind -> renderBadgeToggle(tag, immutableKind)
            is CheckboxToggleKind -> renderCheckboxToggle(tag, immutableKind)
            is ActionToggleKind -> renderActionToggle(tag, immutableKind)
            is ImageToggleKind -> renderImageToggle(tag, immutableKind)
        }
    }

    internal open fun renderTextToggle(tag: Tag<HTMLElement>, kind: TextToggleKind) {
        with(tag) {
            button(
                baseClass = classes {
                    +componentBaseClass.component("toggle")
                    +("text".modifier() `when` (kind.variant == ButtonVariant.plain))
                    +kind.variant?.modifier
                }
            ) {
                setupToggleButton(this)
                span(baseClass = componentBaseClass.component("toggle", "text")) {
                    kind.title?.let { +it }
                    kind.context(this)
                }
                span(baseClass = componentBaseClass.component("toggle", "icon")) {
                    icon("caret-down".fas())
                }
            }
        }
    }

    internal open fun renderIconToggle(tag: Tag<HTMLElement>, kind: IconToggleKind) {
        with(tag) {
            button(
                baseClass = classes(
                    componentBaseClass.component("toggle"),
                    "plain".modifier()
                )
            ) {
                setupToggleButton(this)
                icon(
                    iconClass = kind.iconClass,
                    baseClass = kind.baseClass,
                    id = kind.id,
                    context = kind.context
                )
            }
        }
    }

    internal open fun renderBadgeToggle(tag: Tag<HTMLElement>, kind: BadgeToggleKind) {
        notImplemented(tag, kind)
    }

    internal open fun renderCheckboxToggle(tag: Tag<HTMLElement>, kind: CheckboxToggleKind) {
        notImplemented(tag, kind)
    }

    internal open fun renderActionToggle(tag: Tag<HTMLElement>, kind: ActionToggleKind) {
        notImplemented(tag, kind)
    }

    internal open fun renderImageToggle(tag: Tag<HTMLElement>, kind: ImageToggleKind) {
        notImplemented(tag, kind)
    }

    internal open fun setupToggleButton(button: Button) {
        with(button) {
            disabled(disabled)
            domNode.id = this@Toggle.id
            aria["haspopup"] = true
            aria["expanded"] = expandedStore.data.map { it.toString() }
            clicks handledBy expandedStore.toggle
        }
    }

    private fun notImplemented(tag: Tag<HTMLElement>, kind: ToggleKind) {
        with(tag) {
            val message = "$kind not implemented for toggle with id ${this@Toggle.id}"
            !message
            console.warn(message)
        }
    }
}
