@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.ButtonVariant.primary
import org.patternfly.dataList
import org.patternfly.pushButton

internal class DataListSample {

    fun dataList() {
        render {
            dataList {
                item {
                    toggle()
                    check()
                    cell { +"Some content" }
                    action {
                        pushButton(primary) { +"Edit" }
                    }
                    content { +"Expandable content" }
                }
            }
        }
    }
}
