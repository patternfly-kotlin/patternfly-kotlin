package org.patternfly

import dev.fritz2.binding.Store
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Hr
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.states
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariant.plain
import org.patternfly.DividerVariant.HR
import org.patternfly.dom.Id
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ factory

/**
 * Creates an [CardView] component.
 *
 * @param selectionMode the selection mode for cards
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.cardView(
    selectionMode: SelectionMode = SelectionMode.NONE,
    baseClass: String? = null,
    id: String? = null,
    context: CardView.() -> Unit = {}
) {
    CardView(selectionMode).apply(context).render(this, baseClass, id)
}

/**
 * Creates a [Card] component.
 *
 * @param variants controls the visual representation of the card
 * @param selection manages the selection of the card
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.card(
    vararg variants: CardVariant,
    selection: Store<Boolean>? = null,
    baseClass: String? = null,
    id: String? = null,
    context: Card.() -> Unit
) {
    Card(variants, selection).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ card view component

/**
 * PatternFly [card view](https://www.patternfly.org/v4/demos/card-view/design-guidelines) component.
 *
 * A card view is a grid of cards in a gallery to facilitate browsing. Card views are typically used to present data set summaries, allowing users to drill down into any card to see more detailed content.
 */
public class CardView internal constructor(private val selectionMode: SelectionMode) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val singleIdSelection: SingleIdStore = SingleIdStore()
    private val multiIdSelection: MultiIdStore = MultiIdStore()
    private val itemStore: HeadTailItemStore<CardViewItem> = HeadTailItemStore()

    public val selectedId: Flow<String?>
        get() = singleIdSelection.data

    public val selectedIds: Flow<List<String>>
        get() = multiIdSelection.data

    public fun card(
        vararg variants: CardVariant,
        id: String = Id.unique(ComponentType.CardView.id, "itm"),
        context: StaticCardViewItem.() -> Unit
    ) {
        val cardSelection = CardViewCardSelection(selectionMode, singleIdSelection, multiIdSelection)
        val item = StaticCardViewItem(id, variants, cardSelection).apply(context)
        itemStore.add(item)
        if (CardVariant.selectable in variants) {
            item.select()
        }
    }

    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        singleSelection: Store<T?>? = null,
        multiSelection: Store<List<T>>? = null,
        display: CardViewItemScope.(T) -> CardViewItem
    ) {
        storeItems(values.data, idProvider, singleSelection, multiSelection, display)
    }

    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        singleSelection: Store<T?>? = null,
        multiSelection: Store<List<T>>? = null,
        display: CardViewItemScope.(T) -> CardViewItem
    ) {
        storeItems(values, idProvider, singleSelection, multiSelection, display)
    }

    private fun <T> storeItems(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String>,
        singleDataSelection: Store<T?>?,
        multiDataSelection: Store<List<T>>?,
        display: CardViewItemScope.(T) -> CardViewItem
    ) {
        itemStore.collect(values) { valueList ->
            val idToData = valueList.associateBy { idProvider(it) }
            itemStore.update(valueList) { value ->
                val cardSelection = CardViewCardSelection(selectionMode, singleIdSelection, multiIdSelection)
                CardViewItemScope(idProvider(value), cardSelection).run {
                    display(this, value)
                }
            }

            // setup data bindings
            if (selectionMode == SelectionMode.SINGLE) {
                singleDataSelection?.let { sds ->
                    singleIdSelection.dataBinding(idToData, idProvider, sds)
                }
            } else if (selectionMode == SelectionMode.MULTI) {
                multiDataSelection?.let { mds ->
                    multiIdSelection.dataBinding(idToData, idProvider, mds)
                }
            }
        }
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            ul(
                baseClass = classes {
                    +"gallery".layout()
                    +"gutter".modifier()
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.CardView)
                itemStore.allItems.renderEach(into = this, idProvider = { it.id }) { card ->
                    card.render(this, baseClass = null, id = card.id)
                }
            }
        }
    }
}

/**
 * DSL scope class to create [CardViewItem]s when using [CardView.items] functions.
 */
