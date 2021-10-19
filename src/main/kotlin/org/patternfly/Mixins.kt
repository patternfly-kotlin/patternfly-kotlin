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

    public fun Flow<String>.asText()

    public fun <T> Flow<T>.asText()

    public operator fun String.unaryPlus()
}

internal class TitleMixin<E : WithText<N>, N : Node> : WithTitle<E, N> {
    override var title: Flow<String> = emptyFlow()

    override fun title(title: String) {
        this.title = flowOf(title)
    }

    override fun Flow<String>.asText() {
        this@TitleMixin.title = this
    }

    override fun <T> Flow<T>.asText() {
        this@TitleMixin.title = this.map { it.toString() }
    }

    override fun String.unaryPlus() {
        this@TitleMixin.title = flowOf(this)
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
    public var closeAction: (EventContext<T>.() -> Unit)?

    public fun closable(action: (EventContext<T>.() -> Unit)? = null)
}

internal class ClosableMixin<T : HTMLElement> : WithClosable<T> {
    override var closable: Boolean = false
    override var closeAction: (EventContext<T>.() -> Unit)? = null

    override fun closable(action: (EventContext<T>.() -> Unit)?) {
        this.closable = true
        this.closeAction = action
    }
}
