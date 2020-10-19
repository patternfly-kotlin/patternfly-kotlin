package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLButtonElement
import kotlin.js.Date

// ------------------------------------------------------ dsl

public fun HtmlElements.pfNotificationBadge(id: String? = null, baseClass: String? = null): NotificationBadge =
    register(NotificationBadge(id = id, baseClass = baseClass), {})

// ------------------------------------------------------ tag

public class NotificationBadge internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLButtonElement>,
    Button(id = id, baseClass = classes(ComponentType.NotificationBadge, baseClass)) {

    init {
        markAs(ComponentType.NotificationBadge)
        Notification.store.unread.map { unread ->
            if (unread) "Unread notifications" else "Notifications"
        }.bindAttr("aria-label")
        span(baseClass = "notification-badge".component()) {
            classMap = Notification.store.unread.map { unread ->
                mapOf("read".modifier() to !unread, "unread".modifier() to unread)
            }
            pfIcon("bell".fas())
        }
    }
}

// ------------------------------------------------------ store

public data class Notification(
    val severity: Severity,
    val text: String,
    val details: String? = null,
    internal val read: Boolean = false,
    internal val timestamp: Long = Date.now().toLong()
) {
    public companion object {
        public val store: NotificationStore = NotificationStore()

        public fun error(text: String, details: String? = null): Unit = send(Notification(Severity.DANGER, text, details))
        public fun info(text: String, details: String? = null): Unit = send(Notification(Severity.INFO, text, details))
        public fun success(text: String, details: String? = null): Unit = send(Notification(Severity.SUCCESS, text, details))
        public fun warning(text: String, details: String? = null): Unit = send(Notification(Severity.WARNING, text, details))

        private fun send(notification: Notification) = flowOf(notification) handledBy store.add
    }
}

public class NotificationStore : RootStore<List<Notification>>(listOf()) {
    public val add: SimpleHandler<Notification> = handle { notifications, notification ->
        notifications + notification
    }

    public val clear: SimpleHandler<Unit> = handle { listOf() }

    public val latest: Flow<Notification> = data
        .map {
            it.maxByOrNull { n -> n.timestamp }
        }
        .filterNotNull()
        .distinctUntilChanged()

    public val unread: Flow<Boolean> = data.map { it.any { n -> !n.read } }.distinctUntilChanged()
}
