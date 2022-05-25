@file:Suppress("TooManyFunctions")

package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.states
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariant.plain
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

/**
 * Creates a [LegacyCard] component which is part of an [LegacyCardView].
 *
 * @param item the item for the card
 * @param selectable whether the card is selectable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardViewSample.cardView
 */
public fun <T> LegacyCardView<T>.legacyCard(
    item: T,
    selectable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCard<T>.() -> Unit = {}
): LegacyCard<T> = register(
    LegacyCard(
        itemsStore = this.itemsStore,
        item = item,
        selectable = selectable,
        singleSelection = this.singleSelection,
        id = id,
        baseClass = baseClass,
        job = job
    ),
    content
)

public fun RenderContext.legacyCard(
    selectable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCard<Unit>.() -> Unit = {}
): LegacyCard<Unit> = register(
    LegacyCard(
        itemsStore = ItemsStore.NOOP,
        item = Unit,
        selectable = selectable,
        singleSelection = false,
        id = id,
        baseClass = baseClass,
        job
    ),
    content
)

/**
 * Creates the [LegacyCardHeader] component inside a [LegacyCard] component. Use a header if you want to add images, actions or a checkbox to the card.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> LegacyCard<T>.legacyCardHeader(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardHeader<T>.() -> Unit = {}
): LegacyCardHeader<T> = register(
    LegacyCardHeader(this.itemsStore, this.item, this, id = id, baseClass = baseClass, job),
    content
)

/**
 * Creates a [LegacyCardToggle] component inside the [LegacyCardHeader] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.expandableCard
 */
