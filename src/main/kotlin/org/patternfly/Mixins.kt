package org.patternfly

import dev.fritz2.dom.EventContext
import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

public interface WithTitle<E : WithText<N>, N : Node> {
    public var title: Flow<String>

    public fun title(title: String)

    public fun title(title: Flow<String>)

    public fun <T> title(title: Flow<T>)

    public fun Flow<String>.asText()

    public fun <T> Flow<T>.asText()

    public operator fun String.unaryPlus()
}

internal class TitleMixin<E : WithText<N>, N : Node> : WithTitle<E, N> {
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
        this.title = title
    }
}

public interface WithContent<E : WithText<N>, N : Node> {
    public var content: (E.() -> Unit)?

    public fun content(content: String)

    public fun content(content: Flow<String>)

    public fun content(content: E.() -> Unit)
}

internal class ContentMixin<E : WithText<N>, N : Node> : WithContent<E, N> {
    override var content: (E.() -> Unit)? = null

    override fun content(content: String) {
        content { +content }
    }

    override fun content(content: Flow<String>) {
        content { content.asText() }
    }

    override fun content(content: E.() -> Unit) {
        this.content = content
    }
}

public interface WithElement<T : Tag<E>, E : HTMLElement> {
    public var element: T.() -> Unit

    public fun element(build: T.() -> Unit)
}

internal class ElementMixin<T : Tag<E>, E : HTMLElement> : WithElement<T, E> {
    override var element: T.() -> Unit = {}

    override fun element(build: T.() -> Unit) {
        this.element = build
    }
}

public interface WithEvents<T : HTMLElement> {
    public var events: EventContext<T>.() -> Unit

    public fun events(build: EventContext<T>.() -> Unit)
}

internal class EventMixin<T : HTMLElement> : WithEvents<T> {
    override var events: EventContext<T>.() -> Unit = {}

    override fun events(build: EventContext<T>.() -> Unit) {
        this.events = build
    }
}

public interface WithClosable<T : HTMLElement> {
    public var closable: Boolean
    public var closeEvents: (EventContext<T>.() -> Unit)?

    public fun closable(closable: Boolean)

    public fun closeButton(events: EventContext<T>.() -> Unit)
}

internal class ClosableMixin<T : HTMLElement> : WithClosable<T> {
    override var closable: Boolean = true
    override var closeEvents: (EventContext<T>.() -> Unit)? = null

    override fun closable(closable: Boolean) {
        this.closable = closable
    }

    override fun closeButton(events: EventContext<T>.() -> Unit) {
        this.closeEvents = events
    }
}
