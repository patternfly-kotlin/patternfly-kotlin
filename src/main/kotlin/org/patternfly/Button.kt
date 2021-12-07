package org.patternfly

import dev.fritz2.dom.DomListener
import dev.fritz2.dom.Listener
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.html.TagContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.patternfly.IconAndTitle.ICON_FIRST
import org.patternfly.IconAndTitle.ICON_LAST
import org.patternfly.IconAndTitle.ICON_ONLY
import org.patternfly.IconAndTitle.TITLE_ONLY
import org.patternfly.IconAndTitle.UNDEFINED
import org.patternfly.dom.debug
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ factory

public fun RenderContext.button2(
    vararg variations: ButtonVariation,
    baseClass: String? = null,
    id: String? = null,
    context: Button2.() -> Unit = {}
) {
    Button2(variations).apply(context).render(this, baseClass, id)
}

/**
 * Creates a [PushButton] component. This component uses a `<button/>` element.
 *
 * @param variations variations to control the visual representation of the button
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.ButtonSample.pushButton
 */
public fun RenderContext.pushButton(
    vararg variations: ButtonVariation,
    baseClass: String? = null,
    id: String? = null,
    context: PushButton.() -> Unit = {}
): PushButton = register(PushButton(variations, id = id, baseClass = baseClass, job), context)

/**
 * Creates a [PushButton] component and returns a [Listener] (basically a [Flow]) in order to combine the button declaration directly to a fitting handler.
 *
 * @param variations variations to control the visual representation of the button
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 * @return a listener for the click events to be consumed by a fitting handler
 *
 * @sample org.patternfly.sample.ButtonSample.clickButton
 */
public fun RenderContext.clickButton(
    vararg variations: ButtonVariation,
    id: String? = null,
    baseClass: String? = null,
    context: PushButton.() -> Unit = {}
): DomListener<MouseEvent, HTMLButtonElement> {
    var clickEvents: DomListener<MouseEvent, HTMLButtonElement>? = null
    pushButton(*variations, id = id, baseClass = baseClass) {
        context(this)
        clickEvents = clicks
    }
    return clickEvents!!
}

/**
 * Creates a [LinkButton] component. This component uses an `<a/>` element.
 *
 * @param variations variations to control the visual representation of the button
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.ButtonSample.linkButton
 */
public fun RenderContext.linkButton(
    vararg variations: ButtonVariation,
    id: String? = null,
    baseClass: String? = null,
    context: LinkButton.() -> Unit = {}
): LinkButton = register(LinkButton(variations, id = id, baseClass = baseClass, job), context)

/**
 * Adds an [Icon] to a [PushButton] or [LinkButton]. Use this function if you also want to add other elements like text to the button. This function adds the icons inside a container that controls the margin between the icon and the other elements (like the text).
 *
 * If you only want to add an icon, you don't have to use this function.
 *
 * @param iconPosition the position of the icon
 * @param id the ID of the icon element
 * @param baseClass optional CSS class that should be applied to the icon element
 * @param content a lambda expression for setting up the icon component
 *
 * @sample org.patternfly.sample.ButtonSample.buttonIcon
 * @sample org.patternfly.sample.ButtonSample.justIcon
 */
public fun ButtonLike.buttonIcon(
    iconPosition: IconPosition,
    iconClass: String,
    id: String? = null,
    baseClass: String? = null,
    content: Icon.() -> Unit = {},
): ButtonIcon = register(ButtonIcon(iconPosition, iconClass, id, baseClass, job, content), {})

// ------------------------------------------------------ component

@Suppress("TooManyFunctions")
public class Button2 internal constructor(private val variations: Array<out ButtonVariation>) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithTitle {

    private var title: (RenderContext.() -> Unit)? = null
    private var iconFirst: Boolean = true
    private var icon: (RenderContext.(align: String?) -> Unit)? = null
    private var iconAndTitle: IconAndTitle = UNDEFINED
    private var type: String? = null
    private var href: String? = null
    private var target: String? = null
    private var disabled: Flow<Boolean> = emptyFlow()
    private var loading: Flow<Boolean>? = null
    private var loadingTitle: (RenderContext.() -> Unit)? = null
    private lateinit var root: Tag<HTMLElement>

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

    public fun type(type: String) {
        this.type = type
    }

    public fun href(href: String) {
        this.href = href
    }

    public fun target(target: String) {
        this.target = target
    }

    public fun icon(iconClass: String = "", context: Icon.() -> Unit = {}) {
        this.iconFirst = !this.hasTitle
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
        iconAndTitle = if (title == null) ICON_ONLY else ICON_LAST
    }

