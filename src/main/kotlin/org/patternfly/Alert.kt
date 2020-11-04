package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.Ul
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.removeFromParent
import kotlinx.browser.window
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

public fun HtmlElements.pfAlertGroup(
    toast: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: AlertGroup.() -> Unit = {}
): AlertGroup = register(AlertGroup(toast, id = id, baseClass = baseClass), content)

public fun HtmlElements.pfAlert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Alert.() -> Unit = {}
): Alert = register(Alert(severity, text, closable, inline, id = id, baseClass = baseClass), content)

public fun AlertGroup.pfAlert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Alert.() -> Unit = {}
): Li = register(li("alert-group".component("item")) {
    pfAlert(severity, text, closable, inline, id = id, baseClass = baseClass) {
        content(this)
    }
}, {})

public fun Alert.pfAlertDescription(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(div(id = id, baseClass = classes("alert".component("description"), baseClass)) {
        content()
    }, {})

public fun Alert.pfAlertActionGroup(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(div(id = id, baseClass = classes("alert".component("action-group"), baseClass)) {
        content()
    }, {})

// ------------------------------------------------------ tag

public class AlertGroup internal constructor(toast: Boolean, id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLUListElement>,
    Ul(id = id, baseClass = classes {
        +ComponentType.AlertGroup
        +("toast".modifier() `when` toast)
        +baseClass
    }) {

    private val timeoutHandles: MutableMap<String, Int> = mutableMapOf()

    init {
        markAs(ComponentType.AlertGroup)
        if (toast) {
            MainScope().launch {
                Notification.store.latest.collect {
                    val alertId = Id.unique("alert")
                    val element = pfAlert(it.severity, it.text, true).domNode
                    element.id = alertId
                    domNode.prepend(element)
                    element.onmouseover = { stopTimeout(alertId) }
                    element.onmouseout = { startTimeout(alertId, element) }
                    startTimeout(alertId, element)
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

@OptIn(ExperimentalCoroutinesApi::class)
public class Alert internal constructor(
    private val severity: Severity,
    private val text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    id: String?,
    baseClass: String?
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
    +ComponentType.Alert
    +severity.modifier
    +("inline".modifier() `when` inline)
    +baseClass
}) {

    private var closeButton: Button? = null

    public val closes: Listener<MouseEvent, HTMLButtonElement> by lazy {
        if (closeButton != null) {
            Listener(callbackFlow {
                val listener: (Event) -> Unit = {
                    offer(it.unsafeCast<MouseEvent>())
                }
                this@Alert.closeButton?.domNode?.addEventListener(Events.click.name, listener)
                awaitClose { this@Alert.closeButton?.domNode?.removeEventListener(Events.click.name, listener) }
            })
        } else {
            Listener(emptyFlow())
        }
    }

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
                this@Alert.closeButton = pfButton("plain".modifier()) {
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
