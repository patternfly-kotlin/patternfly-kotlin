package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.EventContext
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Events.click
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import org.patternfly.ButtonVariant.plain
import org.patternfly.dom.removeFromParent
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ factory

/**
 * Creates an [StaticAlertGroup] component. This alert group is used to stack and position inline [Alert]s.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.alertGroup
 */
public fun RenderContext.alertGroup(
    baseClass: String? = null,
    id: String? = null,
    context: StaticAlertGroup.() -> Unit
) {
    StaticAlertGroup().apply(context).render(this, baseClass, id)
}

/**
 * Creates an [Alert] component.
 *
 * @param severity the severity level
 * @param title the title of the alert
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.alert
 */
public fun RenderContext.alert(
    severity: Severity = Severity.INFO,
    title: String = "",
    baseClass: String? = null,
    id: String? = null,
    context: Alert.() -> Unit = {}
) {
    Alert(severity, title).apply(context).render(this, baseClass, id)
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
                    set(Scopes.ALERT_GROUP, true)
                }
            ) {
                markAs(ComponentType.AlertGroup)
                renderAlerts(this)
            }
        }
    }

    internal abstract fun renderAlerts(context: Tag<HTMLElement>)
}

/**
 * PatternFly [alert group](https://www.patternfly.org/v4/components/alert-group/design-guidelines) component.
 *
 * This alert group is used to stack and position inline [Alert]s.
 *
 * @sample org.patternfly.sample.AlertSample.alertGroup
 */
public open class StaticAlertGroup : BaseAlertGroup(false) {

    private val alerts: MutableList<StaticAlertBuilder> = mutableListOf()

    public fun alert(
        severity: Severity = Severity.INFO,
        title: String = "",
        baseClass: String? = null,
        id: String? = null,
        context: Alert.() -> Unit = {}
    ) {
        alerts.add(StaticAlertBuilder(severity, title, baseClass, id, context))
    }

    override fun renderAlerts(context: Tag<HTMLElement>) {
        with(context) {
            alerts.forEach { alertBuilder ->
                li(baseClass = "alert-group".component("item")) {
                    alert(
                        severity = alertBuilder.severity,
                        title = alertBuilder.title,
                        baseClass = alertBuilder.baseClass,
                        id = alertBuilder.id
                    ) {
                        alertBuilder.context(this)
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
    val context: Alert.() -> Unit
)

/**
 * PatternFly [alert](https://www.patternfly.org/v4/components/alert/design-guidelines) component.
 *
 * Alerts are used to notify the user about a change in status or other event.
 *
 * @sample org.patternfly.sample.AlertSample.alert
 */
public open class Alert(private var severity: Severity, title: String) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    private lateinit var root: Tag<HTMLElement>
    private var inline: Boolean = false
    private var content: (RenderContext.() -> Unit)? = null
    private val actions: MutableList<AlertAction> = mutableListOf()
    private val ariaLabels: Pair<String, String> = when (severity) {
        Severity.DEFAULT -> "Default alert" to "Close default alert"
        Severity.INFO -> "Info alert" to "Close info alert"
        Severity.SUCCESS -> "Success alert" to "Close success alert"
        Severity.WARNING -> "Warning alert" to "Close warning alert"
        Severity.DANGER -> "Error alert" to "Close error alert"
    }
    private var closable: Boolean = false
    private val closeStore: RootStore<MouseEvent> = storeOf(MouseEvent(""))
    private val closeHandler: (Event) -> Unit = ::removeFromParent
    public val closes: Flow<MouseEvent> = closeStore.data.drop(1)

    init {
        this.title(title)
    }

    public fun closable(closable: Boolean) {
        this.closable = closable
    }

    public fun severity(severity: Severity) {
        this.severity = severity
    }

    public fun inline(inline: Boolean) {
        this.inline = inline
    }

    public fun content(content: RenderContext.() -> Unit) {
        this.content = content
    }

    /**
     * Adds an actions to this [Alert].
     *
     * @param title the title of the action
     * @param events a lambda expression for setting up the events of the action
     *
     * @sample org.patternfly.sample.AlertSample.actions
     */
    public fun action(title: String, events: EventContext<HTMLElement>.() -> Unit) {
        actions.add(AlertAction({ +title }, events))
    }

    /**
     * Adds an actions to this [Alert].
     *
     * @param context a lambda expression for setting up the action
     * @param events a lambda expression for setting up the events of the action
     *
     * @sample org.patternfly.sample.AlertSample.actions
     */
    public fun action(context: Button.() -> Unit, events: EventContext<HTMLElement>.() -> Unit) {
        actions.add(AlertAction(context, events))
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
                applyElement(this)
                applyEvents(this)

                div(baseClass = "alert".component("icon")) {
                    icon(severity.iconClass)
                }
                h4(baseClass = "alert".component("title")) {
                    span(baseClass = "pf-screen-reader") {
                        +ariaLabels.first
                    }
                    this@Alert.applyTitle(this)
                }
                if (closable) {
                    div(baseClass = "alert".component("action")) {
                        pushButton(plain) {
                            icon("times".fas())
                            aria["label"] = this@Alert.ariaLabels.second
                            domNode.addEventListener(click.name, this@Alert.closeHandler)
                            clicks.map { it } handledBy this@Alert.closeStore.update
                        }
                    }
                }
                content?.let { content ->
                    div(baseClass = "alert".component("description")) {
                        content(this)
                    }
                }
                if (actions.isNotEmpty()) {
                    div(baseClass = "alert".component("action-group")) {
                        actions.forEach { alertAction ->
                            pushButton(ButtonVariant.inline, ButtonVariant.link) {
                                alertAction.context(this)
                                events {
                                    alertAction.events(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun removeFromParent(event: Event) {
        (event.target as Element).removeEventListener(click.name, closeHandler)
        if (root.scope.contains(Scopes.ALERT_GROUP)) {
            root.domNode.parentElement.removeFromParent()
        } else {
            root.domNode.removeFromParent()
        }
    }
}

internal class AlertAction(
    val context: Button.() -> Unit,
    val events: (EventContext<HTMLElement>.() -> Unit)
)
