package org.patternfly.showcase

import kotlin.browser.document

fun main() {
    kotlinext.js.require("@patternfly/patternfly/patternfly.css")
    kotlinext.js.require("@patternfly/patternfly/patternfly-addons.css")

    Skeleton.elements.forEach {
        document.body!!.appendChild(it.domNode)
    }
}
