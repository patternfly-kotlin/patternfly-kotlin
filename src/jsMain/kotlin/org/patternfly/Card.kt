package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.states
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ card view dsl

fun <T> HtmlElements.pfCardView(
    store: ItemStore<T>,
    classes: String? = null,
    content: CardView<T>.() -> Unit = {}
): CardView<T> = register(CardView(store, classes), content)

fun <T> CardView<T>.pfCard(
    item: T,
    selectable: Boolean = false,
    classes: String? = null,
    content: Card<T>.() -> Unit = {}
): Card<T> = register(Card(this.itemStore, item, selectable, classes), content)

fun <T> Card<T>.pfCardHeader(
    classes: String? = null,
    content: CardHeader<T>.() -> Unit = {}
): CardHeader<T> = register(CardHeader(this.itemStore, this.item, this, classes), content)

fun <T> CardHeader<T>.pfCardHeaderMain(
    classes: String? = null,
    content: CardHeaderMain<T>.() -> Unit = {}
): CardHeaderMain<T> = register(CardHeaderMain(this.itemStore, this.item, classes), content)

fun <T> CardHeader<T>.pfCardActions(
    classes: String? = null,
    content: CardActions<T>.() -> Unit = {}
): CardActions<T> = register(CardActions(this.itemStore, this.item, this.card, classes), content)

fun <T> CardHeader<T>.pfCardTitle(
    classes: String? = null,
    content: CardTitle.() -> Unit = {}
): CardTitle = register(CardTitle(classes), content)

fun <T> CardActions<T>.pfCardCheckbox(
    classes: String? = null,
    content: CardCheckbox<T>.() -> Unit = {}
): CardCheckbox<T> = register(CardCheckbox(this.itemStore, this.item, this.card, classes), content)

fun <T> Card<T>.pfCardTitle(
    classes: String? = null,
    content: CardTitle.() -> Unit = {}
): CardTitle = register(CardTitle(classes), content)

fun <T> Card<T>.pfCardBody(
    classes: String? = null,
    content: CardBody.() -> Unit = {}
): CardBody = register(CardBody(classes), content)

fun <T> Card<T>.pfCardFooter(
    classes: String? = null,
    content: CardFooter.() -> Unit = {}
): CardFooter = register(CardFooter(classes), content)

// ------------------------------------------------------ plain card dsl

fun HtmlElements.pfCard(
    selectable: Boolean = false,
    classes: String? = null,
    content: Card<Unit>.() -> Unit = {}
): Card<Unit> = register(Card(null, null, selectable, classes), content)

fun Card<Unit>.pfCardHeader(
    classes: String? = null,
    content: CardHeader<Unit>.() -> Unit = {}
): CardHeader<Unit> = register(CardHeader(null, null, this, classes), content)

fun CardHeader<Unit>.pfCardHeaderMain(
    classes: String? = null,
    content: CardHeaderMain<Unit>.() -> Unit = {}
): CardHeaderMain<Unit> = register(CardHeaderMain(null, null, classes), content)

fun CardHeader<Unit>.pfCardActions(
    classes: String? = null,
    content: CardActions<Unit>.() -> Unit = {}
): CardActions<Unit> = register(CardActions(null, null, this.card, classes), content)

fun CardHeader<Unit>.pfCardTitle(
    classes: String? = null,
    content: CardTitle.() -> Unit = {}
): CardTitle = register(CardTitle(classes), content)

fun CardActions<Unit>.pfCardCheckbox(
    classes: String? = null,
    content: CardCheckbox<Unit>.() -> Unit = {}
): CardCheckbox<Unit> = register(CardCheckbox(null, null, this.card, classes), content)

fun Card<Unit>.pfCardTitle(
    classes: String? = null,
    content: CardTitle.() -> Unit = {}
): CardTitle = register(CardTitle(classes), content)

fun Card<Unit>.pfCardBody(
    classes: String? = null,
    content: CardBody.() -> Unit = {}
): CardBody = register(CardBody(classes), content)

fun Card<Unit>.pfCardFooter(
    classes: String? = null,
    content: CardFooter.() -> Unit = {}
): CardFooter = register(CardFooter(classes), content)

// ------------------------------------------------------ tag

class CardView<T> internal constructor(internal val itemStore: ItemStore<T>, classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +"gallery".layout()
    +"gutter".modifier()
    +classes
}) {
    lateinit var display: (T) -> Card<T>

    init {
        markAs(ComponentType.CardView)
        itemStore.visible.each { itemStore.identifier(it) }.render { item ->
            display(item)
        }.bind()
    }
}

class Card<T> internal constructor(
    internal val itemStore: ItemStore<T>?,
    internal val item: T?,
    internal val selectable: Boolean,
    classes: String?
) : PatternFlyComponent<HTMLElement>, TextElement("article", baseClass = classes {
    +ComponentType.Card
    +("selectable".modifier() `when` selectable)
    +classes
}) {
    val selected = CardStore()

    init {
        markAs(ComponentType.Card)
        if (selectable) {
            domNode.tabIndex = 0
            if (itemStore != null && item != null) {
                classMap = itemStore.data
                    .map { it.isSelected(item) }
                    .map { mapOf("selected".modifier() to it) }
                clicks.map { item } handledBy itemStore.toggleSelection
            } else {
                classMap = selected.data.map { mapOf("selected".modifier() to it) }
                clicks handledBy selected.toggle
            }
        }
    }
}

class CardHeader<T> internal constructor(
    internal val itemStore: ItemStore<T>?,
    internal val item: T?,
    internal val card: Card<T>,
    classes: String?
) : Div(baseClass = classes("card".component("header"), classes))

class CardHeaderMain<T> internal constructor(
    internal val itemStore: ItemStore<T>?,
    internal val item: T?,
    classes: String?
) : Div(baseClass = classes("card".component("header", "main"), classes))

class CardActions<T> internal constructor(
    internal val itemStore: ItemStore<T>?,
    internal val item: T?,
    internal val card: Card<T>,
    classes: String?
) : Div(baseClass = classes("card".component("actions"), classes)) {
    init {
        if (card.selectable) {
            domNode.onclick = { it.stopPropagation() }
        }
    }
}

class CardCheckbox<T> internal constructor(itemStore: ItemStore<T>?, item: T?, card: Card<T>, classes: String?) :
    Div(baseClass = classes("check".component(), classes)) {
    init {
        input(baseClass = "check".component("input")) {
            domNode.type = "checkbox"
//            type = const("checkbox") // this causes visual flickering
            aria["invalid"] = false
            if (itemStore != null && item != null) {
                checked = itemStore.data.map { it.isSelected(item) }
                changes.states().map { Pair(item, it) } handledBy itemStore.select
            } else {
                checked = card.selected.data
                changes.states() handledBy card.selected.update
            }
        }
    }
}

class CardTitle(classes: String?) :
    Div(baseClass = classes("card".component("title"), classes))

class CardBody(classes: String?) :
    Div(baseClass = classes("card".component("body"), classes))

class CardFooter(classes: String?) :
    Div(baseClass = classes("card".component("footer"), classes))

// ------------------------------------------------------ store

class CardStore : RootStore<Boolean>(false) {
    internal val toggle = handle { !it }
}
