package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithText
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

public interface HasTitle {
    public var title: String

    public fun title(title: String)
}

public class TitleMixin : HasTitle {
    override var title: String = ""

    override fun title(title: String) {
        this.title = title
    }
}

public interface HasContent<E : WithText<N>, N : Node> {
    public var content: (E.() -> Unit)?

    public fun content(content: String)
    public fun content(content: Flow<String>)
    public fun content(content: E.() -> Unit)
}

public class ContentMixin<E : WithText<N>, N : Node> : HasContent<E, N> {
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

public interface ElementProperties<T : Tag<E>, E : HTMLElement> {
    public val element: T.() -> Unit
}

public class ElementMixin<T : Tag<E>, E : HTMLElement> : ElementProperties<T, E> {
    override val element: T.() -> Unit = {}
}
