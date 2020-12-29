package org.patternfly.sample

import dev.fritz2.dom.html.render
import kotlinx.browser.document
import org.patternfly.dom.debug

internal interface DebugSample {

    fun debug() {
        render {
            val menu = nav {
                ul {
                    li {
                        a { +"Foo"; href("#foo") }
                    }
                    li {
                        a { +"Bar"; href("#bar") }
                    }
                }
            }
            console.log(menu.domNode.debug()) // <nav></nav>

            val link = a {
                href("#foo")
                +"Foo"
            }
            console.log(link.domNode.debug()) // <a href="#foo"></a>

            val img = img { src("./logo.svg") }
            console.log(img.domNode.debug()) // <img src="./logo.svg"/>

            val br = document.createElement("br")
            console.log(br.debug()) // <br/>
        }
    }
}
