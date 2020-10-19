package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

public fun HtmlElements.pfIcon(
    iconClass: String? = null,
    id: String? = null,
    baseClass: String? = null,
    content: Icon.() -> Unit = {}
): Icon = register(Icon(iconClass, id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

public class Icon internal constructor(iconClass: String?, id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("i", id = id, baseClass = classes(ComponentType.Icon, baseClass)) {

    public var iconClass: Flow<String>
        get() {
            throw NotImplementedError()
        }
        set(value) {
            (baseClass?.let { value.map { "$baseClass $it" } } ?: value).bindAttr("class")
        }

    init {
        markAs(ComponentType.Icon)
        attr("aria-hidden", "true")
        iconClass?.let {
            it.split(' ').forEach { c -> domNode.classList += c }
        }
    }
}
