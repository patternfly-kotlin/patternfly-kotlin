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
    id: String? = null,
    classes: String? = null,
    content: CardView<T>.() -> Unit = {}
): CardView<T> = register(CardView(store, id = id, classes = classes), content)

fun <T> CardView<T>.pfCard(
    item: T,
    selectable: Boolean = false,
    id: String? = null,
    classes: String? = null,
    content: Card<T>.() -> Unit = {}
): Card<T> = register(Card(this.itemStore, item, selectable, id = id, classes = classes), content)

fun <T> Card<T>.pfCardHeader(
    id: String? = null,
    classes: String? = null,
    content: CardHeader<T>.() -> Unit = {}
): CardHeader<T> = register(CardHeader(this.itemStore, this.item, this, id = id, classes = classes), content)

fun <T> CardHeader<T>.pfCardHeaderMain(
    id: String? = null,
    classes: String? = null,
    content: CardHeaderMain<T>.() -> Unit = {}
): CardHeaderMain<T> = register(CardHeaderMain(this.item, id = id, classes = classes), content)

fun <T> CardHeader<T>.pfCardActions(
    id: String? = null,
    classes: String? = null,
    content: CardActions<T>.() -> Unit = {}
): CardActions<T> = register(CardActions(this.itemStore, this.item, this.card, id = id, classes = classes), content)

fun <T> CardHeader<T>.pfCardTitle(
    id: String? = null,
    classes: String? = null,
    content: CardTitle.() -> Unit = {}
): CardTitle = register(CardTitle(id = id, classes = classes), content)

fun <T> CardActions<T>.pfCardCheckbox(
    id: String? = null,
    classes: String? = null,
    content: CardCheckbox<T>.() -> Unit = {}
): CardCheckbox<T> = register(CardCheckbox(this.itemStore, this.item, this.card, id = id, classes = classes), content)

fun <T> Card<T>.pfCardTitle(
    id: String? = null,
    classes: String? = null,
    content: CardTitle.() -> Unit = {}
): CardTitle = register(CardTitle(id = id, classes = classes), content)

fun <T> Card<T>.pfCardBody(
    id: String? = null,
    classes: String? = null,
    content: CardBody.() -> Unit = {}
): CardBody = register(CardBody(id = id, classes = classes), content)

fun <T> Card<T>.pfCardFooter(
    id: String? = null,
    classes: String? = null,
    content: CardFooter.() -> Unit = {}
): CardFooter = register(CardFooter(id = id, classes = classes), content)

// ------------------------------------------------------ plain card dsl

fun HtmlElements.pfCard(
    selectable: Boolean = false,
    id: String? = null,
    classes: String? = null,
    content: Card<Unit>.() -> Unit = {}
): Card<Unit> = register(Card(null, null, selectable, id = id, classes = classes), content)

fun Card<Unit>.pfCardHeader(
    id: String? = null,
    classes: String? = null,
    content: CardHeader<Unit>.() -> Unit = {}
): CardHeader<Unit> = register(CardHeader(null, null, this, id = id, classes = classes), content)

fun CardHeader<Unit>.pfCardHeaderMain(
    id: String? = null,
    classes: String? = null,
    content: CardHeaderMain<Unit>.() -> Unit = {}
): CardHeaderMain<Unit> = register(CardHeaderMain(null, null, classes), content)

fun CardHeader<Unit>.pfCardActions(
    id: String? = null,
    classes: String? = null,
    content: CardActions<Unit>.() -> Unit = {}
): CardActions<Unit> = register(CardActions(null, null, this.card, id = id, classes = classes), content)

fun CardHeader<Unit>.pfCardTitle(
    id: String? = null,
    classes: String? = null,
    content: CardTitle.() -> Unit = {}
): CardTitle = register(CardTitle(id = id, classes = classes), content)

fun CardActions<Unit>.pfCardCheckbox(
    id: String? = null,
    classes: String? = null,
    content: CardCheckbox<Unit>.() -> Unit = {}
): CardCheckbox<Unit> = register(CardCheckbox(null, null, this.card, id = id, classes = classes), content)

fun Card<Unit>.pfCardTitle(
    id: String? = null,
    classes: String? = null,
    content: CardTitle.() -> Unit = {}
): CardTitle = register(CardTitle(id = id, classes = classes), content)

fun Card<Unit>.pfCardBody(
    id: String? = null,
    classes: String? = null,
    content: CardBody.() -> Unit = {}
): CardBody = register(CardBody(id = id, classes = classes), content)

fun Card<Unit>.pfCardFooter(
    id: String? = null,
    classes: String? = null,
    content: CardFooter.() -> Unit = {}
): CardFooter = register(CardFooter(id = id, classes = classes), content)

// ------------------------------------------------------ tag

class CardView<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    id: String?,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
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
    id: String?,
    classes: String?
) : PatternFlyComponent<HTMLElement>, TextElement("article", id = id, baseClass = classes {
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
    id: String?,
    classes: String?
) : Div(id = id, baseClass = classes("card".component("header"), classes))

class CardHeaderMain<T> internal constructor(
    internal val item: T?,
    id: String?,
    classes: String?
) : Div(id = id, baseClass = classes("card".component("header", "main"), classes))

class CardActions<T> internal constructor(
    internal val itemStore: ItemStore<T>?,
    internal val item: T?,
    internal val card: Card<T>,
    id: String?,
    classes: String?
) : Div(id = id, baseClass = classes("card".component("actions"), classes)) {
    init {
        if (card.selectable) {
            domNode.onclick = { it.stopPropagation() }
        }
    }
}

class CardCheckbox<T> internal constructor(
    itemStore: ItemStore<T>?,
    item: T?,
    card: Card<T>,
    id: String?,
    classes: String?
) : Div(id = id, baseClass = classes("check".component(), classes)) {
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

class CardTitle(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("card".component("title"), classes))

class CardBody(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("card".component("body"), classes))

class CardFooter(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("card".component("footer"), classes))

// ------------------------------------------------------ store

class CardStore : RootStore<Boolean>(false) {
    internal val toggle = handle { !it }
}
