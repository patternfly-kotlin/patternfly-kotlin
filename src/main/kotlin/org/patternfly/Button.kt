package org.patternfly

import dev.fritz2.binding.mountSimple
import dev.fritz2.dom.DomListener
import dev.fritz2.dom.Listener
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonType.BUTTON
import org.patternfly.ButtonType.INLINE_LINK
import org.patternfly.ButtonType.LINK
import org.patternfly.IconAndTitle.ICON_FIRST
import org.patternfly.IconAndTitle.ICON_LAST
import org.patternfly.IconAndTitle.ICON_ONLY
import org.patternfly.IconAndTitle.TITLE_ONLY
import org.patternfly.IconAndTitle.UNDEFINED
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ factory

/**
 * Creates a [Button] component backed by a `<button/>` element.
 *
 * @param variants controls the visual representation of the button
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.ButtonSample.pushButton
 */
public fun RenderContext.pushButton(
    vararg variants: ButtonVariant,
    baseClass: String? = null,
    id: String? = null,
    context: Button.() -> Unit = {}
) {
    Button(BUTTON, variants).apply(context).render(this, baseClass, id)
}

/**
 * Creates a [Button] component backed by a `<button/>` element and returns a [Listener] (basically a [Flow]) in order to combine the button declaration directly to a fitting handler.
 *
 * @param variants controls the visual representation of the button
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.ButtonSample.clickButton
 */
public fun RenderContext.clickButton(
    vararg variants: ButtonVariant,
    baseClass: String? = null,
    id: String? = null,
    context: Button.() -> Unit = {}
): DomListener<MouseEvent, HTMLElement> {
    var clickEvents: DomListener<MouseEvent, HTMLElement>? = null
    pushButton(variants = variants, baseClass = baseClass, id = id) {
        context()
        events {
            clickEvents = clicks
        }
    }
    return clickEvents!!
}

/**
 * Creates a [Button] component backed by an `<a/>` element.
 *
 * @param variants variations to control the visual representation of the button
 * @param href the URL of the link
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.ButtonSample.linkButton
 */
public fun RenderContext.linkButton(
    vararg variants: ButtonVariant,
    href: String? = null,
    baseClass: String? = null,
    id: String? = null,
    context: Button.() -> Unit = {}
) {
    Button(LINK, variants).apply(context).also { button ->
        href?.let { button.href(it) }
    }.render(this, baseClass, id)
}

/**
 * Creates a [Button] component backed by an `<span/>` element. Use this if you want to use a long text that needs to  wrap.
 *
 * @param variants variations to control the visual representation of the button
 * @param href the URL of the link
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.inlineLinkButton(
    vararg variants: ButtonVariant,
    href: String? = null,
    baseClass: String? = null,
    id: String? = null,
    context: Button.() -> Unit = {}
) {
    Button(INLINE_LINK, variants).apply(context).also { button ->
        href?.let { button.href(it) }
    }.render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [button](https://www.patternfly.org/v4/components/button/design-guidelines/) component.
 *
 * A button is a box area or text that communicates and triggers user actions when clicked or selected.
 *
 * The button's visual representation is controlled by one or several [ButtonVariant]s.
 */
