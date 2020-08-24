package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Span

// ------------------------------------------------------ dsl

fun HtmlElements.pfButton(vararg modifiers: String, content: Button.() -> Unit = {}): Button =
    register(Button(modifiers.toList()), content)

fun HtmlElements.pfButton(vararg modifiers: Modifier, content: Button.() -> Unit = {}): Button =
    register(Button(modifiers.map { it.value }), content)

fun HtmlElements.pfLinkButton(vararg modifiers: String, content: LinkButton.() -> Unit = {}): LinkButton =
    register(LinkButton(modifiers.toList()), content)

fun HtmlElements.pfLinkButton(vararg modifiers: Modifier, content: LinkButton.() -> Unit = {}): LinkButton =
    register(LinkButton(modifiers.map { it.value }), content)

fun Button.pfIcon(position: Position, iconClass: String, content: Icon.() -> Unit = {}): Span =
    span(buildString { append("button".component("icon")).append(" ").append(position.modifier.value) }) {
        pfIcon(iconClass, content)
    }

// ------------------------------------------------------ tag

class Button internal constructor(modifiers: List<String>) :
    dev.fritz2.dom.html.Button(baseClass = buildString {
        append("button".component())
        if (modifiers.isNotEmpty()) {
            modifiers.joinTo(this, " ", " ")
        }
    }) {
    init {
        domNode.componentType(ComponentType.Button)
    }
}

class LinkButton internal constructor(modifiers: List<String>) :
    dev.fritz2.dom.html.A(baseClass = buildString {
        append("button".component())
        if (modifiers.isNotEmpty()) {
            modifiers.joinTo(this, " ", " ")
        }
    }) {
    init {
        domNode.componentType(ComponentType.Button)
    }
}
