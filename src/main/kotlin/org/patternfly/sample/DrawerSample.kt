package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.drawer

internal class DrawerSample {

    fun drawerSetup() {
        render {
            drawer {
                primary {
                    content { +"Primary content" }
                    content { +"More content" }
                }
                detail {
                    head {
                        h2 { +"Details" }
                    }
                    content { +"Some details" }
                    content { +"More details" }
                }
            }
        }
    }
}
