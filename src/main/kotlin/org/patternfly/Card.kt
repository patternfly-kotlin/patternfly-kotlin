package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Hr
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariant.plain
import org.patternfly.DividerVariant.HR
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

public fun RenderContext.card(
    vararg variants: CardVariant,
    toggleRight: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Card.() -> Unit
) {
    Card(variants, toggleRight).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

public open class Card(
    private val variants: Array<out CardVariant>,
    private val toggleRight: Boolean
) : PatternFlyComponent<TextElement>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithExpandedStore by ExpandedStoreMixin() {

    private var components: MutableList<CardComponent<*>> = mutableListOf()
    private val expandable: Boolean = CardVariant.expandable in variants
    private val selectable: Boolean = CardVariant.selectable in variants
    private var selected: Flow<Boolean> = emptyFlow()

    public fun header(baseClass: String? = null, id: String? = null, context: CardHeader.() -> Unit) {
        CardHeader(
            expandable = expandable,
            selectable = selectable,
            toggleRight = toggleRight,
            expandedStore = expandedStore,
            baseClass = baseClass,
            id = id
        ).apply(context).also {
            components.add(it)
        }
    }

    public fun title(baseClass: String? = null, id: String? = null, context: Div.() -> Unit) {
        components.add(CardTitle(baseClass, id ?: Id.unique(ComponentType.Card.id, "title"), context))
    }

    public fun body(
        fill: Boolean = false,
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        components.add(CardBody(components, fill, baseClass, id, context))
    }

    public fun footer(baseClass: String? = null, id: String? = null, context: Div.() -> Unit) {
        components.add(CardFooter(baseClass, id, context))
    }

    public fun divider() {
        components.add(CardDivider())
    }

    /**
     * Controls whether the card is selected.
     */
    public fun selected(value: Boolean) {
        selected = flowOf(value)
    }

    /**
     * Controls whether the card is selected.
     */
    public fun selected(value: Flow<Boolean>) {
        selected = value
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?): TextElement = with(context) {
        article(
            baseClass = classes {
                +ComponentType.Card
                +variants.joinToString(" ") { it.modifier }
            },
            id = id
        ) {
            markAs(ComponentType.Card)
            applyElement(this)
            applyEvents(this)
            if (selectable && expandable) {
                classMap(
                    expandedStore.data.combine(selected) { expanded, selected ->
                        expanded to selected
                    }.map { (expanded, selected) ->
                        mapOf(
                            "expanded".modifier() to expanded,
                            "selected-raised".modifier() to selected
                        )
                    }
                )
            } else if (expandable) {
                with(expandedStore) {
                    toggleExpanded()
                }
            }
            if (expandable) {
                components.find { it is CardHeader }?.render(this)
                div(baseClass = "card".component("expandable", "content")) {
                    with(expandedStore) {
                        hideIfCollapsed()
                        toggleDisplayNone()
                    }
                    components.filterNot { it is CardHeader }.forEach { it.render(this) }
                }
            } else {
                components.forEach { it.render(this) }
            }
        }
    }
}

/**
 * Visual modifiers for [Card]s.
 *
 * @see <a href="https://www.patternfly.org/v4/components/card/design-guidelines#variations">https://www.patternfly.org/v4/components/card/design-guidelines#variations</a>
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class CardVariant(internal val modifier: String) {
    compact("compact".modifier()),
    expandable(""),
    flat("flat".modifier()),
    fullHeight("full-height".modifier()),
    hoverable("hoverable-raised".modifier()),
    large("display-lg".modifier()),
    plain("plain".modifier()),
    rounded("rounded".modifier()),
    selectable("selectable-raised".modifier()),
}

// ------------------------------------------------------ card components

public sealed class CardComponent<T>(baseClass: String?, id: String?, context: T.() -> Unit) :
    SubComponent<T>(baseClass, id, context) {

    internal abstract fun render(context: RenderContext)
}

public class CardHeader(
    private val expandable: Boolean,
    private val selectable: Boolean,
    private val toggleRight: Boolean,
    private val expandedStore: ExpandedStore,
    baseClass: String?,
    id: String?
) : CardComponent<Div>(baseClass, id, {}) {

    private var title: CardTitle? = null
    private var actions: SubComponent<Div>? = null
    private var main: SubComponent<Div>? = null
    private var content: SubComponent<Div>? = null

    public fun title(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        this.title = CardTitle(baseClass, id ?: Id.unique(ComponentType.Card.id, "title"), context)
    }

    public fun actions(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit
    ) {
        this.actions = SubComponent(baseClass, id, context)
    }

    public fun main(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit
    ) {
        this.main = SubComponent(baseClass, id, context)
    }

    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit
    ) {
        this.content = SubComponent(baseClass, id, context)
    }

    override fun render(context: RenderContext) {
        with(context) {
            div(
                baseClass = classes {
                    +"card".component("header")
                    +("toggle-right".modifier() `when` toggleRight)
                    +this@CardHeader.baseClass
                },
                id = this@CardHeader.id
            ) {
                if (selectable) {
                    domNode.onclick = { it.stopPropagation() }
                }
                if (expandable && !toggleRight) {
                    renderToggle(this)
                }
                actions?.let { actions ->
                    div(
                        baseClass = classes("card".component("actions"), actions.baseClass),
                        id = actions.id
                    ) {
                        actions.context(this)
                    }
                }
                if (content != null) {
                    content?.context?.invoke(this)
                } else {
                    main?.let { main ->
                        div(
                            baseClass = classes("card".component("header", "main"), main.baseClass),
                            id = main.id
                        ) {
                            main.context(this)
                        }
                    }
                    title?.render(this)
                }
                if (expandable && toggleRight) {
                    renderToggle(this)
                }
            }
        }
    }

    private fun renderToggle(context: RenderContext) {
        with(context) {
            div(baseClass = "card".component("header", "toggle")) {
                clickButton(plain) {
                    title?.id?.let { aria["labelledby"] = it }
                    content {
                        span(baseClass = "card".component("header", "toggle", "icon")) {
                            icon("angle-right".fas())
                        }
                    }
                } handledBy expandedStore.toggle
            }
        }
    }
}

public class CardTitle(baseClass: String?, id: String, context: Div.() -> Unit) :
    CardComponent<Div>(baseClass, id, context) {

    override fun render(context: RenderContext) {
        with(context) {
            div(
                baseClass = classes("card".component("title"), this@CardTitle.baseClass),
                id = this@CardTitle.id
            ) {
                this@CardTitle.context(this)
            }
        }
    }
}

public class CardBody(
    private val components: List<CardComponent<*>>,
    private val fill: Boolean,
    baseClass: String?,
    id: String?,
    context: Div.() -> Unit
) : CardComponent<Div>(baseClass, id, context) {

    override fun render(context: RenderContext) {
        with(context) {
            if (anyBodyWithFill()) {
                div(
                    baseClass = classes {
                        +"card".component("body")
                        +("no-fill".modifier() `when` !fill)
                        +this@CardBody.baseClass
                    },
                    id = this@CardBody.id
                ) {
                    this@CardBody.context(this)
                }
            } else {
                div(
                    baseClass = classes("card".component("body"), this@CardBody.baseClass),
                    id = this@CardBody.id
                ) {
                    this@CardBody.context(this)
                }
            }
        }
    }

    private fun anyBodyWithFill() = fill || components.any { it is CardBody && it.fill }
}

public class CardFooter(baseClass: String?, id: String?, context: Div.() -> Unit) :
    CardComponent<Div>(baseClass, id, context) {

    override fun render(context: RenderContext) {
        with(context) {
            div(
                baseClass = classes("card".component("footer"), this@CardFooter.baseClass),
                id = this@CardFooter.id
            ) {
                this@CardFooter.context(this)
            }
        }
    }
}

public class CardDivider : CardComponent<Hr>(baseClass = null, id = null, context = {}) {

    override fun render(context: RenderContext) {
        with(context) {
            divider(HR)
        }
    }
}