public class CardViewItemScope internal constructor(
    private val id: String,
    private val cardViewCardSelection: CardViewCardSelection,
) {
    public fun card(vararg variants: CardVariant, context: CardViewItem.() -> Unit): CardViewItem =
        CardViewItem(id, variants, cardViewCardSelection).apply(context)
}

// ------------------------------------------------------ card component

/**
 * PatternFly [card](https://www.patternfly.org/v4/components/card/design-guidelines) component.
 *
 * A card is a square or rectangular container that can contain any kind of content. Cards symbolize units of information, and each one acts as an entry point for users to access more details.
 */
public class Card internal constructor(variants: Array<out CardVariant>, selection: Store<Boolean>?) :
    BaseCard(variants, if (selection != null) StandaloneCardSelection(selection) else NoopCardSelection())

/**
 * A card inside a [CardView]
 */

public open class CardViewItem internal constructor(
    public val id: String,
    variants: Array<out CardVariant>,
    internal val cardViewCardSelection: CardViewCardSelection
) : BaseCard(variants, cardViewCardSelection)

/**
 * A static card inside a [CardView]
 */
public class StaticCardViewItem internal constructor(
    id: String,
    variants: Array<out CardVariant>,
    cardViewCardSelection: CardViewCardSelection
) : CardViewItem(id, variants, cardViewCardSelection) {

    private val selection: FlagOrFlow = FlagOrFlow(id)

    public fun selected(value: Boolean) {
        selection.flag = value
    }

    public fun selected(value: Flow<Boolean>) {
        selection.flow = value
    }

    internal fun select() {
        if (cardViewCardSelection.selectionMode == SelectionMode.SINGLE) {
            selection.singleSelect(cardViewCardSelection.singleIdSelection)
        } else if (cardViewCardSelection.selectionMode == SelectionMode.MULTI) {
            selection.multiSelect(cardViewCardSelection.multiIdSelection)
        }
    }
}

/**
 * Common base class for standalone [Card]s and cards in a [CardView].
 */
