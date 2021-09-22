package org.patternfly.component

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import kotlinx.browser.window
import org.patternfly.ComponentType
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.set

private const val COMPONENT_TYPE: String = "pfct"

internal fun Tag<HTMLElement>.markAs(componentType: ComponentType) {
    domNode.dataset[COMPONENT_TYPE] = componentType.id
    if (window.localStorage["ouia"].toString() == "true") {
        domNode.dataset["ouiaComponentType"] = componentType.name
    }
}

internal interface PatternFlyComponent2<T> {
    fun render(
        context: RenderContext,
        baseClass: String?,
        id: String?,
    ): T
}
