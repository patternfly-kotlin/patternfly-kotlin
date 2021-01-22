package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.patternfly.NotificationStore.addInternal
import org.patternfly.dom.aria
import org.w3c.dom.HTMLButtonElement
import kotlin.js.Date

// ------------------------------------------------------ dsl

/**
 * Creates the [NotificationBadge] component.
 *
 * @param withCount whether to show the number of unread notifications
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 */
public fun RenderContext.notificationBadge(
    withCount: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
): NotificationBadge = register(NotificationBadge(withCount, id = id, baseClass = baseClass, job), {})

// ------------------------------------------------------ tag

/**
 * PatternFly [notification badge](https://www.patternfly.org/v4/components/notification-badge/design-guidelines) component.
 *
 * The notification badge is intended to be used with the notification drawer as a visible indicator to alert the user about incoming notifications. It uses the [NotificationStore] to decide which visible indicator to show.
 *
 * The notification badge is typically part of the [pageHeaderTools].
 */
public class NotificationBadge internal constructor(withCount: Boolean, id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLButtonElement>,
    Button(id = id, baseClass = classes(ComponentType.NotificationBadge, baseClass), job) {

    init {
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

// ------------------------------------------------------ data & store

/**
 * Helper class used by [Notification.add] to quickly create [Notification]s with a given [Severity].
 */
public class NotificationScope {

    /**
     * Creates a [Notification] with severity [Severity.DEFAULT].
     */
    public fun default(text: String, details: String? = null): Notification =
        Notification(Severity.DEFAULT, text, details)

    /**
     * Creates a [Notification] with severity [Severity.DANGER].
     */
    public fun error(text: String, details: String? = null): Notification =
        Notification(Severity.DANGER, text, details)

    /**
     * Creates a [Notification] with severity [Severity.INFO].
     */
    public fun info(text: String, details: String? = null): Notification =
        Notification(Severity.INFO, text, details)

    /**
     * Creates a [Notification] with severity [Severity.SUCCESS].
     */
    public fun success(text: String, details: String? = null): Notification =
        Notification(Severity.SUCCESS, text, details)

    /**
     * Creates a [Notification] with severity [Severity.WARNING].
     */
    public fun warning(text: String, details: String? = null): Notification =
        Notification(Severity.WARNING, text, details)
}

/**
 * Data class for a notification. Normally you don't need to create notifications yourself. Use one of the helper function defined in the [companion object][Notification.Companion] instead.
 */
public data class Notification(
    val severity: Severity,
    val text: String,
    val details: String? = null,
    internal val read: Boolean = false,
    internal val timestamp: Long = Date.now().toLong()
) {

    /**
     * Contains functions to easily add notifications from source [flows][Flow], [emitting handlers][dev.fritz2.binding.EmittingHandler] or [listeners][dev.fritz2.dom.Listener].
     *
     * There are two flavours of functions:
     *
     * 1. Functions [default], [error], [info], [success] and [warning]: Use these functions, if you want to add static notifications and don't need the payload from the source flow.
     * 1. Function [add]: Use this function, if you want to use the payload from the source flow to create the notification.
     *
     * @sample org.patternfly.sample.ButtonSample.clickButton
     * @sample org.patternfly.sample.ChipGroupSample.remove
     */
    public companion object {

        /**
         * Creates a handler which adds a static [Notification] with severity [Severity.DEFAULT]
         */
        public fun default(text: String, details: String? = null): Handler<Unit> =
            addInternal { default(text, details) }

        /**
         * Creates a handler which adds a static [Notification] with severity [Severity.DANGER]
         */
        public fun error(text: String, details: String? = null): Handler<Unit> =
            addInternal { error(text, details) }

        /**
         * Creates a handler which adds a static [Notification] with severity [Severity.INFO]
         */
        public fun info(text: String, details: String? = null): Handler<Unit> =
            addInternal { info(text, details) }

        /**
         * Creates a handler which adds a static [Notification] with severity [Severity.SUCCESS]
         */
        public fun success(text: String, details: String? = null): Handler<Unit> =
            addInternal { success(text, details) }

        /**
         * Creates a handler which adds a static [Notification] with severity [Severity.WARNING]
         */
        public fun warning(text: String, details: String? = null): Handler<Unit> =
            addInternal { warning(text, details) }

        /**
         * Creates a handler which adds the notification created by the specified function.
         *
         * The function uses [NotificationScope] as its receiver and the payload from the source [flow][Flow], [emitting handler][dev.fritz2.binding.EmittingHandler] or [listener][dev.fritz2.dom.Listener].
         *
         * @param T the type of the source [Flow]
         * @param block the function to create the [Notification]
         *
         * @sample org.patternfly.sample.NotificationSample.add
         */
        public fun <T> add(block: NotificationScope.(T) -> Notification): Handler<T> =
            addInternal(block)
    }
}

/**
 * Store for [Notification]s.
 */
public object NotificationStore : RootStore<List<Notification>>(listOf()) {

    /**
     * The latest notification added to this store.
     */
    public val latest: Flow<Notification> = data
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
    public val add: Handler<Notification> = handle { notifications, notification ->
        notifications + notification
    }

    /**
     * Removes all notifications from this store.
     */
    public val clear: Handler<Unit> = handle { listOf() }

    internal fun <T> addInternal(block: NotificationScope.(T) -> Notification): Handler<T> =
        handle { notifications, notification ->
            notifications + block(NotificationScope(), notification)
        }
}
