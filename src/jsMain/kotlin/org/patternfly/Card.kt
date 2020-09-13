package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.const
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.states
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfCard(selectable: Boolean = false, classes: String? = null, content: Card.() -> Unit = {}): Card =
    register(Card(selectable, classes), content)

fun Card.pfCardHeader(classes: String? = null, content: CardHeader.() -> Unit = {}): CardHeader =
    register(CardHeader(this, classes), content)

fun CardHeader.pfCardHeaderMain(classes: String? = null, content: CardHeaderMain.() -> Unit = {}): CardHeaderMain =
    register(CardHeaderMain(classes), content)

fun CardHeader.pfCardActions(classes: String? = null, content: CardActions.() -> Unit = {}): CardActions =
    register(CardActions(this.card, classes), content)

fun CardHeader.pfCardTitle(classes: String? = null, content: CardTitle.() -> Unit = {}): CardTitle =
    register(CardTitle(classes), content)

fun CardActions.pfCardCheckbox(classes: String? = null, content: CardCheckbox.() -> Unit = {}): CardCheckbox =
    register(CardCheckbox(this.card, classes), content)

fun Card.pfCardTitle(classes: String? = null, content: CardTitle.() -> Unit = {}): CardTitle =
    register(CardTitle(classes), content)

fun Card.pfCardBody(classes: String? = null, content: CardBody.() -> Unit = {}): CardBody =
    register(CardBody(classes), content)

fun Card.pfCardFooter(classes: String? = null, content: CardFooter.() -> Unit = {}): CardFooter =
    register(CardFooter(classes), content)

// ------------------------------------------------------ tag

class Card(internal val selectable: Boolean, classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("article", baseClass = classes {
        +ComponentType.Card
        +("selectable".modifier() `when` selectable)
        +classes
    }) {

    val selected = CardStore()

    init {
        markAs(ComponentType.Card)
        if (selectable) {
            domNode.tabIndex = 0
            classMap = selected.data.map { mapOf("selected".modifier() to it) }
            clicks handledBy selected.toggle
        }
    }
}

class CardHeader(internal val card: Card, classes: String?) :
    Div(baseClass = classes("card".component("header"), classes))

class CardHeaderMain(classes: String?) :
    Div(baseClass = classes("card".component("header", "main"), classes))

class CardActions(internal val card: Card, classes: String?) :
    Div(baseClass = classes("card".component("actions"), classes)) {
    init {
        if (card.selectable) {
            domNode.onclick = { it.stopPropagation() }
        }
    }
}

class CardCheckbox(card: Card, classes: String?) :
    Input(baseClass = classes) {
    init {
        type = const("checkbox")
        if (card.selectable) {
            checked = card.selected.data
            changes.states() handledBy card.selected.update
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
