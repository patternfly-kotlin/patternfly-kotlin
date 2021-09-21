package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.set

public interface PatternFlyComponent2<T> {

    public fun render(
        context: RenderContext,
        baseClass: String?,
        id: String?,
    ): T
}

private const val COMPONENT_TYPE: String = "pfct"

internal fun Tag<HTMLElement>.markAs(componentType: ComponentType) {
    domNode.dataset[COMPONENT_TYPE] = componentType.id
    if (window.localStorage["ouia"].toString() == "true") {
        domNode.dataset["ouiaComponentType"] = componentType.name
    }
}
