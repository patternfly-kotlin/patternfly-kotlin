package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.render
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
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

/**
 * Creates a notification.
 *
 * @sample org.patternfly.sample.NotificationSample.add
 */
public fun <T> notification(
    severity: Severity = Severity.INFO,
    title: String = "",
    context: NotificationAlert.(T) -> Unit = {}
): Handler<T> = NotificationStore.addInternal(severity, title, context)

public fun RenderContext.notificationBadge(
    baseClass: String? = null,
    id: String? = null,
    context: NotificationBadge.() -> Unit = {}
) {
    NotificationBadge().apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

internal class NotificationAlertGroup : BaseAlertGroup(true) {

    private val timeoutHandles: MutableMap<String, Int> = mutableMapOf()

    override fun renderAlerts(context: Tag<HTMLElement>) {
        with(context) {
            (MainScope() + NotificationStore.job).launch {
                NotificationStore.latest.collect { notificationAlert ->
                    val alertId = Id.unique("alert")
                    val li = Li(baseClass = "alert-group".component("item"), job = Job(), scope = Scope())
                    with(li) {
                        alert(
                            severity = notificationAlert.severity,
                            id = alertId
                        ) {
                            severity(notificationAlert.severity)
                            if (notificationAlert.staticTitle != null) {
                                title(notificationAlert.staticTitle!!)
                            } else if (notificationAlert.flowTitle != null) {
                                title(notificationAlert.flowTitle!!)
                            }
                            notificationAlert.content?.let {
                                content(it)
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
public open class NotificationBadge : PatternFlyComponent<Unit> {

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
                            NotificationStore.count.renderText(into = this)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------ store

internal val NOTIFICATION_ALERT_GROUP_ID = Id.unique("notification", ComponentType.AlertGroup.id)

/**
 * Store for [NotificationAlert]s.
 */
public object NotificationStore : RootStore<List<NotificationAlert>>(listOf()) {

    private var toastAlertGroupPresent: Boolean = false

    /**
     * The latest notification added to this store.
     */
    public val latest: Flow<NotificationAlert> = data
        .map {
            it.maxByOrNull { n -> n.timestamp }
        }
        .filterNotNull()
        .distinctUntilChanged()

    /**
     * Whether this store has unread notifications.
     */
    public val unread: Flow<Boolean> = data.map { it.any { n -> !n.read } }

    /**
     * The number of notifications.
     */
    public val count: Flow<Int> = data.map { it.size }

    /**
     * Adds the specified notification to the list of notifications.
     */
    public val add: Handler<NotificationAlert> =
        handle { notifications, notification ->
            notifications + notification
        }

    /**
     * Removes all notifications from this store.
     */
    public val clear: Handler<Unit> = handle { listOf() }

    internal fun <T> addInternal(
        severity: Severity,
        title: String,
        context: NotificationAlert.(T) -> Unit
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
            items + NotificationAlert(severity, title).apply {
                context(this, payload)
            }
        }
    }
}

public class NotificationAlert(internal var severity: Severity, title: String) {

    internal val read: Boolean = false
    internal var flowTitle: Flow<String>? = null
    internal var staticTitle: String? = null
    internal val timestamp: Long = Date.now().toLong()
    internal var content: (RenderContext.() -> Unit)? = null

    init {
        this.title(title)
    }

    public operator fun String.unaryPlus() {
        staticTitle = this
    }

    public fun title(title: String) {
        staticTitle = title
    }

    public fun title(title: Flow<String>) {
        flowTitle = title
    }

    public fun <T> title(title: Flow<T>) {
        flowTitle = title.map { it.toString() }
    }

    public fun Flow<String>.asText() {
        flowTitle = this
    }

    public fun <T> Flow<T>.asText() {
        flowTitle = this.map { it.toString() }
    }

    public fun severity(severity: Severity) {
        this.severity = severity
    }

    public fun content(content: RenderContext.() -> Unit) {
        this.content = content
    }
}
