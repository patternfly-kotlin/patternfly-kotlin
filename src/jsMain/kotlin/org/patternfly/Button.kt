package org.patternfly

import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Span
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfButton(classes: String? = null, content: Button.() -> Unit = {}): Button =
    register(Button(classes), content)

fun HtmlElements.pfButton(modifier: Modifier, content: Button.() -> Unit = {}): Button =
    register(Button(modifier.value), content)

fun HtmlElements.pfLinkButton(classes: String? = null, content: LinkButton.() -> Unit = {}): LinkButton =
    register(LinkButton(classes), content)

fun HtmlElements.pfLinkButton(modifier: Modifier, content: LinkButton.() -> Unit = {}): LinkButton =
    register(LinkButton(modifier.value), content)

fun Button.pfIcon(position: Position, iconClass: String, content: Icon.() -> Unit = {}): Span =
    span(buildString { append("button".component("icon")).append(" ").append(position.modifier.value) }) {
        pfIcon(iconClass, content)
    }

// ------------------------------------------------------ tag

class Button internal constructor(classes: String?) :
    PatternFlyComponent<HTMLButtonElement>,
    dev.fritz2.dom.html.Button(baseClass = classes(ComponentType.Button, classes)) {
    init {
        markAs(ComponentType.Button)
    }
}

class LinkButton internal constructor(classes: String?) :
    PatternFlyComponent<HTMLAnchorElement>,
    A(baseClass = classes(ComponentType.Button, classes)) {
    init {
        markAs(ComponentType.Button)
    }
}
