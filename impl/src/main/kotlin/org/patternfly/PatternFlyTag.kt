package org.patternfly

import dev.fritz2.dom.Tag
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

open class PatternFlyTag<out E : HTMLElement>(
    componentType: ComponentType,
    tagName: String,
    baseClass: String
) : Tag<E>(tagName, baseClass = baseClass) {
    override val domNode: E = document.createElement(tagName).also { element ->
        element.setAttribute(Dataset.COMPONENT_TYPE.long, componentType.id)
        if (this is Ouia) {
            element.setOuiaType(componentType)
        }
        element.setAttribute("class", baseClass)
    }.unsafeCast<E>()
}
