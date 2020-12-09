package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.states
import org.patternfly.dom.aria
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

// TODO Implement expandable cards
// ------------------------------------------------------ card view dsl

/**
 * Creates a [CardView] component.
 *
 * @param store the item store
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.cardView(
    store: ItemStore<T> = ItemStore(),
    id: String? = null,
    baseClass: String? = null,
    content: CardView<T>.() -> Unit = {}
): CardView<T> = register(CardView(store, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [Card] component which is part of an [CardView].
 *
 * @param item the item for the card
 * @param selectable whether the card is selectable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> CardView<T>.card(
    item: T,
    selectable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Card<T>.() -> Unit = {}
): Card<T> = register(Card(this.itemStore, item, selectable, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ card dsl

/**
 * Creates a standalone [Card] component.
 *
 * @param selectable whether the card is selectable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.card(
    selectable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Card<Unit>.() -> Unit = {}
): Card<Unit> = register(Card(ItemStore.NOOP, Unit, selectable, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardHeader] container inside a [Card] component. Use a header if you want to add images, actions or a checkbox to the card.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> Card<T>.cardHeader(
    id: String? = null,
    baseClass: String? = null,
    content: CardHeader<T>.() -> Unit = {}
): CardHeader<T> = register(CardHeader(this.itemStore, this.item, this, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardHeaderMain] container inside the [CardHeader]. Use this function if you want to add images or other none-text like elements to the header.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.CardSample.cardHeaderMain
 */
