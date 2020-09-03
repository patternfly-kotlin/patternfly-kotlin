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
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfAlertGroup(
    toast: Boolean = false,
    classes: String? = null,
    content: AlertGroup.() -> Unit = {}
): AlertGroup = register(AlertGroup(toast, classes), content)

fun HtmlElements.pfAlertGroup(
    toast: Boolean = false,
    modifier: Modifier,
    content: AlertGroup.() -> Unit = {}
): AlertGroup = register(AlertGroup(toast, modifier.value), content)

fun HtmlElements.pfAlert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    classes: String? = null,
    content: Alert.() -> Unit = {}
): Alert = register(Alert(severity, text, closable, inline, classes), content)

fun HtmlElements.pfAlert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    modifier: Modifier,
    content: Alert.() -> Unit = {}
): Alert = register(Alert(severity, text, closable, inline, modifier.value), content)

fun AlertGroup.pfAlert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    classes: String? = null,
    content: Alert.() -> Unit = {}
): Li = register(li("alert-group".component("item")) {
    pfAlert(severity, text, closable, inline, classes) {
        content(this)
    }
}, {})

fun AlertGroup.pfAlert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    modifier: Modifier,
    content: Alert.() -> Unit = {}
): Li = register(li("alert-group".component("item")) {
    pfAlert(severity, text, closable, inline, modifier) {
        content(this)
    }
}, {})

fun Alert.pfAlertDescription(classes: String? = null, content: Div.() -> Unit = {}): Div =
    register(div(baseClass = classes("alert".component("description"), classes)) {
        content()
    }, {})

fun Alert.pfAlertDescription(modifier: Modifier, content: Div.() -> Unit = {}): Div =
    register(div(baseClass = classes("alert".component("description"), modifier.value)) {
        content()
    }, {})

fun Alert.pfAlertActionGroup(classes: String? = null, content: Div.() -> Unit = {}): Div =
    register(div(baseClass = classes("alert".component("action-group"), classes)) {
        content()
    }, {})

fun Alert.pfAlertActionGroup(modifier: Modifier, content: Div.() -> Unit = {}): Div =
    register(div(baseClass = classes("alert".component("action-group"), modifier.value)) {
        content()
    }, {})

// ------------------------------------------------------ tag

class AlertGroup internal constructor(toast: Boolean, classes: String?) :
    PatternFlyComponent<HTMLUListElement>,
    Ul(baseClass = classes {
        +ComponentType.AlertGroup
        +(Modifier.toast `when` toast)
        +classes
    }) {

    private val timeoutHandles: MutableMap<String, Int> = mutableMapOf()

    init {
        markAs(ComponentType.AlertGroup)
        if (toast) {
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
    inline: Boolean = false,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +ComponentType.Alert
    +severity.modifier
    +(Modifier.inline `when` inline)
    +classes
}) {

    init {
        markAs(ComponentType.Alert)
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
                    aria["label"] = "Close ${this@Alert.severity.aria.toLowerCase()}: ${this@Alert.text}"
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
