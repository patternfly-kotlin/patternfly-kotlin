package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.ItemsStore
import org.patternfly.PageInfo
import org.patternfly.pagination
import org.patternfly.toolbar
import org.patternfly.toolbarContent
import org.patternfly.toolbarContentSection
import org.patternfly.toolbarItem

internal interface PaginationSample {

    fun itemStore() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemsStore<Demo> { it.id }

            pagination(store)
        }
    }

    fun pageInfo() {
        render {
            pagination(PageInfo(10, 0, 73))
        }
    }

    fun toolbar() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemsStore<Demo> { it.id }

            toolbar {
                toolbarContent {
                    toolbarContentSection {
                        toolbarItem {
                            pagination(store)
                        }
                    }
                }
            }
        }
    }
}
