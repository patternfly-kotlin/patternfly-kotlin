@file:Suppress("SpellCheckingInspection")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.textContent

internal class TextContentSample {

    fun textContent() {
        render {
            textContent {
                h1 { +"Hello world" }
                p { +"Lorem ipsum dolor sit amet." }
                h2 { +"Second level" }
                p {
                    +"Curabitur accumsan turpis pharetra "
                    strong { +"augue tincidunt" }
                    +" blandit."
                }
                ul {
                    li { +"In fermentum leo eu lectus mollis." }
                    li { +"Morbi eu nulla lobortis." }
                    li { +"Lobortis est in." }
                    li { +"Integer in volutpat libero." }
                }
                h3 { +"Third level" }
                p {
                    +"Quisque ante lacus, malesuada ac auctor vitae, "
                    a {
                        +"non ante"
                        href("#")
                    }
                    +". Phasellus lacus ex, semper ac tortor nec."
                }
                blockquote { +"Ut blandit est tellus sit amet turpis." }
                p {
                    +"Etiam auctor nisl et "
                    em { +"justo sodales" }
                    +" elementum."
                }
                hr {}
                p { +"Sed sagittis enim ac tortor maximus rutrum." }
            }
        }
    }
}
