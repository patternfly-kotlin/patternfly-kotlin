package org.patternfly

import dev.fritz2.dom.EventContext
import dev.fritz2.dom.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement

/**
 * Interface meant to be implemented by components which have a title. The title can be specified either as static string or as a flow of string. Components can use [hasTitle] to check whether a title has been assigned.
 */
public interface WithTitle {
    public val hasTitle: Boolean

    public var title: Flow<String>

    public fun title(title: String)

    public fun title(title: Flow<String>)

    public fun <T> title(title: Flow<T>)

    public fun Flow<String>.asText()

    public fun <T> Flow<T>.asText()

    public operator fun String.unaryPlus()
}

internal class TitleMixin : WithTitle {
    private var assigned: Boolean = false

    override val hasTitle: Boolean
        get() = assigned

    override var title: Flow<String> = emptyFlow()

    override fun title(title: String) {
        assign(flowOf(title))
    }

    override fun title(title: Flow<String>) {
        assign(title)
    }

    override fun <T> title(title: Flow<T>) {
        assign(title.map { it.toString() })
    }

    override fun Flow<String>.asText() {
        assign(this)
    }

    override fun <T> Flow<T>.asText() {
        assign(this.map { it.toString() })
    }

    override fun String.unaryPlus() {
        assign(flowOf(this))
    }

    internal fun assign(title: Flow<String>) {
        this.assigned = true
        this.title = title
    }
}

public interface WithElement {
    public var element: Tag<HTMLElement>.() -> Unit

    public fun element(context: Tag<HTMLElement>.() -> Unit)
}

internal class ElementMixin : WithElement {
    override var element: Tag<HTMLElement>.() -> Unit = {}

    override fun element(context: Tag<HTMLElement>.() -> Unit) {
        this.element = context
    }
}

internal val EMPTY_EVENT_CONTEXT: EventContext<HTMLElement>.() -> Unit = {}

public interface WithEvents {
    public var events: EventContext<HTMLElement>.() -> Unit

    public fun events(context: EventContext<HTMLElement>.() -> Unit)
}

internal class EventMixin : WithEvents {
    override var events: EventContext<HTMLElement>.() -> Unit = EMPTY_EVENT_CONTEXT

    override fun events(context: EventContext<HTMLElement>.() -> Unit) {
        this.events = context
    }
}

public interface WithClosable {
    public var closable: Boolean
    public var closeEvents: (EventContext<HTMLElement>.() -> Unit)?

    public fun closable(closable: Boolean)

    public fun closeButton(events: EventContext<HTMLElement>.() -> Unit)
}

internal class ClosableMixin : WithClosable {
    override var closable: Boolean = true
    override var closeEvents: (EventContext<HTMLElement>.() -> Unit)? = null

    override fun closable(closable: Boolean) {
        this.closable = closable
    }

    override fun closeButton(events: EventContext<HTMLElement>.() -> Unit) {
        this.closeEvents = events
    }
}

/**
 * Interface meant to be implemented by components which can expand / collapse in some form or another.
 */
public interface WithExpandedStore {

    /**
     * The store which holds the expanded / collapse state.
     */
    public val expandedStore: ExpandedStore

    /**
     * The current expanded / collapsed state.
     */
    public val expos: Flow<Boolean>
}

public class ExpandedStoreMixin(collapsePredicate: CollapsePredicate? = null) : WithExpandedStore {

    override val expandedStore: ExpandedStore = ExpandedStore(collapsePredicate)

    override val expos: Flow<Boolean> = expandedStore.data.drop(1)
}
