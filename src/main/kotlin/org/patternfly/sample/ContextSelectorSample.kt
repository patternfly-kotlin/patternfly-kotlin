@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.ContextSelectorStore
import org.patternfly.Severity.INFO
import org.patternfly.contextSelector
import org.patternfly.item
import org.patternfly.items
import org.patternfly.notification
import org.patternfly.updateItems

internal interface ContextSelectorSample {

    fun contextSelectorDsl() {
        render {
            contextSelector<String> {
                items {
                    item("My Project") { selected = true }
                    item("OpenShift Cluster")
                    item("Production Ansible")
                    item("AWS")
                    item("Azure")
                    item("My Project 2")
                    item("Production Ansible 2")
                    item("AWS 2")
                }
            }
        }
    }

    fun contextSelectorStore() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ContextSelectorStore<Demo>()
            contextSelector(store) {
                display { demo -> +demo.name }
            }

            store.updateItems {
                item(Demo("foo", "Foo"))
                item(Demo("bar", "Bar"))
            }
        }
    }

    fun expanded() {
        render {
            contextSelector<String> {
                expanded.data handledBy notification(INFO) { expanded ->
                    title("Expanded state of context selector: $expanded.")
                }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun items() {
        render {
            contextSelector<String> {
                items {
                    item("Item 1")
                    item("Item 2")
                }
            }
        }
    }
}
