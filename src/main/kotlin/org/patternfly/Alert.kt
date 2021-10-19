package org.patternfly

import dev.fritz2.dom.EventContext
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.RenderContext
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.By
import org.patternfly.dom.matches
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
 * @sample org.patternfly.sample.AlertSample.staticAlertGroup
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
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param build a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AlertSample.standaloneAlert
 */
public fun RenderContext.alert(
    baseClass: String? = null,
    id: String? = null,
    build: Alert.() -> Unit
) {
    Alert().apply(build).render(this, baseClass, id)
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
                id = id
            ) {
                markAs(ComponentType.AlertGroup)
                renderAlerts(this)
            }
        }
    }

    internal abstract fun renderAlerts(context: RenderContext)
}

/**
 * PatternFly [alert group](https://www.patternfly.org/v4/components/alert-group/design-guidelines) component.
 *
 * This alert group is used to stack and position inline [Alert]s.
 *
 * @sample org.patternfly.sample.AlertSample.staticAlertGroup
 */
public class StaticAlertGroup : BaseAlertGroup(false) {

    private val alerts: MutableList<StaticAlertContext> = mutableListOf()

    public fun alert(
        baseClass: String? = null,
        id: String? = null,
        build: Alert.() -> Unit
    ) {
        alerts.add(StaticAlertContext(baseClass, id, build))
    }

    override fun renderAlerts(context: RenderContext) {
        with(context) {
            alerts.forEach { sac ->
                li(baseClass = "alert-group".component("item")) {
                    alert(sac.baseClass, sac.id) {
                        sac.build(this)
                        inline(true) // force alerts to be inline
                    }
                }
            }
        }
    }
}

internal class StaticAlertContext(
    val baseClass: String? = null,
    val id: String? = null,
    val build: Alert.() -> Unit
)

/**
 * PatternFly [alert](https://www.patternfly.org/v4/components/alert/design-guidelines) component.
 *
 * Alerts are used to notify the user about a change in status or other event.
 *
 * @sample org.patternfly.sample.AlertSample.standaloneAlert
 */
public class Alert :
    PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement<Div, HTMLDivElement> by ElementMixin(),
    WithEvents<HTMLDivElement> by EventMixin(),
    WithTitle<H, HTMLHeadingElement> by TitleMixin(),
    WithContent<Div, HTMLDivElement> by ContentMixin(),
    WithClosable<HTMLButtonElement> by ClosableMixin() {

    private lateinit var root: Tag<HTMLElement>
    private var severity: Severity = Severity.INFO
    private var inline: Boolean = false
    private val actions: MutableMap<String, EventContext<HTMLButtonElement>.() -> Unit> = mutableMapOf()
    private val ariaLabels: Pair<String, String> = when (severity) {
        Severity.DEFAULT -> "Default alert" to "Close default alert"
        Severity.INFO -> "Info alert" to "Close info alert"
        Severity.SUCCESS -> "Success alert" to "Close success alert"
        Severity.WARNING -> "Warning alert" to "Close warning alert"
        Severity.DANGER -> "Error alert" to "Close error alert"
    }

    public fun severity(severity: Severity) {
        this.severity = severity
    }

    public fun inline(inline: Boolean) {
        this.inline = inline
    }

    /**
     * Adds an actions to this [Alert].
     *
     * @param title the title of the action
     * @param action a lambda which provides event listeners of the action
     *
     * @sample org.patternfly.sample.AlertSample.actions
     */
    public fun action(title: String, action: EventContext<HTMLButtonElement>.() -> Unit = {}) {
        actions[title] = action
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
                ariaContext.applyTo(this)
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
                            domNode.addEventListener(Events.click.name, this@Alert::close)
                            closeAction?.invoke(this)
                        }
                    }
                }
                content?.let { cnt ->
                    div(baseClass = "alert".component("description")) {
                        cnt(this)
                    }
                }
                if (actions.isNotEmpty()) {
                    div(baseClass = "alert".component("action-group")) {
                        actions.forEach { (action, events) ->
                            pushButton(ButtonVariation.inline, ButtonVariation.link) {
                                +action
                                events(this)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun close(event: Event) {
        (event.target as Element).removeEventListener(Events.click.name, ::close)
        if (root.domNode.parentElement?.matches(By.classname("alert-group".component("item"))) == true) {
            root.domNode.parentElement.removeFromParent()
        } else {
            root.domNode.removeFromParent()
        }
    }
}
