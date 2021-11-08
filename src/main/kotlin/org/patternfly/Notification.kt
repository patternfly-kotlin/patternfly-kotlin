package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.render
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.querySelector
import org.patternfly.dom.removeFromParent
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadingElement
import kotlin.js.Date

// ------------------------------------------------------ factory

public fun <T> notification(
    severity: Severity = Severity.INFO,
    title: String = "",
    build: NotificationAlert.(T) -> Unit = {}
): Handler<T> = NotificationStore.addInternal(severity, title, build)

public fun RenderContext.notificationBadge(
    baseClass: String? = null,
    id: String? = null,
    build: NotificationBadge.() -> Unit = {}
) {
    NotificationBadge().apply(build).render(this, baseClass, id)
}

// ------------------------------------------------------ component

internal class NotificationAlertGroup : BaseAlertGroup(true) {

    private val timeoutHandles: MutableMap<String, Int> = mutableMapOf()

    override fun renderAlerts(context: RenderContext) {
        with(context) {
            (MainScope() + job).launch {
                NotificationStore.latest.collect { notificationAlert ->
                    val alertId = Id.unique("alert")
                    val li = Li(baseClass = "alert-group".component("item"), job = Job(), scope = Scope())
                    with(li) {
                        alert(
                            severity = notificationAlert.severity,
                            id = alertId
                        ) {
                            severity(notificationAlert.severity)
                            title(notificationAlert.title)
                            notificationAlert.content?.let {
                                content {
                                    it.context(this)
                                }
                            }
                            closable(true)
                            element {
                                with(domNode) {
                                    onmouseover = { this@NotificationAlertGroup.stopTimeout(alertId) }
                                    onmouseout = { this@NotificationAlertGroup.startTimeout(alertId, li.domNode) }
                                    this@NotificationAlertGroup.startTimeout(alertId, li.domNode)
                                }
                            }
                        }
                    }
                    domNode.prepend(li.domNode)
                }
            }
        }
    }

    private fun startTimeout(id: String, element: HTMLElement) {
        val handle = window.setTimeout({ element.removeFromParent() }, Settings.NOTIFICATION_TIMEOUT)
        timeoutHandles[id] = handle
    }

    private fun stopTimeout(id: String) {
        timeoutHandles[id]?.let { window.clearTimeout(it) }
    }
}

/**
 * PatternFly [notification badge](https://www.patternfly.org/v4/components/notification-badge/design-guidelines) component.
 *
 * The notification badge is intended to be used with the notification drawer as a visible indicator to alert the user about incoming notifications.
 *
 * The notification badge is typically part of the content area inside [Page.masthead].
 */
public class NotificationBadge : PatternFlyComponent<Unit> {

    private var withCount: Boolean = false

    public fun withCount(value: Boolean) {
        this.withCount = value
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            button(baseClass = classes(ComponentType.NotificationBadge, baseClass), id = id) {
                markAs(ComponentType.NotificationBadge)
                aria["label"] = NotificationStore.unread.map { unread ->
                    if (unread) "Unread notifications" else "Notifications"
                }
                span(baseClass = "notification-badge".component()) {
                    className(
                        NotificationStore.data.map { notifications ->
                            when {
                                notifications.isEmpty() -> "read".modifier()
                                notifications.any { it.severity == Severity.DANGER } -> "attention".modifier()
                                else -> "unread".modifier()
                            }
                        }
                    )
                    icon("bell".pfIcon()) {
                        iconClass(
                            NotificationStore.data.map { notifications ->
                                if (notifications.any { it.severity == Severity.DANGER })
                                    "attention-bell".pfIcon()
                                else
                                    "bell".pfIcon()
                            }
                        )
                    }
                    if (withCount) {
                        span(baseClass = "notification-badge".component("count")) {
                            NotificationStore.count.asText()
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------ store

internal val NOTIFICATION_ALERT_GROUP_ID = Id.unique("notification", ComponentType.AlertGroup.id)

internal object NotificationStore : RootStore<List<NotificationAlert>>(listOf()) {

    private var toastAlertGroupPresent: Boolean = false

    val latest: Flow<NotificationAlert> = data
        .map {
            it.maxByOrNull { n -> n.timestamp }
        }
        .filterNotNull()
        .distinctUntilChanged()

    val unread: Flow<Boolean> = data.map { it.any { n -> !n.read } }

    val count: Flow<Int> = data.map { it.size }

    val clear: Handler<Unit> = handle { listOf() }

    fun <T> addInternal(severity: Severity, title: String, build: NotificationAlert.(T) -> Unit): Handler<T> {
        if (!toastAlertGroupPresent) {
            if (document.querySelector(By.id(NOTIFICATION_ALERT_GROUP_ID)) == null) {
                render(override = false) {
                    NotificationAlertGroup().render(context = this, baseClass = null, id = NOTIFICATION_ALERT_GROUP_ID)
                }
                toastAlertGroupPresent = true
            }
        }

        return handle { items, payload ->
            items + NotificationAlert(severity, title).apply {
                build(this, payload)
            }
        }
    }
}

public class NotificationAlert(internal var severity: Severity, title: String) :
    WithTitle<H, HTMLHeadingElement> by TitleMixin() {

    internal val read: Boolean = false
    internal val timestamp: Long = Date.now().toLong()
    internal var content: SubComponent<RenderContext>? = null

    init {
        this.title(title)
    }

    public fun severity(severity: Severity) {
        this.severity = severity
    }

    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit = {}
    ) {
        this.content = SubComponent(baseClass, id, context)
    }
}
