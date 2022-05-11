package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

// ------------------------------------------------------ factory

/**
 * Creates the [EmptyState] component.
 *
 * @param size the size of the empty state component. See [EmptyState.SUPPORTED_SIZES] for supported sizes.
 * @param iconClass an optional icon class
 * @param title the title of the empty state component
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.emptyState(
    size: Size = Size.MD,
    iconClass: String? = null,
    title: String? = null,
    baseClass: String? = null,
    id: String? = null,
    context: EmptyState.() -> Unit = {}
) {
    EmptyState(
        size = size,
        iconClass = iconClass,
        title = title
    ).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [empty state](https://www.patternfly.org/v4/components/empty-state/design-guidelines) component.
 *
 * An empty state component fills a screen that is not yet populated with data or information.
 *
 * @sample org.patternfly.sample.EmptyStateSample.basicSetup
 */
@Suppress("TooManyFunctions")
public open class EmptyState(
    private val size: Size,
    private var iconClass: String?,
    title: String?,
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    private var icon: (Icon.() -> Unit)? = null
    private var content: SubComponent<Div>? = null
    private var loading: Flow<Boolean>? = null
    private var loadingTitle: (RenderContext.() -> Unit)? = null
    private var primaryAction: SubComponent<Button>? = null
    private var primaryActionVariants: Array<out ButtonVariant> = emptyArray()
    private val secondaryActions: MutableList<Pair<Array<out ButtonVariant>, SubComponent<Button>>> = mutableListOf()

    init {
        title?.let { this.title(it) }
    }

    public fun icon(iconClass: String?, context: Icon.() -> Unit = {}) {
        this.iconClass = iconClass
        this.icon = context
    }

    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        this.content = SubComponent(baseClass, id, context)
    }

    public fun loading(value: Flow<Boolean>) {
        loading = value
    }

    public fun loading(loading: Flow<Boolean>, loadingTitle: String) {
        this.loading = loading
        this.loadingTitle = { span { +loadingTitle } }
    }

    public fun loading(loading: Flow<Boolean>, loadingTitle: Flow<String>) {
        this.loading = loading
        this.loadingTitle = { span { loadingTitle.renderText() } }
    }

    public fun loading(loading: Flow<Boolean>, loadingTitle: RenderContext.() -> Unit) {
        this.loading = loading
        this.loadingTitle = loadingTitle
    }

    public fun primaryAction(
        vararg variants: ButtonVariant = arrayOf(ButtonVariant.primary),
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit
    ) {
        this.primaryAction = SubComponent(baseClass, id, context)
        this.primaryActionVariants = variants
    }

    public fun secondaryAction(
        vararg variants: ButtonVariant = arrayOf(ButtonVariant.link),
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit
    ) {
        secondaryActions.add(variants to SubComponent(baseClass, id, context))
    }

    @Suppress("LongMethod")
    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(
                baseClass = classes {
                    +ComponentType.EmptyState
                    +(size.modifier `when` (size in SUPPORTED_SIZES))
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.EmptyState)
                applyElement(this)
                applyEvents(this)

                div(baseClass = "empty-state".component("content")) {
                    if (loading != null) {
                        loading?.let { loading ->
                            loading.distinctUntilChanged().render(into = this) { running ->
                                if (running) {
                                    div(baseClass = "empty-state".component("icon")) {
                                        spinner { }
                                    }
                                }
                                renderIcon(this)
                            }
                        }
                    } else {
                        renderIcon(this)
                    }
                    val titleSize = when (size) {
                        Size.XL -> Size.XL_4
                        Size.XS -> Size.MD
                        else -> Size.LG
                    }
                    title(size = titleSize) {
                        element {
                            applyTitle(this)
                        }
                    }
                    content?.let { body ->
                        div(
                            baseClass = classes("empty-state".component("body"), body.baseClass),
                            id = body.id
                        ) {
                            body.context(this)
                        }
                    }
                    primaryAction?.let { primary ->
                        if (ButtonVariant.primary in primaryActionVariants) {
                            renderPrimary(this, primary)
                        } else {
                            div(baseClass = "empty-state".component("primary")) {
                                renderPrimary(this, primary)
                            }
                        }
                    }
                    if (secondaryActions.isNotEmpty()) {
                        div(baseClass = "empty-state".component("secondary")) {
                            secondaryActions.forEach { (variants, secondary) ->
                                pushButton(
                                    variants = variants,
                                    baseClass = secondary.baseClass,
                                    id = secondary.id
                                ) {
                                    secondary.context(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderIcon(context: RenderContext) {
        with(context) {
            iconClass?.let {
                icon(
                    iconClass = it,
                    baseClass = "empty-state".component("icon")
                ) {
                    this@EmptyState.icon?.invoke(this)
                }
            }
        }
    }

    private fun renderPrimary(context: RenderContext, primary: SubComponent<Button>) {
        with(context) {
            pushButton(
                variants = primaryActionVariants,
                baseClass = primary.baseClass,
                id = primary.id
            ) {
                primary.context(this)
            }
        }
    }

    public companion object {
        public val SUPPORTED_SIZES: Set<Size> = setOf(Size.XL, Size.LG, Size.SM, Size.XS)
    }
}
