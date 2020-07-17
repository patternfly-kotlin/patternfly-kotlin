package org.patternfly

import kotlinx.browser.window
import org.patternfly.Dataset.OUIA_COMPONENT_TYPE
import org.w3c.dom.Element
import org.w3c.dom.get

internal fun Element.setOuiaType(componentType: ComponentType) {
    if (isSupported()) {
        this.setAttribute(OUIA_COMPONENT_TYPE.long, componentType.name)
    }
}

// TODO Why doesn't toBoolean() work here?
private fun isSupported(): Boolean = window.localStorage["ouia"].toString() == "true"

internal interface Ouia
