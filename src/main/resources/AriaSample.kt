package org.patternfly.dom

import dev.fritz2.dom.html.render
import dev.fritz2.elemento.aria
import kotlinx.browser.document

internal interface AriaSample {

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
