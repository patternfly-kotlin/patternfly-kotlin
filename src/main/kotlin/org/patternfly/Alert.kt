package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.html.renderElement
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.matches
import org.patternfly.dom.querySelector
import org.patternfly.dom.removeFromParent
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.ButtonVariation.plain
import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

internal val TOAST_ALERT_GROUP = Id.unique("toast", ComponentType.AlertGroup.id)

/**
 * Creates the singleton toast [AlertGroup] component and [prepends][org.w3c.dom.ParentNode.prepend] it to the body of this document. If the toast alert group is already present in the DOM, this function does nothing.
 *
 * @receiver the document
 *
 * @param baseClass optional CSS class that should be applied to the element
 *
 * @sample org.patternfly.sample.AlertSample.toastAlertGroup
 */
public fun Document.addToastAlertGroup(baseClass: String? = null) {
    if (querySelector(By.id(TOAST_ALERT_GROUP)) == null) {
        body?.prepend(renderElement { AlertGroup(true, TOAST_ALERT_GROUP, baseClass, job) }.domNode)
    }
}

/**
 * Creates an [AlertGroup] component. Alert groups are used to stack and position [Alert]s. Besides the singleton toast alert group, alert groups are most often used to group inline alerts.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.inlineAlertGroup
 */
public fun RenderContext.alertGroup(
    id: String? = null,
    baseClass: String? = null,
    content: AlertGroup.() -> Unit = {}
): AlertGroup = register(AlertGroup(false, id = id, baseClass = baseClass, job), content)

/**
 * Creates a standalone [Alert] component.
 *
 * @param severity the severity level
 * @param text the text of the alert
 * @param closable whether the alert can be closed
 * @param inline whether the alert is rendered as an inline alert
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.standaloneAlert
 */
public fun RenderContext.alert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Alert.() -> Unit = {}
): Alert = register(Alert(severity, text, closable, inline, id = id, baseClass = baseClass, job), content)

/**
 * Creates an [Alert] component nested inside an [AlertGroup] component.
 *
 * @param severity the severity level
 * @param text the text of the alert
 * @param closable whether the alert can be closed
 * @param inline whether the alert is rendered as an inline alert
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.inlineAlertGroup
 */
public fun AlertGroup.alert(
    severity: Severity,
    text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Alert.() -> Unit = {}
): Li = register(li("alert-group".component("item")) {
    alert(severity, text, closable, inline, id = id, baseClass = baseClass) {
        content(this)
    }
}, {})

/**
 * Adds a description to an [Alert] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.description
 */
public fun Alert.alertDescription(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(div(id = id, baseClass = classes("alert".component("description"), baseClass)) {
    content()
}, {})

/**
 * Adds a container for actions to an [Alert] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.actions
 */
public fun Alert.alertActions(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(div(id = id, baseClass = classes("alert".component("action-group"), baseClass)) {
    content()
}, {})

// ------------------------------------------------------ tag

/**
 * PatternFly [alert group](https://www.patternfly.org/v4/components/alert-group/design-guidelines) component.
 *
 * An alert group is used to stack and position [Alert]s. Besides the singleton toast alert group,
 * alert groups are most often used to group inline alerts.
 *
 * @sample org.patternfly.sample.AlertSample.inlineAlertGroup
 */
public class AlertGroup internal constructor(toast: Boolean, id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLUListElement>,
    Ul(id = id, baseClass = classes {
        +ComponentType.AlertGroup
        +("toast".modifier() `when` toast)
        +baseClass
    }, job) {

    private val timeoutHandles: MutableMap<String, Int> = mutableMapOf()

    init {
        markAs(ComponentType.AlertGroup)
        if (toast) {
            (MainScope() + job).launch {
                NotificationStore.latest.collect {
                    val alertId = Id.unique("alert")
                    domNode.prepend(renderElement {
                        alert(it.severity, it.text, true, id = alertId) {
                            with(domNode) {
                                onmouseover = { this@AlertGroup.stopTimeout(alertId) }
                                onmouseout = { this@AlertGroup.startTimeout(alertId, this) }
                                this@AlertGroup.startTimeout(alertId, this)
                            }
                        }
                    }.domNode)
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

/**
 * PatternFly [alert](https://www.patternfly.org/v4/components/alert/design-guidelines) component.
 *
 * Alerts are used to notify the user about a change in status or other event.
 *
 * @sample org.patternfly.sample.AlertSample.standaloneAlert
 */
public class Alert internal constructor(
    private val severity: Severity,
    private val text: String,
    closable: Boolean = false,
    inline: Boolean = false,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
    +ComponentType.Alert
    +severity.modifier
    +("inline".modifier() `when` inline)
    +baseClass
}, job) {

    private var closeButton: PushButton? = null
    private val ariaLabels: Pair<String, String> = when (severity) {
        Severity.DEFAULT -> "Default alert" to "Close default alert"
        Severity.INFO -> "Info alert" to "Close info alert"
        Severity.SUCCESS -> "Success alert" to "Close success alert"
        Severity.WARNING -> "Warning alert" to "Close warning alert"
        Severity.DANGER -> "Error alert" to "Close error alert"
    }

    /**
     * Listener for the close button (if any).
     *
     * @sample org.patternfly.sample.AlertSample.closes
     */
    public val closes: Listener<MouseEvent, HTMLButtonElement> by lazy { subscribe(closeButton, Events.click) }

    init {
        markAs(ComponentType.Alert)
        aria["label"] = ariaLabels.first
        div(baseClass = "alert".component("icon")) {
            icon(this@Alert.severity.iconClass)
        }
        h4(baseClass = "alert".component("title")) {
            span(baseClass = "pf-screen-reader") {
                +this@Alert.ariaLabels.first
            }
            +this@Alert.text
        }
        if (closable) {
            div(baseClass = "alert".component("action")) {
                this@Alert.closeButton = pushButton(plain) {
                    icon("times".fas())
                    aria["label"] = this@Alert.ariaLabels.second
                    domNode.addEventListener(Events.click.name, this@Alert::close)
                }
            }
        }
    }

    private fun close(@Suppress("UNUSED_PARAMETER") ignore: Event) {
        closeButton?.domNode?.removeEventListener(Events.click.name, ::close)
        if (domNode.parentElement?.matches(By.classname("alert-group".component("item"))) == true) {
            domNode.parentElement.removeFromParent()
        } else {
            domNode.removeFromParent()
        }
    }
}