public fun <T> CardHeader<T>.cardHeaderMain(
    id: String? = null,
    baseClass: String? = null,
    content: CardHeaderMain<T>.() -> Unit = {}
): CardHeaderMain<T> = register(CardHeaderMain(this.itemStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardActions] container inside the [CardHeader]. Use this function to group actions in the header.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> CardHeader<T>.cardActions(
    id: String? = null,
    baseClass: String? = null,
    content: CardActions<T>.() -> Unit = {}
): CardActions<T> =
    register(CardActions(this.itemStore, this.item, this.card, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [CardCheckbox] inside the [CardActions]. If the card is selectable, the checkbox is bound to the [Card.selected] store (when used standalone) or the [ItemStore] (when used as part of a [CardView]).
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> CardActions<T>.cardCheckbox(
    id: String? = null,
    baseClass: String? = null,
    content: CardCheckbox<T>.() -> Unit = {}
): CardCheckbox<T> =
    register(CardCheckbox(this.itemStore, this.item, this.card, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardTitle] as part of the [CardHeader]. Use this function if you want to place the title in the header together with the actions and / or checkbox.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.CardSample.cardTitleInHeader
 */
public fun <T> CardHeader<T>.cardTitle(
    id: String? = null,
    baseClass: String? = null,
    content: CardTitle<T>.() -> Unit = {}
): CardTitle<T> = register(CardTitle(this.itemStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardTitle] as part of the [Card]. Use this function if you don't need a header.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.CardSample.cardTitleInCard
 */
public fun <T> Card<T>.cardTitle(
    id: String? = null,
    baseClass: String? = null,
    content: CardTitle<T>.() -> Unit = {}
): CardTitle<T> = register(CardTitle(this.itemStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [CardBody] container inside a [Card] component. You can have multiple bodies in one card.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.CardSample.multipleBodies
 */
public fun <T> Card<T>.cardBody(
    id: String? = null,
    baseClass: String? = null,
    content: CardBody<T>.() -> Unit = {}
): CardBody<T> = register(CardBody(this.itemStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [CardFooter] container inside a [Card] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> Card<T>.cardFooter(
    id: String? = null,
    baseClass: String? = null,
    content: CardFooter<T>.() -> Unit = {}
): CardFooter<T> = register(CardFooter(this.itemStore, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [card view](https://www.patternfly.org/v4/components/card/design-guidelines/#card-view-usage) component.
 *
 * A card view is a grid of cards that displays a small to moderate amount of content. The card view uses a [display] function to render the items in the [ItemStore] as [Card]s. One of the elements in the [display] should use the [ItemStore.idProvider] to assign an element ID. This ID is referenced by various [ARIA labelledby](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-labelledby_attribute) attributes.
 *
 * @param T the type which is used for the [Card]s in this card view.
 *
 * @sample org.patternfly.CardViewSample.cardView
 */
public class CardView<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
    +"gallery".layout()
    +"gutter".modifier()
    +baseClass
}, job) {

    init {
        markAs(ComponentType.CardView)
    }

    /**
     * Defines how to display the items in the [ItemStore] as [Card]s. Call this function *before* populating the store.
     */
    public fun display(display: (T) -> Card<T>) {
        itemStore.visible.renderEach({ itemStore.idProvider(it) }, { item ->
            display(item)
        })
    }
}

/**
 * PatternFly [card](https://www.patternfly.org/v4/components/card/design-guidelines) component.
 *
 * A card can be used standalone or as part of a [CardView]. If used standalone and the card is [selectable], the card stores its selection state using the [selected] property. If the card is part of a [CardView], the selection is stored in the [ItemStore].
 *
 * @sample org.patternfly.CardSample.card
 */
public class Card<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    internal val selectable: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLElement>,
    WithIdProvider<T> by itemStore,
    TextElement("article", id = id, baseClass = classes {
        +ComponentType.Card
        +("selectable".modifier() `when` selectable)
        +baseClass
    }, job) {

    /**
     * Stores the selection of this card if this card is used standalone (i.e. not as part of a [CardView])
     */
    public val selected: CardStore = CardStore()

    init {
        markAs(ComponentType.Card)
        if (selectable) {
            domNode.tabIndex = 0
            if (itemStore != ItemStore.NOOP) {
                classMap(itemStore.data
                    .map { it.isSelected(item) }
                    .map { mapOf("selected".modifier() to it) })
                clicks.map { item } handledBy itemStore.toggleSelection
            } else {
                classMap(selected.data.map { mapOf("selected".modifier() to it) })
                clicks handledBy selected.toggle
            }
        }
    }
}

/**
 * The header container of a [Card].
 */
public class CardHeader<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    internal val card: Card<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemStore,
    Div(id = id, baseClass = classes("card".component("header"), baseClass), job)

/**
 * A container in the [CardHeader] used to to add images or other none-text like elements to the header.
 *
 * @sample org.patternfly.CardSample.cardHeaderMain
 */
public class CardHeaderMain<T> internal constructor(
    itemStore: ItemStore<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemStore,
    Div(id = id, baseClass = classes("card".component("header", "main"), baseClass), job)

/**
 * A container to group actions in the [CardHeader].
 */
public class CardActions<T> internal constructor(
    internal val itemStore: ItemStore<T>,
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
 * Checkbox to (de)select a card. If the card is used standalone and is [selectable][Card.selectable], the checkbox is bound to the [Card.selected] store. If the card is part of a [CardView], the checkbox is bound to the selection state of the [ItemStore].
 */
public class CardCheckbox<T> internal constructor(
    itemStore: ItemStore<T>,
    item: T,
    card: Card<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Input(id = id, baseClass = baseClass, job) {

    init {
        type("checkbox")
        aria["invalid"] = false
        if (itemStore != ItemStore.NOOP) {
            checked(itemStore.data.map { it.isSelected(item) })
            changes.states().map { Pair(item, it) } handledBy itemStore.select
        } else {
            checked(card.selected.data)
            changes.states() handledBy card.selected.update
        }
    }
}

/**
 * The title of a [Card].
 */
public class CardTitle<T> internal constructor(itemStore: ItemStore<T>, id: String?, baseClass: String?, job: Job) :
    WithIdProvider<T> by itemStore, Div(id = id, baseClass = classes("card".component("title"), baseClass), job)

/**
 * The body of a [Card]. You can have multiple bodies in one card.
 *
 * @sample org.patternfly.CardSample.multipleBodies
 */
public class CardBody<T> internal constructor(itemStore: ItemStore<T>, id: String?, baseClass: String?, job: Job) :
    WithIdProvider<T> by itemStore, Div(id = id, baseClass = classes("card".component("body"), baseClass), job)


/**
 * The title of a [Card].
 */
public class CardFooter<T> internal constructor(itemStore: ItemStore<T>, id: String?, baseClass: String?, job: Job) :
    WithIdProvider<T> by itemStore, Div(id = id, baseClass = classes("card".component("footer"), baseClass), job)

// ------------------------------------------------------ store

/**
 * Store to hold the selection of a [Card] when used standalone.
 */
public class CardStore : RootStore<Boolean>(false) {
    internal val toggle = handle { !it }
}