public fun <T> LegacyCardHeader<T>.legacyCardToggle(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardToggle<T>.() -> Unit = {}
): LegacyCardToggle<T> =
    register(LegacyCardToggle(this.itemsStore, this.item, this.card, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [LegacyCardAction] component inside the [LegacyCardHeader] component. Use this function to group actions in the header.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> LegacyCardHeader<T>.legacyCardAction(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardAction<T>.() -> Unit = {}
): LegacyCardAction<T> =
    register(LegacyCardAction(this.itemsStore, this.item, this.card, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [LegacyCardCheckbox] inside the [LegacyCardAction]. If the card is selectable, the checkbox is bound to the [LegacyCard.selected] store (when used standalone) or the [ItemsStore] (when used as part of a [LegacyCardView]).
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> LegacyCardAction<T>.legacyCardCheckbox(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardCheckbox<T>.() -> Unit = {}
): LegacyCardCheckbox<T> =
    register(LegacyCardCheckbox(this.itemsStore, this.item, this.card, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [LegacyCardTitle] as part of the [LegacyCardHeader]. Use this function if you want to place the title in the header together with the actions and / or checkbox.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.titleInHeader
 */
public fun <T> LegacyCardHeader<T>.legacyCardTitle(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardTitle<T>.() -> Unit = {}
): LegacyCardTitle<T> = register(LegacyCardTitle(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [LegacyCardTitle] as part of the [LegacyCard]. Use this function if you don't need a header.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.noHeader
 */
public fun <T> LegacyCard<T>.legacyCardTitle(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardTitle<T>.() -> Unit = {}
): LegacyCardTitle<T> = register(LegacyCardTitle(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [LegacyCardBody] component inside a [LegacyCard] component. You can have multiple bodies in one card.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.multipleBodies
 */
public fun <T> LegacyCard<T>.legacyCardBody(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardBody<T>.() -> Unit = {}
): LegacyCardBody<T> = register(LegacyCardBody(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [LegacyCardFooter] component inside a [LegacyCard] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> LegacyCard<T>.legacyCardFooter(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardFooter<T>.() -> Unit = {}
): LegacyCardFooter<T> = register(LegacyCardFooter(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates a container for the expandable content of a expandable [LegacyCard].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardSample.expandableCard
 */
public fun <T> LegacyCard<T>.legacyCardExpandableContent(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardExpandableContent<T>.() -> Unit = {}
): LegacyCardExpandableContent<T> =
    register(LegacyCardExpandableContent(this.itemsStore, this, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [LegacyCardBody] component inside a [LegacyCardExpandableContent] component. You can have multiple bodies in one card.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> LegacyCardExpandableContent<T>.legacyCardBody(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardBody<T>.() -> Unit = {}
): LegacyCardBody<T> = register(LegacyCardBody(this.itemsStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [LegacyCardFooter] component inside a [LegacyCardExpandableContent] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> LegacyCardExpandableContent<T>.legacyCardFooter(
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardFooter<T>.() -> Unit = {}
): LegacyCardFooter<T> = register(LegacyCardFooter(this.itemsStore, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [card](https://www.patternfly.org/v4/components/card/design-guidelines) component.
 *
 * A card can be used standalone or as part of a [LegacyCardView]. If used standalone and the card is created as [selectable], the card stores its selection state using the [selected] property. If the card is part of a [LegacyCardView], the selection is stored in the [ItemsStore].
 *
 * A card contains nested components which make up the card itself. The [LegacyCardTitle] can be placed either in the [LegacyCardHeader] or in the [LegacyCard] itself. If used in the [LegacyCardHeader], make sure to add it **after** the [LegacyCardAction]. Besides the [LegacyCardCheckbox] the [LegacyCardAction] can contain other control components such as [Dropdown]s or [PushButton]s.
 *
 * If you want to make the card expandable, add a [LegacyCardToggle] to the [LegacyCardHeader] and wrap the [LegacyCardBody] and [LegacyCardFooter] inside a [LegacyCardExpandableContent] component. Otherwise add the [LegacyCardBody] and [LegacyCardFooter] directly to the [LegacyCard] component.
 *
 * Most components should be used exactly once, except the [LegacyCardBody], which can be used multiple times.
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
 * @sample org.patternfly.sample.CardSample.imageInHeader
 */
public class LegacyCard<T> internal constructor(
    internal val itemsStore: ItemsStore<T>,
    internal val item: T,
    internal val selectable: Boolean,
    internal val singleSelection: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyElement<HTMLElement>,
    WithIdProvider<T> by itemsStore,
    TextElement(
        "article",
        id = id,
        baseClass = classes {
            +ComponentType.Card
            +("selectable".modifier() `when` selectable)
            +baseClass
        },
        job,
        scope = Scope()
    ) {

    /**
     * Stores the selection of this card if this card is used standalone (i.e. not as part of a [LegacyCardView])
     */
    public val selected: LegacyCardStore = LegacyCardStore()

    /**
     * Manages the expanded state of an expandable [LegacyCard]. Use this property if you want to track the collapse / expand state.
     *
     * @sample org.patternfly.sample.CardSample.expandableCard
     */
    public val expanded: ExpandedStore = ExpandedStore()

    init {
        markAs(ComponentType.Card)
        if (selectable) {
            domNode.tabIndex = 0
            if (itemsStore != ItemsStore.NOOP) {
                classMap(
                    itemsStore.data.map { it.isSelected(item) }.combine(expanded.data) { selected, expanded ->
                        selected to expanded
                    }.map { (selected, expanded) ->
                        mapOf(
                            "selected".modifier() to selected,
                            "expanded".modifier() to expanded
                        )
                    }
                )
                if (singleSelection) {
                    clicks.map { item } handledBy itemsStore.selectOnly
                } else {
                    clicks.map { item } handledBy itemsStore.toggleSelection
                }
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
 * The header component of a [LegacyCard].
 */
public class LegacyCardHeader<T> internal constructor(
    internal val itemsStore: ItemsStore<T>,
    internal val item: T,
    internal val card: LegacyCard<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(
        id = id, baseClass = classes("card".component("header"), baseClass), job,
        scope = Scope()
    )

/**
 * The toggle control of a expandable card inside the [LegacyCardHeader].
 */
public class LegacyCardToggle<T> internal constructor(
    itemsStore: ItemsStore<T>,
    item: T,
    card: LegacyCard<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(
        id = id, baseClass = classes("card".component("header", "toggle"), baseClass), job,
        scope = Scope()
    ) {

    init {
        clickButton(plain) {
            aria["label"] = "Details"
            if (itemsStore != ItemsStore.NOOP) {
                aria["labelledby"] = this@LegacyCardToggle.itemId(item)
            }
            attr("aria-expanded", card.expanded.data.map { it.toString() })
            span(baseClass = "card".component("header", "toggle", "icon")) {
                icon("angle-right".fas())
            }
        } handledBy card.expanded.toggle
    }
}

/**
 * A component to group actions in the [LegacyCardHeader]. Besides the [LegacyCardCheckbox] the [LegacyCardAction] can contain other control components such as [Dropdown]s or [PushButton]s.
 */
public class LegacyCardAction<T> internal constructor(
    internal val itemsStore: ItemsStore<T>,
    internal val item: T,
    internal val card: LegacyCard<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(
    id = id, baseClass = classes("card".component("actions"), baseClass), job,
    scope = Scope()
) {

    init {
        if (card.selectable) {
            domNode.onclick = { it.stopPropagation() }
        }
    }
}

/**
 * Checkbox to (de)select a card. If the card is used standalone and is [selectable][LegacyCard.selectable], the checkbox is bound to a [LegacyCardStore]. If the card is part of a [LegacyCardView], the checkbox is bound to the selection state of the [ItemsStore].
 */
public class LegacyCardCheckbox<T> internal constructor(
    itemsStore: ItemsStore<T>,
    item: T,
    card: LegacyCard<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Input(
    id = id, baseClass = baseClass, job,
    scope = Scope()
) {

    init {
        type("checkbox")
        aria["invalid"] = false
        if (itemsStore != ItemsStore.NOOP) {
            checked(itemsStore.data.map { it.isSelected(item) })
            if (card.singleSelection) {
                // TODO Implement single selection
            } else {
                changes.states().map { Pair(item, it) } handledBy itemsStore.select
            }
        } else {
            checked(card.selected.data)
            changes.states() handledBy card.selected.update
        }
    }
}

/**
 * The title of a [LegacyCard]. The title can be placed either in the [LegacyCardHeader] or in the [LegacyCard] itself. If used in the [LegacyCardHeader], make sure to add it **after** the [LegacyCardAction].
 *
 * @sample org.patternfly.sample.CardSample.titleInHeader
 * @sample org.patternfly.sample.CardSample.noHeader
 */
public class LegacyCardTitle<T> internal constructor(
    itemsStore: ItemsStore<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(
        id = id, baseClass = classes("card".component("title"), baseClass), job,
        scope = Scope()
    )

/**
 * A container for the expandable content of a expandable [LegacyCard].
 *
 * @sample org.patternfly.sample.CardSample.expandableCard
 */
public class LegacyCardExpandableContent<T> internal constructor(
    internal val itemsStore: ItemsStore<T>,
    card: LegacyCard<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(
        id = id, baseClass = classes("card".component("expandable", "content"), baseClass), job,
        scope = Scope()
    ) {

    init {
        attr("hidden", card.expanded.data.map { !it })
        classMap(card.expanded.data.map { expanded -> mapOf("display-none".util() to !expanded) })
    }
}

/**
 * The body of a [LegacyCard]. You can have multiple bodies in one card.
 *
 * @sample org.patternfly.sample.CardSample.multipleBodies
 */
public class LegacyCardBody<T> internal constructor(
    itemsStore: ItemsStore<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(
        id = id, baseClass = classes("card".component("body"), baseClass), job,
        scope = Scope()
    )

/**
 * The footer of a [LegacyCard].
 */
public class LegacyCardFooter<T> internal constructor(
    itemsStore: ItemsStore<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : WithIdProvider<T> by itemsStore,
    Div(
        id = id, baseClass = classes("card".component("footer"), baseClass), job,
        scope = Scope()
    )

// ------------------------------------------------------ store

/**
 * Store to hold the selection of a [LegacyCard] when used standalone.
 */
public class LegacyCardStore : RootStore<Boolean>(false) {
    internal val toggle = handle { !it }
}
