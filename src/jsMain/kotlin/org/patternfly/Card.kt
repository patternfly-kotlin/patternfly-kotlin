package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfCard(classes: String? = null, content: Card.() -> Unit = {}): Card =
    register(Card(classes), content)

fun HtmlElements.pfCard(modifier: Modifier, content: Card.() -> Unit = {}): Card =
    register(Card(modifier.value), content)

fun Card.pfCardHeader(classes: String? = null, content: CardHeader.() -> Unit = {}): CardHeader =
    register(CardHeader(classes), content)

fun Card.pfCardHeader(modifier: Modifier, content: CardHeader.() -> Unit = {}): CardHeader =
    register(CardHeader(modifier.value), content)

fun CardHeader.pfCardHeaderMain(classes: String? = null, content: CardHeaderMain.() -> Unit = {}): CardHeaderMain =
    register(CardHeaderMain(classes), content)

fun CardHeader.pfCardHeaderMain(modifier: Modifier, content: CardHeaderMain.() -> Unit = {}): CardHeaderMain =
    register(CardHeaderMain(modifier.value), content)

fun CardHeader.pfCardActions(classes: String? = null, content: CardActions.() -> Unit = {}): CardActions =
    register(CardActions(classes), content)

fun CardHeader.pfCardActions(modifier: Modifier, content: CardActions.() -> Unit = {}): CardActions =
    register(CardActions(modifier.value), content)

fun CardHeader.pfCardTitle(classes: String? = null, content: CardTitle.() -> Unit = {}): CardTitle =
    register(CardTitle(classes), content)

fun CardHeader.pfCardTitle(modifier: Modifier, content: CardTitle.() -> Unit = {}): CardTitle =
    register(CardTitle(modifier.value), content)

fun Card.pfCardTitle(classes: String? = null, content: CardTitle.() -> Unit = {}): CardTitle =
    register(CardTitle(classes), content)

fun Card.pfCardTitle(modifier: Modifier, content: CardTitle.() -> Unit = {}): CardTitle =
    register(CardTitle(modifier.value), content)

fun Card.pfCardBody(classes: String? = null, content: CardBody.() -> Unit = {}): CardBody =
    register(CardBody(classes), content)

fun Card.pfCardBody(modifier: Modifier, content: CardBody.() -> Unit = {}): CardBody =
    register(CardBody(modifier.value), content)

fun Card.pfCardFooter(classes: String? = null, content: CardFooter.() -> Unit = {}): CardFooter =
    register(CardFooter(classes), content)

fun Card.pfCardFooter(modifier: Modifier, content: CardFooter.() -> Unit = {}): CardFooter =
    register(CardFooter(modifier.value), content)

// ------------------------------------------------------ tag

class Card(classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("article", baseClass = classes(ComponentType.Button, classes)) {
    init {
        markAs(ComponentType.Button)
    }
}

class CardHeader(classes: String?) : Div(baseClass = classes("card".component("header"), classes))

class CardHeaderMain(classes: String?) : Div(baseClass = classes("card".component("header", "main"), classes))

class CardActions(classes: String?) : Div(baseClass = classes("card".component("actions"), classes))

class CardTitle(classes: String?) : Div(baseClass = classes("card".component("title"), classes))

class CardBody(classes: String?) : Div(baseClass = classes("card".component("body"), classes))

class CardFooter(classes: String?) : Div(baseClass = classes("card".component("footer"), classes))
