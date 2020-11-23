package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLButtonElement
import kotlin.js.Date

// ------------------------------------------------------ dsl

public fun RenderContext.notificationBadge(
    id: String? = null,
    baseClass: String? = null,
): NotificationBadge = register(NotificationBadge(id = id, baseClass = baseClass, job), {})

// ------------------------------------------------------ tag

public class NotificationBadge internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLButtonElement>,
    Button(id = id, baseClass = classes(ComponentType.NotificationBadge, baseClass), job) {

    init {
        markAs(ComponentType.NotificationBadge)
        attr("aria-label", NotificationStore.unread.map { unread ->
            if (unread) "Unread notifications" else "Notifications"
        })
        span(baseClass = "notification-badge".component()) {
            classMap(NotificationStore.unread.map { unread ->
                mapOf("read".modifier() to !unread, "unread".modifier() to unread)
            })
            icon("bell".fas())
        }
    }
}

// ------------------------------------------------------ data & store

public data class Notification(
    val severity: Severity,
    val text: String,
    val details: String? = null,
    internal val read: Boolean = false,
    internal val timestamp: Long = Date.now().toLong()
) {
    public companion object {
        public fun error(text: String, details: String? = null): SimpleHandler<Unit> =
            NotificationStore.push(Notification(Severity.DANGER, text, details))

        public fun info(text: String, details: String? = null): SimpleHandler<Unit> =
            NotificationStore.push(Notification(Severity.INFO, text, details))

        public fun success(text: String, details: String? = null): SimpleHandler<Unit> =
            NotificationStore.push(Notification(Severity.SUCCESS, text, details))

        public fun warning(text: String, details: String? = null): SimpleHandler<Unit> =
            NotificationStore.push(Notification(Severity.WARNING, text, details))
    }
}

public object NotificationStore : RootStore<List<Notification>>(listOf()) {

    public fun push(notification: Notification): SimpleHandler<Unit> =
        handle { notifications-> notifications + notification }

    public val clear: SimpleHandler<Unit> = handle { listOf() }

    public val latest: Flow<Notification> = data
        .map {
            it.maxByOrNull { n -> n.timestamp }
        }
        .filterNotNull()
        .distinctUntilChanged()

    public val unread: Flow<Boolean> = data.map { it.any { n -> !n.read } }.distinctUntilChanged()
}
