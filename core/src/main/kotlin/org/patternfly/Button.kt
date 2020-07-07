package org.patternfly

import dev.fritz2.dom.WithText
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLButtonElement

// ------------------------------------------------------ dsl

@OptIn(ExperimentalStdlibApi::class)
fun HtmlElements.pfButton(
    style: Style,
    text: String? = null,
    iconClass: String? = null,
    iconRight: Boolean = false,
    blockLevel: Boolean = false,
    content: Button.() -> Unit = {}
): Button = register(Button(buildSet {
    add(style.modifier)
    if (blockLevel) add("block".modifier())
}, text, iconClass, iconRight), content)

fun HtmlElements.pfControlButton(
    text: String? = null,
    iconClass: String? = null,
    iconRight: Boolean = false,
    content: Button.() -> Unit = {}
): Button = register(Button(setOf("control".modifier()), text, iconClass, iconRight), content)

@OptIn(ExperimentalStdlibApi::class)
fun HtmlElements.pfLinkButton(
    text: String? = null,
    iconClass: String? = null,
    iconRight: Boolean = false,
    inline: Boolean = false,
    content: Button.() -> Unit = {}
): Button = register(Button(buildSet {
    add("link".modifier())
    if (inline) add("inline".modifier())
}, text, iconClass, iconRight), content)

fun HtmlElements.pfPlainButton(
    text: String? = null,
    iconClass: String? = null,
    iconRight: Boolean = false,
    content: Button.() -> Unit = {}
): Button = register(Button(setOf("plain".modifier()), text, iconClass, iconRight), content)

// ------------------------------------------------------ tag

class Button(
    modifier: Set<String>,
    private val text: String? = null,
    private val iconClass: String? = null,
    iconRight: Boolean = false
) : PatternFlyTag<HTMLButtonElement>(ComponentType.Button, "button", "button".component()),
    WithText<HTMLButtonElement> {

    init {
        if (modifier.isNotEmpty()) {
            domNode.classList.add(*modifier.toTypedArray())
        }
        when {
            text != null && iconClass != null -> if (iconRight) {
                span("button".component("text")) {
                    +this@Button.text
                }
                span("button".component("icon")) {
                    pfIcon(this@Button.iconClass)
                }
            } else {
                span("button".component("icon")) {
                    pfIcon(this@Button.iconClass)
                }
                span("button".component("text")) {
                    +this@Button.text
                }
            }
            text != null -> +text
            iconClass != null -> pfIcon(iconClass)
        }
    }
}