public abstract class BaseCard internal constructor(
    private val variants: Array<out CardVariant>,
    private val cardSelection: CardSelection
) : PatternFlyComponent<TextElement>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithExpandedStore by ExpandedStoreMixin() {

    private var components: MutableList<CardComponent<*>> = mutableListOf()
    private val selectableVariant: Boolean
        get() = CardVariant.selectable in variants
    private val expandable: Boolean
        get() = components.filterIsInstance<CardHeader>().any { it.toggle || it.toggleRight }

    /**
     * Adds the card header.
     *
     * @sample org.patternfly.sample.CardSample.imageInHeader
     * @sample org.patternfly.sample.CardSample.titleInHeader
     */
    public fun header(baseClass: String? = null, id: String? = null, context: CardHeader.() -> Unit) {
        CardHeader(
            selectableVariant = selectableVariant,
            cardSelection = cardSelection,
            expandedStore = expandedStore,
            baseClass = baseClass,
            id = id
        ).apply(context).also {
            components.add(it)
        }
    }

    /**
     * Adds the card title.
     *
     * @sample org.patternfly.sample.CardSample.noHeader
     */
    public fun title(baseClass: String? = null, id: String? = null, context: Div.() -> Unit) {
        components.add(CardTitle(baseClass, id ?: Id.unique(ComponentType.Card.id, "title"), context))
    }

    /**
     * Adds a card body.
     *
     * @sample org.patternfly.sample.CardSample.multipleBodies
     */
    public fun body(
        fill: Boolean = false,
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        components.add(CardBody(components, fill, baseClass, id, context))
    }

    /**
     * Adds the card footer
     */
    public fun footer(baseClass: String? = null, id: String? = null, context: Div.() -> Unit) {
        components.add(CardFooter(baseClass, id, context))
    }

    /**
     * Adds a card divider.
     */
    public fun divider() {
        components.add(CardDivider())
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?): TextElement = with(context) {
        // make sure we have a valid card ID; then pass it to the card header
        val cardId = id ?: Id.unique(ComponentType.Card.id)
        components.filterIsInstance<CardHeader>().forEach {
            it.cardId = cardId
        }

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

            // setup CSS classes based on stores & flags
            if (cardSelection.supported) {
                if (selectableVariant && expandable) {
                    cardSelection.setupClicks(this, cardId)
                    classMap(
                        expandedStore.data.combine(cardSelection.selected(cardId)) { expanded, selected ->
                            expanded to selected
                        }.map { (expanded, selected) ->
                            mapOf(
                                "expanded".modifier() to expanded,
                                "selected-raised".modifier() to selected
                            )
                        }
                    )
                } else {
                    if (selectableVariant) {
                        cardSelection.setupClicks(this, cardId)
                        classMap(cardSelection.selected(cardId).map { mapOf("selected-raised".modifier() to it) })
                    }
                    if (expandable) {
                        with(expandedStore) {
                            toggleExpanded()
                        }
                    }
                }
            } else {
                if (expandable) {
                    with(expandedStore) {
                        toggleExpanded()
                    }
                }
            }

            renderComponents(this)
        }
    }

    private fun renderComponents(context: RenderContext) {
        with(context) {
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
 * Common base class for all components inside a card.
 */
public sealed class CardComponent<T>(baseClass: String?, id: String?, context: T.() -> Unit) :
    SubComponent<T>(baseClass, id, context) {

    internal abstract fun render(context: RenderContext)
}

/**
 * Card header. When used, defines the contents of a card. Card headers can contain images as well as the title of a card and an actions menu represented by the right-aligned kabob. In most cases, your card should include a header. The only exceptions are when cards being used as a layout element to create a white background behind other content.
 */
public class CardHeader internal constructor(
    private val selectableVariant: Boolean,
    private val cardSelection: CardSelection,
    private val expandedStore: ExpandedStore,
    baseClass: String?,
    id: String?
) : CardComponent<Div>(baseClass, id, {}) {

    private var check: Boolean = false
    private var title: CardTitle? = null
    private var actions: SubComponent<Div>? = null
    private var content: SubComponent<Div>? = null
    private var custom: SubComponent<Div>? = null
    internal lateinit var cardId: String
    internal var toggle: Boolean = false
    internal var toggleRight: Boolean = false

    public fun check() {
        this.check = true
    }

    public fun toggle() {
        this.toggle = true
    }

    public fun toggleRight() {
        this.toggleRight = true
    }

    /**
     * Sets the title of the header.
     */
    public fun title(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        this.title = CardTitle(baseClass, id, context)
    }

    /**
     * Adds an action container.
     */
    public fun actions(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit
    ) {
        this.actions = SubComponent(baseClass, id, context)
    }

    /**
     * Adds a container for the content of the header. If specified, the content container does not replace the [title], but is added in addition to the [title].
     */
    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit
    ) {
        this.content = SubComponent(baseClass, id, context)
    }

    /**
     * Sets a custom content of the header. Use this method if you need full control over the card's header.
     *
     * If specified, the [title] and [content] containers are ignored, but [actions] are respected in any case.
     */
    public fun custom(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit
    ) {
        this.custom = SubComponent(baseClass, id, context)
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
                if (toggle) {
                    renderToggle(this)
                }
                if (check || actions != null) {
                    renderActions(this)
                }
                if (custom != null) {
                    custom?.context?.invoke(this)
                } else {
                    content?.let { main ->
                        div(
                            baseClass = classes("card".component("header", "main"), main.baseClass),
                            id = main.id
                        ) {
                            main.context(this)
                        }
                    }
                    title?.render(this)
                }
                if (toggleRight) {
                    renderToggle(this)
                }
            }
        }
    }

    private fun renderActions(context: RenderContext) {
        with(context) {
            div(
                baseClass = classes("card".component("actions"), actions?.baseClass),
                id = actions?.id
            ) {
                if (selectableVariant) {
                    domNode.onclick = { it.stopPropagation() }
                }
                actions?.context?.invoke(this)
                if (check) {
                    // don't use checkbox component, since we need full control over the selection logic
                    div(
                        baseClass = classes {
                            +ComponentType.Checkbox
                            +"standalone".modifier()
                        }
                    ) {
                        input(baseClass = "check".component("input")) {
                            type("checkbox")
                            name(Id.unique(ComponentType.Card.id, "sel"))
                            checked(cardSelection.selected(cardId))
                            cardSelection.setupChanges(this, cardId)
                        }
                    }
                }
            }
        }
    }

    private fun renderToggle(context: RenderContext) {
        with(context) {
            div(baseClass = "card".component("header", "toggle")) {
                if (selectableVariant) {
                    domNode.onclick = { it.stopPropagation() }
                }
                clickButton(plain) {
                    title?.id?.let { id ->
                        element {
                            aria["labelledby"] = id
                        }
                    }
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

/**
 * Card title. Communicates the title of a card if it's not included in the header.
 */
public class CardTitle(baseClass: String?, id: String?, context: Div.() -> Unit) :
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

/**
 * Card body. Provides details about the item. A card body can include any combination of static text and/or active content.
 */
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

/**
 * Card footer. Contains links, actions, or static text at the bottom of a card.
 */
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

/**
 * Card separator. Separates multiple body components.
 */
public class CardDivider : CardComponent<Hr>(baseClass = null, id = null, context = {}) {

    override fun render(context: RenderContext) {
        with(context) {
            divider(HR)
        }
    }
}

// ------------------------------------------------------ card selection

public interface CardSelection {
    public val supported: Boolean
    public val selectionMode: SelectionMode

    public fun selected(id: String): Flow<Boolean>
    public fun setupClicks(tag: Tag<HTMLElement>, id: String)
    public fun setupChanges(input: Input, id: String)
}

internal class NoopCardSelection : CardSelection {
    override val supported: Boolean = false
    override val selectionMode: SelectionMode = SelectionMode.NONE

    override fun selected(id: String): Flow<Boolean> = emptyFlow()

    override fun setupClicks(tag: Tag<HTMLElement>, id: String) {
        // noop
    }

    override fun setupChanges(input: Input, id: String) {
        // noop
    }
}

internal class StandaloneCardSelection(private val selection: Store<Boolean>) : CardSelection {

    override val supported: Boolean = true
    override val selectionMode: SelectionMode = SelectionMode.SINGLE

    override fun selected(id: String): Flow<Boolean> = selection.data

    override fun setupClicks(tag: Tag<HTMLElement>, id: String) {
        with(tag) {
            domNode.tabIndex = 0
            clicks.map { !selection.current } handledBy selection.update
        }
    }

    override fun setupChanges(input: Input, id: String) {
        with(input) {
            changes.states() handledBy selection.update
        }
    }
}

internal class CardViewCardSelection(
    override val selectionMode: SelectionMode,
    internal val singleIdSelection: SingleIdStore,
    internal val multiIdSelection: MultiIdStore
) : CardSelection {

    override val supported: Boolean = true

    override fun selected(id: String): Flow<Boolean> =
        if (selectionMode == SelectionMode.SINGLE) {
            singleIdSelection.data.map { id == it }
        } else { // selectionMode == SelectionMode.MULTI
            multiIdSelection.data.map { id in it }
        }

    override fun setupClicks(tag: Tag<HTMLElement>, id: String) {
        with(tag) {
            domNode.tabIndex = 0
            if (selectionMode == SelectionMode.SINGLE) {
                clicks.map { id } handledBy singleIdSelection.update
            } else if (selectionMode == SelectionMode.MULTI) {
                clicks.map { id } handledBy multiIdSelection.toggle
            }
        }
    }

    override fun setupChanges(input: Input, id: String) {
        with(input) {
            if (selectionMode == SelectionMode.SINGLE) {
                changes.states().map { if (it) id else null } handledBy singleIdSelection.update
            } else { // selectionMode == SelectionMode.MULTI
                changes.states().map { id to it } handledBy multiIdSelection.select
            }
        }
    }
}

// ------------------------------------------------------ card variant

/**
 * Visual modifiers for [Card]s.
 *
 * @see <a href="https://www.patternfly.org/v4/components/card/design-guidelines#variations">https://www.patternfly.org/v4/components/card/design-guidelines#variations</a>
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class CardVariant(internal val modifier: String) {
    compact("compact".modifier()),
    flat("flat".modifier()),
    fullHeight("full-height".modifier()),
    hoverable("hoverable-raised".modifier()),
    large("display-lg".modifier()),
    plain("plain".modifier()),
    rounded("rounded".modifier()),
    selectable("selectable-raised".modifier()),
}
