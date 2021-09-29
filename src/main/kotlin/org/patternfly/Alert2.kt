package org.patternfly

import dev.fritz2.dom.EventContext
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import org.patternfly.dom.By
import org.patternfly.dom.matches
import org.patternfly.dom.removeFromParent
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

// ------------------------------------------------------ factory

/**
 * Creates a standalone [Alert2] component.
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
    build: Alert2.() -> Unit
) {
    Alert2().apply(build).render(this, baseClass, id)
}

public fun test() {
    render {
        alert {
            title("foo")
            content {
                +"Description"
            }
            action("Review") {
                clicks handledBy Notification.info("Clicked!")
            }
            closable(true) {
                clicks handledBy Notification.info("Closed!")
            }
            aria {
                role("foo")
                set("label", "bar")
            }
        }
    }
}

// ------------------------------------------------------ component

public class Alert2 :
    PatternFlyComponent<Unit>,
    Aria by AriaMixin(),
    HasTitle by TitleMixin(),
    HasContent<Div, HTMLDivElement> by ContentMixin(),
    ElementProperties<Div, HTMLDivElement> by ElementMixin(),
    EventProperties<HTMLDivElement> by EventMixin() {

    private lateinit var root: Tag<HTMLElement>
    private var severity: Severity = Severity.INFO
    private var closable: Boolean = false
    private var inline: Boolean = false
    private var closeAction: (EventContext<HTMLButtonElement>.() -> Unit)? = null
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

    public fun closable(closable: Boolean, action: (EventContext<HTMLButtonElement>.() -> Unit)? = null) {
        this.closable = closable
        this.closeAction = action
    }

    public fun inline(inline: Boolean) {
        this.inline = inline
    }

    public fun action(text: String, action: EventContext<HTMLButtonElement>.() -> Unit = {}) {
        actions[text] = action
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
                events(this)
                element(this)

                div(baseClass = "alert".component("icon")) {
                    icon(severity.iconClass)
                }
                h4(baseClass = "alert".component("title")) {
                    span(baseClass = "pf-screen-reader") {
                        +ariaLabels.first
                    }
                    +title
                }
                if (closable) {
                    div(baseClass = "alert".component("action")) {
                        pushButton(ButtonVariation.plain) {
                            icon("times".fas())
                            aria["label"] = ariaLabels.second
                            domNode.addEventListener(Events.click.name, this@Alert2::close)
                            closeAction?.invoke(this)
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
