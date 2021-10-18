package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
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
import kotlin.js.Date

// ------------------------------------------------------ factory

public fun <T> notification(
    baseClass: String? = null,
    build: Alert.(T) -> Unit
): Handler<T> {
    return NotificationStore.addInternal(baseClass, null, build)
}

public object Notification {
    public fun default(title: String, content: String? = null): Handler<Unit> = toast(Severity.DEFAULT, title, content)

    public fun error(title: String, content: String? = null): Handler<Unit> = toast(Severity.DANGER, title, content)

    public fun info(title: String, content: String? = null): Handler<Unit> = toast(Severity.INFO, title, content)

    public fun success(title: String, content: String? = null): Handler<Unit> = toast(Severity.SUCCESS, title, content)

    public fun warning(title: String, content: String? = null): Handler<Unit> = toast(Severity.WARNING, title, content)

    private fun toast(severity: Severity, title: String, content: String? = null): Handler<Unit> =
        NotificationStore.addInternal(baseClass = null, knownSeverity = severity) {
            severity(severity)
            title(title)
            content?.let { content(it) }
        }
}

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
                NotificationStore.latest.collect { toastContext ->
                    val alertId = Id.unique("alert")
                    val li = Li(baseClass = "alert-group".component("item"), job = Job(), scope = Scope())
                    with(li) {
                        alert(id = alertId) {
                            toastContext.build(this)
                            // always apply these settings (might override toastContext.build)
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
 * The notification badge is typically part of the [pageHeaderTools].
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
                                notifications.any { it.knownSeverity == Severity.DANGER } -> "attention".modifier()
                                else -> "unread".modifier()
                            }
                        }
                    )
                    icon("bell".pfIcon()) {
                        iconClass(
                            NotificationStore.data.map { notifications ->
                                if (notifications.any { it.knownSeverity == Severity.DANGER })
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

internal object NotificationStore : RootStore<List<NotificationContext>>(listOf()) {

    private var toastAlertGroupPresent: Boolean = false

    val latest: Flow<NotificationContext> = data
        .map {
            it.maxByOrNull { n -> n.timestamp }
        }
        .filterNotNull()
        .distinctUntilChanged()

    val unread: Flow<Boolean> = data.map { it.any { n -> !n.read } }

    val count: Flow<Int> = data.map { it.size }

    val clear: Handler<Unit> = handle { listOf() }

    fun <T> addInternal(
        baseClass: String?,
        knownSeverity: Severity?,
        build: Alert.(T) -> Unit
    ): Handler<T> {
        if (!toastAlertGroupPresent) {
            if (document.querySelector(By.id(NOTIFICATION_ALERT_GROUP_ID)) == null) {
                render(override = false) {
                    NotificationAlertGroup().render(context = this, baseClass = null, id = NOTIFICATION_ALERT_GROUP_ID)
                }
                toastAlertGroupPresent = true
            }
        }

        return handle { items, payload ->
            items + NotificationContext(baseClass, knownSeverity) {
                build(this, payload)
            }
        }
    }
}

internal class NotificationContext(
    val baseClass: String?,
    val knownSeverity: Severity?,
    val build: Alert.() -> Unit
) {
    val read: Boolean = false
    val timestamp: Long = Date.now().toLong()
}
