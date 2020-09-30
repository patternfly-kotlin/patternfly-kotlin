package org.patternfly

import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Span
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfButton(
    id: String? = null,
    baseClass: String? = null,
    content: Button.() -> Unit = {}
): Button = register(Button(id = id, baseClass = baseClass), content)

fun HtmlElements.pfLinkButton(
    id: String? = null,
    baseClass: String? = null,
    content: LinkButton.() -> Unit = {}
): LinkButton = register(LinkButton(id = id, baseClass = baseClass), content)

fun Button.pfIcon(
    position: Position,
    iconClass: String,
    content: Icon.() -> Unit = {}
): Span = span(baseClass = classes("button".component("icon"), position.modifier)) {
    pfIcon(iconClass, content = content)
}

// ------------------------------------------------------ tag

class Button internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLButtonElement>,
    dev.fritz2.dom.html.Button(id = id, baseClass = classes(ComponentType.Button, baseClass)) {
    init {
        markAs(ComponentType.Button)
    }
}

class LinkButton internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLAnchorElement>,
    A(id = id, baseClass = classes(ComponentType.Button, baseClass)) {
    init {
        markAs(ComponentType.Button)
    }
}
