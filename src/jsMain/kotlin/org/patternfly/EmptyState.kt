package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfEmptyState(
    iconClass: String,
    title: String,
    size: Size? = null,
    classes: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = register(EmptyState(iconClass, title, size, classes, content), {})

fun HtmlElements.pfEmptyState(
    iconClass: String,
    title: String,
    size: Size? = null,
    modifier: Modifier,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = register(EmptyState(iconClass, title, size, modifier.value, content), {})

fun EmptyState.pfEmptyStateContent(
    classes: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyStateContent = register(EmptyStateContent(classes), content)

fun EmptyState.pfEmptyStateContent(
    modifier: Modifier,
    content: EmptyStateContent.() -> Unit = {}
): EmptyStateContent = register(EmptyStateContent(modifier.value), content)

fun EmptyStateContent.pfEmptyStateBody(
    classes: String? = null,
    content: EmptyStateBody.() -> Unit = {}
): EmptyStateBody = register(EmptyStateBody(classes), content)

fun EmptyStateContent.pfEmptyStateBody(
    modifier: Modifier,
    content: EmptyStateBody.() -> Unit = {}
): EmptyStateBody = register(EmptyStateBody(modifier.value), content)

fun EmptyStateContent.pfEmptyStateSecondary(classes: String? = null, content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = classes("empty-state".component("secondary"), classes)), content)

fun EmptyStateContent.pfEmptyStateSecondary(modifier: Modifier, content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = classes("empty-state".component("secondary"), modifier.value)), content)

// ------------------------------------------------------ tag

class EmptyState(
    iconClass: String,
    title: String,
    size: Size?,
    classes: String?,
    content: EmptyStateContent.() -> Unit
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +ComponentType.EmptyState
    +size?.modifier
    +classes
}) {

    init {
        markAs(ComponentType.EmptyState)
        pfEmptyStateContent {
            pfIcon(iconClass).apply {
                domNode.classList.add("empty-state".component("icon"))
            }
            pfTitle(size = Size.LG) {
                +title
            }
            content(this)
        }
    }
}

class EmptyStateBody(classes: String?) :
    Div(baseClass = classes("empty-state".component("body"), classes))

class EmptyStateContent(classes: String?) :
    Div(baseClass = classes("empty-state".component("content"), classes))
