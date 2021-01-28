@file:Suppress("TooManyFunctions")

package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.states
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.aria
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

/**
 * Creates a [Card] component which is part of an [CardView].
 *
 * @param item the item for the card
 * @param selectable whether the card is selectable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardViewSample.cardView
 */
public fun <T> CardView<T>.card(
    item: T,
    selectable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Card<T>.() -> Unit = {}
): Card<T> = register(Card(this.itemsStore, item, selectable, id = id, baseClass = baseClass, job), content)

/**
 * Creates a standalone [Card] component.
 *
 * @param selectable whether the card is selectable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.card
 */
public fun RenderContext.card(
    selectable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Card<Unit>.() -> Unit = {}
): Card<Unit> = register(Card(ItemsStore.NOOP, Unit, selectable, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardHeader] component inside a [Card] component. Use a header if you want to add images, actions or a checkbox to the card.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> Card<T>.cardHeader(
    id: String? = null,
    baseClass: String? = null,
    content: CardHeader<T>.() -> Unit = {}
): CardHeader<T> = register(CardHeader(this.itemsStore, this.item, this, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [CardToggle] component inside the [CardHeader] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.expandable
 */
public fun <T> CardHeader<T>.cardToggle(
    id: String? = null,
    baseClass: String? = null,
    content: CardToggle<T>.() -> Unit = {}
): CardToggle<T> =
    register(CardToggle(this.itemsStore, this.item, this.card, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardAction] component inside the [CardHeader] component. Use this function to group actions in the header.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> CardHeader<T>.cardAction(
    id: String? = null,
    baseClass: String? = null,
    content: CardAction<T>.() -> Unit = {}
): CardAction<T> =
    register(CardAction(this.itemsStore, this.item, this.card, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [CardCheckbox] inside the [CardAction]. If the card is selectable, the checkbox is bound to the [Card.selected] store (when used standalone) or the [ItemsStore] (when used as part of a [CardView]).
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> CardAction<T>.cardCheckbox(
    id: String? = null,
    baseClass: String? = null,
    content: CardCheckbox<T>.() -> Unit = {}
): CardCheckbox<T> =
    register(CardCheckbox(this.itemsStore, this.item, this.card, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardTitle] as part of the [CardHeader]. Use this function if you want to place the title in the header together with the actions and / or checkbox.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.cardTitleInHeader
 */
public fun <T> CardHeader<T>.cardTitle(
    id: String? = null,
    baseClass: String? = null,
    content: CardTitle<T>.() -> Unit = {}
): CardTitle<T> = register(CardTitle(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardTitle] as part of the [Card]. Use this function if you don't need a header.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.cardTitleInCard
 */
public fun <T> Card<T>.cardTitle(
    id: String? = null,
    baseClass: String? = null,
    content: CardTitle<T>.() -> Unit = {}
): CardTitle<T> = register(CardTitle(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [CardBody] component inside a [Card] component. You can have multiple bodies in one card.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.multipleBodies
 */
public fun <T> Card<T>.cardBody(
    id: String? = null,
    baseClass: String? = null,
    content: CardBody<T>.() -> Unit = {}
): CardBody<T> = register(CardBody(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardFooter] component inside a [Card] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> Card<T>.cardFooter(
    id: String? = null,
    baseClass: String? = null,
    content: CardFooter<T>.() -> Unit = {}
): CardFooter<T> = register(CardFooter(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates a container for the expandable content of a expandable [Card].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.expandable
 */
public fun <T> Card<T>.cardExpandableContent(
    id: String? = null,
    baseClass: String? = null,
    content: CardExpandableContent<T>.() -> Unit = {}
): CardExpandableContent<T> =
    register(CardExpandableContent(this.itemsStore, this, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [CardBody] component inside a [CardExpandableContent] component. You can have multiple bodies in one card.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> CardExpandableContent<T>.cardBody(
    id: String? = null,
    baseClass: String? = null,
    content: CardBody<T>.() -> Unit = {}
): CardBody<T> = register(CardBody(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardFooter] component inside a [CardExpandableContent] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> CardExpandableContent<T>.cardFooter(
    id: String? = null,
    baseClass: String? = null,
    content: CardFooter<T>.() -> Unit = {}
): CardFooter<T> = register(CardFooter(this.itemsStore, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [card](https://www.patternfly.org/v4/components/card/design-guidelines) component.
 *
 * A card can be used standalone or as part of a [CardView]. If used standalone and the card is created as [selectable], the card stores its selection state using the [selected] property. If the card is part of a [CardView], the selection is stored in the [ItemsStore].
 *
 * A card contains nested components which make up the card itself. The [CardTitle] can be placed either in the [CardHeader] or in the [Card] itself. If used in the [CardHeader], make sure to add it **after** the [CardAction]. Besides the [CardCheckbox] the [CardAction] can contain other control components such as [Dropdown]s or [PushButton]s.
 *
 * If you want to make the card expandable, add a [CardToggle] to the [CardHeader] and wrap the [CardBody] and [CardFooter] inside a [CardExpandableContent] component. Otherwise add the [CardBody] and [CardFooter] directly to the [Card] component.
 *
 * Most components should be used exactly once, except the [CardBody], which can be used multiple times.
 *
 * ```
 * ┏━━━━━━━━━━━━━━ card: Card ━━━━━━━━━━━━━━┓
 * ┃                                        ┃
 * ┃ ┌────── cardHeader: CardHeader ──────┐ ┃
 * ┃ │ ┌────────────────────────────────┐ │ ┃
 * ┃ │ │     cardToggle: CardToggle     │ │ ┃
 * ┃ │ └────────────────────────────────┘ │ ┃
 * ┃ │ ┌──── cardAction: CardAction ────┐ │ ┃
 * ┃ │ │ ┌────────────────────────────┐ │ │ ┃
 * ┃ │ │ │ cardCheckbox: CardCheckbox │ │ │ ┃
 * ┃ │ │ └────────────────────────────┘ │ │ ┃
 * ┃ │ └────────────────────────────────┘ │ ┃
 * ┃ │ ┌────────────────────────────────┐ │ ┃
 * ┃ │ │      cardTitle: CardTitle      │ │ ┃
 * ┃ │ └────────────────────────────────┘ │ ┃
 * ┃ └────────────────────────────────────┘ ┃
 * ┃ ┌────────────────────────────────────┐ ┃
 * ┃ │        cardTitle: CardTitle        │ ┃
 * ┃ └────────────────────────────────────┘ ┃
 * ┃                                        ┃
 * ┃ ┌────── cardExpandableContent: ──────┐ ┃
 * ┃ │       CardExpandableContent        │ ┃
 * ┃ │ ┌────────────────────────────────┐ │ ┃
 * ┃ │ │       cardBody: CardBody       │ │ ┃
 * ┃ │ └────────────────────────────────┘ │ ┃
 * ┃ │ ┌────────────────────────────────┐ │ ┃
 * ┃ │ │     cardFooter: CardFooter     │ │ ┃
 * ┃ │ └────────────────────────────────┘ │ ┃
 * ┃ └────────────────────────────────────┘ ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * ```
 *
 * @sample org.patternfly.sample.CardSample.card
 */
public class Card<T> internal constructor(
    internal val itemsStore: SelectableItemPageContents<T>,
    internal val item: T,
    internal val selectable: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLElement>,
    WithIdProvider<T> by itemsStore,
    TextElement(
        "article",
        id = id,
        baseClass = classes {
            +ComponentType.Card
            +("selectable".modifier() `when` selectable)
            +baseClass
        },
        job
    ) {

    /**
     * Stores the selection of this card if this card is used standalone (i.e. not as part of a [CardView])
     */
    public val selected: CardStore = CardStore()

    /**
     * Manages the expanded state of an expandable [Card]. Use this property if you want to track the collapse / expand state.
     *
     * @sample org.patternfly.sample.CardSample.expandable
     */
    public val expanded: ExpandedStore = ExpandedStore()

    init {
        markAs(ComponentType.Card)
        if (selectable) {
            domNode.tabIndex = 0
            if (itemsStore != ItemsStore.NOOP) {
                classMap(
                    itemsStore.isSelected(item).combine(expanded.data) { selected, expanded ->
                        selected to expanded
                    }.map { (selected, expanded) ->
                        mapOf(
                            "selected".modifier() to selected,
                            "expanded".modifier() to expanded
                        )
                    }
                )
                clicks.map { item } handledBy itemsStore.toggleSelection
            } else {
                classMap(
                    selected.data.combine(expanded.data) { selected, expanded ->
                        selected to expanded
                    }.map { (selected, expanded) ->
                        mapOf(
                            "selected".modifier() to selected,
                            "expanded".modifier() to expanded
                        )
                    }
                )
                clicks handledBy selected.toggle
            }
        } else {
            classMap(expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
        }
    }
}

/**
 * The header component of a [Card].
 */
public class CardHeader<T> internal constructor(
    internal val itemsStore: SelectableItemPageContents<T>,
    internal val item: T,
    internal val card: Card<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(id = id, baseClass = classes("card".component("header"), baseClass), job)

/**
 * The toggle control of a expandable card inside the [CardHeader].
 */
public class CardToggle<T> internal constructor(
    itemsStore: SelectableItemPageContents<T>,
    item: T,
    card: Card<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(id = id, baseClass = classes("card".component("header", "toggle"), baseClass), job) {

    init {
        clickButton(plain) {
            aria["label"] = "Details"
            if (itemsStore != ItemsStore.NOOP) {
                aria["labelledby"] = this@CardToggle.itemId(item)
            }
            attr("aria-expanded", card.expanded.data.map { it.toString() })
            span(baseClass = "card".component("header", "toggle", "icon")) {
                icon("angle-right".fas())
            }
        } handledBy card.expanded.toggle
    }
}

/**
 * A component to group actions in the [CardHeader]. Besides the [CardCheckbox] the [CardAction] can contain other control components such as [Dropdown]s or [PushButton]s.
 */
public class CardAction<T> internal constructor(
    internal val itemsStore: SelectableItemPageContents<T>,
    internal val item: T,
    internal val card: Card<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(id = id, baseClass = classes("card".component("actions"), baseClass), job) {

    init {
        if (card.selectable) {
            domNode.onclick = { it.stopPropagation() }
        }
    }
}

/**
 * Checkbox to (de)select a card. If the card is used standalone and is [selectable][Card.selectable], the checkbox is bound to a [CardStore]. If the card is part of a [CardView], the checkbox is bound to the selection state of the [ItemsStore].
 */
public class CardCheckbox<T> internal constructor(
    itemsStore: SelectableItemPageContents<T>,
    item: T,
    card: Card<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Input(id = id, baseClass = baseClass, job) {

    init {
        type("checkbox")
        aria["invalid"] = false
        if (itemsStore != ItemsStore.NOOP) {
            checked(itemsStore.isSelected(item))
            changes.states().map { Pair(item, it) } handledBy itemsStore.select
        } else {
            checked(card.selected.data)
            changes.states() handledBy card.selected.update
        }
    }
}

/**
 * The title of a [Card]. The title can be placed either in the [CardHeader] or in the [Card] itself. If used in the [CardHeader], make sure to add it **after** the [CardAction].
 *
 * @sample org.patternfly.sample.CardSample.cardTitleInHeader
 * @sample org.patternfly.sample.CardSample.cardTitleInCard
 */
public class CardTitle<T> internal constructor(itemsStore: SelectableItemPageContents<T>, id: String?, baseClass: String?, job: Job) :
    WithIdProvider<T> by itemsStore, Div(id = id, baseClass = classes("card".component("title"), baseClass), job)

/**
 * A container for the expandable content of a expandable [Card].
 *
 * @sample org.patternfly.sample.CardSample.expandable
 */
public class CardExpandableContent<T> internal constructor(
    internal val itemsStore: SelectableItemPageContents<T>,
    card: Card<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(id = id, baseClass = classes("card".component("expandable", "content"), baseClass), job) {

    init {
        attr("hidden", card.expanded.data.map { !it })
        classMap(card.expanded.data.map { expanded -> mapOf("display-none".util() to !expanded) })
    }
}

/**
 * The body of a [Card]. You can have multiple bodies in one card.
 *
 * @sample org.patternfly.sample.CardSample.multipleBodies
 */
public class CardBody<T> internal constructor(itemsStore: SelectableItemPageContents<T>, id: String?, baseClass: String?, job: Job) :
    WithIdProvider<T> by itemsStore, Div(id = id, baseClass = classes("card".component("body"), baseClass), job)

/**
 * The footer of a [Card].
 */
public class CardFooter<T> internal constructor(itemsStore: SelectableItemPageContents<T>, id: String?, baseClass: String?, job: Job) :
    WithIdProvider<T> by itemsStore, Div(id = id, baseClass = classes("card".component("footer"), baseClass), job)

// ------------------------------------------------------ store

/**
 * Store to hold the selection of a [Card] when used standalone.
 */
public class CardStore : RootStore<Boolean>(false) {
    internal val toggle = handle { !it }
}
