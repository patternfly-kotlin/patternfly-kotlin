package org.patternfly

import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement
import kotlin.browser.window

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
): Li = register(li(baseClass = "alert-group".component("item")) {
    pfAlert(severity, text, closable, inline) {
        content(this)
    }
}, {})

// ------------------------------------------------------ tag

class AlertGroup internal constructor(toast: Boolean) :
    PatternFlyTag<HTMLUListElement>(ComponentType.AlertGroup, "ul", "alert-group".component()) {

    private val timeoutHandles: MutableMap<String, Int> = mutableMapOf()

    init {
        if (toast) {
            domNode.classList.add("toast".modifier())
            MainScope().launch {
                Notification.store.latest.collect {
                    val id = Id.unique("alert")
                    val element = pfAlert(it.severity, it.text, true).domNode
                    element.id = id
                    domNode.prepend(element)
                    element.onmouseover = { stopTimeout(id, element) }
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

    private fun stopTimeout(id: String, element: HTMLElement) {
        timeoutHandles[id]?.let { window.clearTimeout(it) }
    }
}

class Alert internal constructor(
    private val severity: Severity,
    private val text: String,
    private val closable: Boolean = false,
    private val inline: Boolean = false
) : PatternFlyTag<HTMLDivElement>(ComponentType.Alert, "div", "alert".component()) {

    init {
        domNode.classList.add(severity.modifier)
        if (inline) {
            domNode.classList.add("inline".modifier())
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
                pfPlainButton(iconClass = "times".fas()) {
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
