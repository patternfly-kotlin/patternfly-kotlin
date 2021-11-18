@file:Suppress("UNUSED_VARIABLE")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import kotlinx.browser.document
import org.patternfly.aria

internal class AriaSample {

    fun tagAria() {
        render {
            val tag = div {
                +"Some text"
            }
            tag.aria["label"] = "More info"
            val yes = "label" in tag.aria
            val moreInfo = tag.aria["label"]
        }
    }

    fun elementAria() {
        val element = document.createElement("div")
        element.aria["label"] = "More info"
        val yes = "label" in element.aria
        val moreInfo = element.aria["label"]
    }
}
