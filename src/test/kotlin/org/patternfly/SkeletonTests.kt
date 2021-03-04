package org.patternfly

import dev.fritz2.dom.html.render
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.browser.document
import org.w3c.dom.asList

@Suppress("unused")
class SkeletonTests : StringSpec({

    beforeEach {
        initDocument()
    }

    "should be created" {
        render {
            skeleton(id = "skeleton", baseClass = "additional-class es")
        }

        val element = document.getElementById("skeleton")!!
        element.nodeName shouldBe "DIV"
        element.classList.asList() shouldBe listOf("pf-c-skeleton", "additional-class", "es")
        element.childElementCount shouldBe 0
    }

    "should be modifiable in shape and size" {
        render {
            skeleton(id = "small-circle", shape = Shape.CIRCLE, width = Width.SM)
        }

        val element = document.getElementById("small-circle")!!
        element.classList.asList() shouldBe listOf("pf-c-skeleton", "pf-m-width-sm", "pf-m-circle")
    }

    "should be accessible" {
        render {
            skeleton(id = "accessible-skeleton") { +"Content loading..." }
        }

        val element = document.getElementById("accessible-skeleton")!!
        element.childElementCount shouldBe 1
        element.firstElementChild!!.nodeName shouldBe "SPAN"
        element.firstElementChild!!.classList.asList() shouldBe listOf("pf-u-screen-reader")
        element.firstElementChild!!.textContent shouldBe "Content loading..."
    }
})
