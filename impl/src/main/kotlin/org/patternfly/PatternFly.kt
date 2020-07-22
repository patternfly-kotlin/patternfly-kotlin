package org.patternfly

import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.get

internal fun Element.componentType(componentType: ComponentType) {
    setAttribute(Dataset.COMPONENT_TYPE.long, componentType.id)
    if (ouia()) {
        setAttribute(Dataset.OUIA_COMPONENT_TYPE.long, componentType.name)
    }
}

private fun ouia(): Boolean = window.localStorage["ouia"].toString() == "true"