    public fun disabled(value: Boolean) {
        disabled = flowOf(value)
    }

    public fun disabled(value: Flow<Boolean>) {
        disabled = value
    }

    public fun loading(value: Flow<Boolean>) {
        loading = value
    }

    public fun loadingTitle(value: String) {
        loadingTitle = { span { +value } }
    }

    public fun loadingTitle(value: Flow<String>) {
        loadingTitle = { span { value.asText() } }
    }

    public fun loadingTitle(value: RenderContext.() -> Unit) {
        loadingTitle = value
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        val buttonType = if (href != null) {
            if (ButtonVariation.inline in variations) {
                ButtonType.INLINE_LINK
            } else {
                ButtonType.LINK
            }
        } else {
            ButtonType.BUTTON
        }
        val classes = classes {
            +ComponentType.Button
            +variations.joinToString(" ") { it.modifier }
            +("progress".modifier() `when` (loading != null))
            +baseClass
        }

        with(context) {
            root = when (buttonType) {
                ButtonType.BUTTON -> {
                    button(baseClass = classes, id = id) {
                        applyCommons(this)
                        attr("disabled", disabled)
                        type?.let { this.type(it) }
                        renderIconAndTitle(this)
                    }
                }
                ButtonType.LINK -> {
                    a(baseClass = classes, id = id) {
                        applyCommons(this)
                        applyTitle(this)
                    }
                }
                ButtonType.INLINE_LINK -> {
                    span(baseClass = classes, id = id) {
                        applyCommons(this)
                        applyTitle(this)
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

    private fun renderIconAndTitle(context: RenderContext) {
        if (loading != null) {
            loading?.let { loading ->
                with(context) {
                    classMap(loading.map { mapOf("in-progress".modifier() to it) })
                    loading.distinctUntilChanged().render(into = this) { running ->
                        if (running) {
                            span(baseClass = "button".component("progress")) {
                                spinner {}
                            }
                        }
                        renderIconAndTitleWithProgress(this, running)
                    }
                }
            }
        } else {
            renderIconAndTitleWithProgress(context, false)
        }
    }

    private fun renderIconAndTitleWithProgress(context: RenderContext, running: Boolean) {
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
            UNDEFINED -> console.warn("Undefined icon and title position in ${root.domNode.debug()}")
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

internal enum class ButtonType {
    BUTTON, LINK, INLINE_LINK
}

internal enum class IconAndTitle {
    UNDEFINED, TITLE_ONLY, ICON_ONLY, ICON_FIRST, ICON_LAST
}

/** Marker interface for [PushButton] and [LinkButton]s. */
public interface ButtonLike : TagContext

/**
 * PatternFly [push button](https://www.patternfly.org/v4/components/button/design-guidelines) component based on a `<button/>` element.
 *
 * A button is a box area or text that communicates and triggers user actions when clicked or selected.
 *
 * @sample org.patternfly.sample.ButtonSample.pushButton
 */
public class PushButton internal constructor(
    variations: Array<out ButtonVariation>,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyElement<HTMLButtonElement>,
    ButtonLike,
    Button(
        id = id,
        baseClass = classes {
            +ComponentType.Button
            +variations.joinToString(" ") { it.modifier }
            +baseClass
        },
        job = job,
        scope = Scope()
    ) {

    init {
        markAs(ComponentType.Button)
    }
}

/**
 * PatternFly [link button](https://www.patternfly.org/v4/components/button/design-guidelines#link-button) component based on an `<a/>` element.
 *
 * Links buttons are labeled buttons with no background or border.
 *
 * @sample org.patternfly.sample.ButtonSample.linkButton
 */
public class LinkButton internal constructor(
    variations: Array<out ButtonVariation>,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyElement<HTMLAnchorElement>,
    ButtonLike,
    A(
        id,
        classes {
            +ComponentType.Button
            +variations.joinToString(" ") { it.modifier }
            +baseClass
        },
        job,
        scope = Scope()
    ) {

    init {
        markAs(ComponentType.Button)
    }
}

/**
 * Container for an icon inside a [PushButton] or [LinkButton]. The container controls a margin between the icon and the button text depending on the value of [IconPosition].
 *
 * @sample org.patternfly.sample.ButtonSample.buttonIcon
 */
public class ButtonIcon internal constructor(
    iconPosition: IconPosition,
    iconClass: String,
    id: String?,
    baseClass: String?,
    job: Job,
    content: Icon.() -> Unit
) : Span(
    baseClass = classes {
        +"button".component("icon")
        +iconPosition.modifier
    },
    job = job,
    scope = Scope()
) {

    init {
        icon(iconClass, id = id, baseClass = baseClass) {
            content(this)
        }
    }
}
