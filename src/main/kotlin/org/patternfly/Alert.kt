package org.patternfly

import dev.fritz2.dom.EventContext
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.keyOf
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.removeFromParent
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadingElement
import org.w3c.dom.events.Event

// ------------------------------------------------------ factory

/**
 * Creates an [StaticAlertGroup] component. This alert group is used to stack and position inline [Alert]s.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param build a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.alertGroup
 */
public fun RenderContext.alertGroup(
    baseClass: String? = null,
    id: String? = null,
    build: StaticAlertGroup.() -> Unit
) {
    StaticAlertGroup().apply(build).render(this, baseClass, id)
}

/**
 * Creates an [Alert] component.
 *
 * @param severity the severity level
 * @param title the title of the alert
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param build a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.alert
 */
public fun RenderContext.alert(
    severity: Severity = Severity.INFO,
    title: String = "",
    baseClass: String? = null,
    id: String? = null,
    build: Alert.() -> Unit = {}
) {
    Alert(severity, title).apply(build).render(this, baseClass, id)
}

// ------------------------------------------------------ component

public abstract class BaseAlertGroup(private val toast: Boolean) : PatternFlyComponent<Unit> {

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            ul(
                baseClass = classes {
                    +ComponentType.AlertGroup
                    +("toast".modifier() `when` toast)
                    +baseClass
                },
                id = id,
                scope = {
                    set(ALERT_GROUP_KEY, true)
                }
            ) {
                markAs(ComponentType.AlertGroup)
                renderAlerts(this)
            }
        }
    }

    internal abstract fun renderAlerts(context: RenderContext)

    internal companion object {
        val ALERT_GROUP_KEY: Scope.Key<Boolean> = keyOf(ComponentType.AlertGroup.id)
    }
}

/**
 * PatternFly [alert group](https://www.patternfly.org/v4/components/alert-group/design-guidelines) component.
 *
 * This alert group is used to stack and position inline [Alert]s.
 *
 * @sample org.patternfly.sample.AlertSample.alertGroup
 */
public class StaticAlertGroup : BaseAlertGroup(false) {

    private val alerts: MutableList<StaticAlertBuilder> = mutableListOf()

    public fun alert(
        severity: Severity = Severity.INFO,
        title: String = "",
        baseClass: String? = null,
        id: String? = null,
        build: Alert.() -> Unit = {}
    ) {
        alerts.add(StaticAlertBuilder(severity, title, baseClass, id, build))
    }

    override fun renderAlerts(context: RenderContext) {
        with(context) {
            alerts.forEach { alertBuilder ->
                li(baseClass = "alert-group".component("item")) {
                    alert(
                        severity = alertBuilder.severity,
                        title = alertBuilder.title,
                        baseClass = alertBuilder.baseClass,
                        id = alertBuilder.id
                    ) {
                        alertBuilder.build(this)
                        inline(true) // force alerts to be inline
                    }
                }
            }
        }
    }
}

internal class StaticAlertBuilder(
    val severity: Severity,
    val title: String,
    val baseClass: String?,
    val id: String?,
    val build: Alert.() -> Unit
)

/**
 * PatternFly [alert](https://www.patternfly.org/v4/components/alert/design-guidelines) component.
 *
 * Alerts are used to notify the user about a change in status or other event.
 *
 * @sample org.patternfly.sample.AlertSample.alert
 */
public class Alert internal constructor(private var severity: Severity, title: String) :
    PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement<Div, HTMLDivElement> by ElementMixin(),
    WithEvents<HTMLDivElement> by EventMixin(),
    WithTitle<H, HTMLHeadingElement> by TitleMixin(),
    WithClosable<HTMLButtonElement> by ClosableMixin() {

    private lateinit var root: Tag<HTMLElement>
    private var inline: Boolean = false
    private var content: SubComponent<RenderContext>? = null
    private val actions: MutableList<AlertAction> = mutableListOf()
    private val ariaLabels: Pair<String, String> = when (severity) {
        Severity.DEFAULT -> "Default alert" to "Close default alert"
        Severity.INFO -> "Info alert" to "Close info alert"
        Severity.SUCCESS -> "Success alert" to "Close success alert"
        Severity.WARNING -> "Warning alert" to "Close warning alert"
        Severity.DANGER -> "Error alert" to "Close error alert"
    }

    init {
        this.title(title)
    }

    public fun severity(severity: Severity) {
        this.severity = severity
    }

    public fun inline(inline: Boolean) {
        this.inline = inline
    }

    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit = {}
    ) {
        this.content = SubComponent(baseClass, id, context)
    }

    /**
     * Adds an actions to this [Alert].
     *
     * @param title the title of the action
     * @param events a lambda expression for setting up the events of the action
     *
     * @sample org.patternfly.sample.AlertSample.actions
     */
    public fun action(title: String, events: EventContext<HTMLButtonElement>.() -> Unit) {
        actions.add(AlertAction({ +title }, events))
    }

    /**
     * Adds an actions to this [Alert].
     *
     * @param build a lambda expression for setting up the action
     * @param events a lambda expression for setting up the events of the action
     *
     * @sample org.patternfly.sample.AlertSample.actions
     */
    public fun action(build: PushButton.() -> Unit, events: EventContext<HTMLButtonElement>.() -> Unit) {
        actions.add(AlertAction(build, events))
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            root = div(
                baseClass = classes {
                    +ComponentType.Alert
                    +severity.modifier
                    +("inline".modifier() `when` inline)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Alert)
                aria["label"] = ariaLabels.first
                aria(this)
                element(this)
                events(this)

                div(baseClass = "alert".component("icon")) {
                    icon(severity.iconClass)
                }
                h4(baseClass = "alert".component("title")) {
                    span(baseClass = "pf-screen-reader") {
                        +ariaLabels.first
                    }
                    title.asText()
                }
                if (closable) {
                    div(baseClass = "alert".component("action")) {
                        pushButton(plain) {
                            icon("times".fas())
                            aria["label"] = ariaLabels.second
                            domNode.addEventListener(Events.click.name, this@Alert::removeFromParent)
                            closeEvents?.invoke(this)
                        }
                    }
                }
                content?.let { cnt ->
                    div(
                        baseClass = classes("alert".component("description"), cnt.baseClass),
                        id = cnt.id
                    ) {
                        cnt.context(this)
                    }
                }
                if (actions.isNotEmpty()) {
                    div(baseClass = "alert".component("action-group")) {
                        actions.forEach { alertAction ->
                            pushButton(ButtonVariation.inline, ButtonVariation.link) {
                                alertAction.build(this)
                                alertAction.events(this)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun removeFromParent(event: Event) {
        (event.target as Element).removeEventListener(Events.click.name, ::removeFromParent)
        if (root.scope.contains(BaseAlertGroup.ALERT_GROUP_KEY)) {
            root.domNode.parentElement.removeFromParent()
        } else {
            root.domNode.removeFromParent()
        }
    }
}

internal class AlertAction(
    val build: PushButton.() -> Unit,
    val events: (EventContext<HTMLButtonElement>.() -> Unit)
)
