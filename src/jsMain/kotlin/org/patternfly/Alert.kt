package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.Ul
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.patternfly.Modifier.plain
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfAlertGroup(toast: Boolean = false, content: AlertGroup.() -> Unit = {}): AlertGroup =
    register(AlertGroup(toast), content)

fun HtmlElements.pfAlert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    content: Alert.() -> Unit = {}
): Alert = register(Alert(severity, text, closable, inline), content)

fun AlertGroup.pfAlert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    content: Alert.() -> Unit = {}
): Li = register(li("alert-group".component("item")) {
    pfAlert(severity, text, closable, inline) {
        content(this)
    }
}, {})

fun Alert.pfAlertDescription(content: Div.() -> Unit = {}): Div =
    register(div("alert".component("description")) {
        content()
    }, {})

fun Alert.pfAlertActionGroup(content: Div.() -> Unit = {}): Div =
    register(div("alert".component("action-group")) {
        content()
    }, {})

// ------------------------------------------------------ tag

class AlertGroup internal constructor(toast: Boolean) : Ul(baseClass = "alert-group".component()) {

    private val timeoutHandles: MutableMap<String, Int> = mutableMapOf()

    init {
        domNode.componentType(ComponentType.AlertGroup)
        if (toast) {
            domNode.classList += Modifier.toast
            MainScope().launch {
                Notification.store.latest.collect {
                    val id = Id.unique("alert")
                    val element = pfAlert(it.severity, it.text, true).domNode
                    element.id = id
                    domNode.prepend(element)
                    element.onmouseover = { stopTimeout(id) }
                    element.onmouseout = { startTimeout(id, element) }
                    startTimeout(id, element)
                }
            }
        }
    }

    private fun startTimeout(id: String, element: HTMLElement) {
        val handle = window.setTimeout({ element.removeFromParent() }, Settings.notificationTimeout)
        timeoutHandles[id] = handle
    }

    private fun stopTimeout(id: String) {
        timeoutHandles[id]?.let { window.clearTimeout(it) }
    }
}

class Alert internal constructor(
    private val severity: Severity,
    private val text: String,
    closable: Boolean = false,
    inline: Boolean = false
) : Div(baseClass = "alert".component()) {

    init {
        domNode.componentType(ComponentType.Alert)
        severity.modifier?.let {
            domNode.classList += it
        }
        if (inline) {
            domNode.classList += Modifier.inline
        }
        attr("aria-label", severity.aria)
        div(baseClass = "alert".component("icon")) {
            pfIcon(this@Alert.severity.iconClass)
        }
        h4(baseClass = "alert".component("title")) {
            span(baseClass = "pf-screen-reader") {
                +this@Alert.severity.aria
            }
            +this@Alert.text
        }
        if (closable) {
            div(baseClass = "alert".component("action")) {
                pfButton(plain) {
                    pfIcon("times".fas())
                    attr("aria-label", "Close ${this@Alert.severity.aria.toLowerCase()}: ${this@Alert.text}")
                    domNode.addEventListener(Events.click.name, { this@Alert.close() })
                }
            }
        }
    }

    private fun close() {
        if (domNode.parentElement?.matches(".${"alert-group".component("item")}") == true) {
            domNode.parentElement.removeFromParent()
        } else {
            domNode.removeFromParent()
        }
    }
}