@Suppress("TooManyFunctions")
public open class Button(
    private val buttonType: ButtonType,
    private val variations: Array<out ButtonVariant>
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithTitle {

    private var content: (RenderContext.() -> Unit)? = null

    /**
     * Uses the specified custom content for the button. Use this if you want to have full control over the button's visual representation. If used any [title] and / or [icon] will be ignored.
     *
     * @sample org.patternfly.sample.ButtonSample.content
     */
    public fun content(content: RenderContext.() -> Unit) {
        this.content = content
    }

    private var disabled: Flow<Boolean> = emptyFlow()

    /**
     * Disables the button.
     */
    public fun disabled(value: Boolean) {
        disabled = flowOf(value)
    }

    /**
     * Disables the button based on the values in the specified [Flow].
     */
    public fun disabled(value: Flow<Boolean>) {
        disabled = value
    }

    private var href: String? = null

    /**
     * Assigns the specified href for a [linkButton] or [inlineLinkButton].
     */
    public fun href(href: String) {
        this.href = href
    }

    private var icon: (RenderContext.(align: String?) -> Unit)? = null
    private var iconAndTitle: IconAndTitle = UNDEFINED
    private var iconFirst: Boolean = true

    /**
     * Uses an icon for the button. Buttons can use title, icons or a combination of both.
     *
     * @sample org.patternfly.sample.ButtonSample.titleAndIcon
     */
    public fun icon(iconClass: String = "", context: Icon.() -> Unit = {}) {
        this.icon = { align ->
            if (align != null) {
                span(baseClass = classes("button".component("icon"), align)) {
                    icon(iconClass = iconClass) {
                        context(this)
                    }
                }
            } else {
                icon(iconClass = iconClass) {
                    context(this)
                }
            }
        }
        iconFirst = !hasTitle
        iconAndTitle = if (hasTitle) ICON_LAST else ICON_ONLY
    }

    private var loading: Flow<Boolean>? = null
    private var loadingTitle: (RenderContext.() -> Unit)? = null

    /**
     * Shows a loading indicator as long as [loading] emits `true`.
     *
     * @sample org.patternfly.sample.ButtonSample.loading
     */
    public fun loading(value: Flow<Boolean>) {
        loading = value
    }

    /**
     * Shows a loading indicator and [loadingTitle] as long as [loading] emits `true`.
     *
     * @sample org.patternfly.sample.ButtonSample.loading
     */
    public fun loading(loading: Flow<Boolean>, loadingTitle: String) {
        this.loading = loading
        this.loadingTitle = { span { +loadingTitle } }
    }

    /**
     * Shows a loading indicator and [loadingTitle] as long as [loading] emits `true`.
     *
     * @sample org.patternfly.sample.ButtonSample.loading
     */
    public fun loading(loading: Flow<Boolean>, loadingTitle: Flow<String>) {
        this.loading = loading
        this.loadingTitle = { span { loadingTitle.asText() } }
    }

    /**
     * Shows a loading indicator and [loadingTitle] as long as [loading] emits `true`.
     *
     * @sample org.patternfly.sample.ButtonSample.loadingTitle
     */
    public fun loading(loading: Flow<Boolean>, loadingTitle: RenderContext.() -> Unit) {
        this.loading = loading
        this.loadingTitle = loadingTitle
    }

    private var size: ButtonSize? = null

    /**
     * Sets the specified button size.
     *
     * @sample org.patternfly.sample.ButtonSample.sizes
     */
    public fun size(size: ButtonSize) {
        this.size = size
    }

    private var target: String? = null

    /**
     * Use the specified target for a [linkButton] or [inlineLinkButton].
     */
    public fun target(target: String) {
        this.target = target
    }

    private var title: (RenderContext.() -> Unit)? = null

    override val hasTitle: Boolean
        get() = title != null

    override fun String.unaryPlus() {
        assignStaticTitle(this)
    }

    override fun title(title: String) {
        assignStaticTitle(title)
    }

    override fun title(title: Flow<String>) {
        assignDynamicTitle(title)
    }

    override fun <T> title(title: Flow<T>) {
        assignDynamicTitle(title.map { it.toString() })
    }

    override fun Flow<String>.asText() {
        assignDynamicTitle(this)
    }

    override fun <T> Flow<T>.asText() {
        assignDynamicTitle(this.map { it.toString() })
    }

    override fun applyTitle(target: RenderContext) {
        title?.invoke(target)
    }

    private fun assignStaticTitle(title: String) {
        this.title = {
            span { +title }
        }
        iconAndTitle = if (icon == null) TITLE_ONLY else ICON_FIRST
    }

    private fun assignDynamicTitle(title: Flow<String>) {
        this.title = {
            span { title.asText() }
        }
        iconAndTitle = if (icon == null) TITLE_ONLY else ICON_FIRST
    }

    private var type: String? = null

    /**
     * Assigns the specified button type.
     */
    public fun type(type: String) {
        this.type = type
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        val classes = classes {
            +ComponentType.Button
            +variations.joinToString(" ") { it.modifier }
            +size?.modifier
            +("progress".modifier() `when` (loading != null))
            +baseClass
        }

        with(context) {
            when (buttonType) {
                BUTTON -> {
                    button(baseClass = classes, id = id) {
                        applyCommons(this)
                        attr("disabled", disabled)
                        type?.let { this.type(it) }
                        renderContent(this, true)
                    }
                }
                LINK -> {
                    a(baseClass = classes, id = id) {
                        applyCommons(this)
                        aria["disabled"] = disabled
                        mountSimple(job, disabled) { d ->
                            if (d) domNode.setAttribute("tabindex", "-1")
                            else domNode.removeAttribute("tabindex")
                        }
                        renderContent(this, false)
                    }
                }
                INLINE_LINK -> {
                    span(baseClass = classes, id = id) {
                        applyCommons(this)
                        aria["disabled"] = disabled
                        attr("type", "button")
                        attr("role", "button")
                        attr("tabindex", 0)
                        renderContent(this, false)
                    }
                }
            }
        }
    }

    private fun <T : Tag<E>, E : HTMLElement> applyCommons(tag: T) {
        with(tag) {
            markAs(ComponentType.Button)
            applyElement(this)
            applyEvents(this)
        }
    }

    private fun renderContent(context: RenderContext, supportsProgress: Boolean) {
        if (supportsProgress && loading != null) {
            loading?.let { loading ->
                with(context) {
                    classMap(loading.map { mapOf("in-progress".modifier() to it) })
                    loading.distinctUntilChanged().render(into = this) { running ->
                        if (running) {
                            span(baseClass = "button".component("progress")) {
                                spinner {}
                            }
                        }
                        renderContentOrIconAndTitle(this, running)
                    }
                }
            }
        } else {
            renderContentOrIconAndTitle(context, false)
        }
    }

    private fun renderContentOrIconAndTitle(context: RenderContext, running: Boolean) {
        if (content != null) {
            content?.invoke(context)
        } else {
            when (iconAndTitle) {
                TITLE_ONLY -> loadingOrTitle(context, running)
                ICON_ONLY -> {
                    loadingOrTitle(context, running)
                    icon?.invoke(context, if (loadingTitle != null) "end".modifier() else null)
                }
                ICON_FIRST -> {
                    icon?.invoke(context, "start".modifier())
                    loadingOrTitle(context, running)
                }
                ICON_LAST -> {
                    loadingOrTitle(context, running)
                    icon?.invoke(context, "end".modifier())
                }
                UNDEFINED -> console.warn("Undefined icon and title definition in button")
            }
        }
    }

    private fun loadingOrTitle(context: RenderContext, running: Boolean) {
        if (running && loadingTitle != null) {
            loadingTitle?.invoke(context)
        } else {
            applyTitle(context)
        }
    }
}

/**
 * Visual modifiers for [Button]s.
 *
 * @see <a href="https://www.patternfly.org/v4/components/button/design-guidelines#button-types">https://www.patternfly.org/v4/components/button/design-guidelines#button-types</a>
 */
@Suppress("EnumEntryName", "EnumNaming", "unused")
public enum class ButtonVariant(internal val modifier: String) {
    block("block".modifier()),
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
 * Supported [Button] sizes.
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class ButtonSize(internal val modifier: String) {
    callToAction("display-lg".modifier()),
    small("small".modifier())
}

/**
 * The button type which controls the DOM element used.
 */
public enum class ButtonType {
    BUTTON, LINK, INLINE_LINK
}

internal enum class IconAndTitle {
    UNDEFINED, TITLE_ONLY, ICON_ONLY, ICON_FIRST, ICON_LAST
}
