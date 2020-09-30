package org.patternfly

import dev.fritz2.binding.SingleMountPoint
import dev.fritz2.dom.DomMountPoint
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.Text
import kotlin.math.max

// ------------------------------------------------------ dsl

fun HtmlElements.pfBadge(
    min: Int = 0,
    max: Int = 999,
    id: String? = null,
    baseClass: String? = null,
    content: Badge.() -> Unit = {}
): Badge = register(Badge(min, max, id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

class Badge internal constructor(
    private val min: Int,
    private val max: Int,
    id: String? = null,
    baseClass: String?
) : PatternFlyComponent<HTMLSpanElement>, Span(id = id, baseClass = classes(ComponentType.Badge, baseClass)) {

    var read: Flow<Boolean>
        get() = flowOf(true)
        set(value) {
            classMap = value.map { mapOf("read".modifier() to it, "unread".modifier() to !it) }
        }

    init {
        markAs(ComponentType.Badge)
        classMap = read.map { mapOf("read".modifier() to it, "unread".modifier() to !it) }
    }

    fun Flow<Int>.bind(): SingleMountPoint<WithDomNode<Text>> {
        val upstream = this.map {
            val lower = max(it, min)
            val upper = if (lower > max) "$max+" else lower.toString()
            TextNode(upper)
        }.distinctUntilChanged()
        return DomMountPoint(upstream, domNode)
    }
}
