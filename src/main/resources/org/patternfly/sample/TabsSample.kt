package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.TabStore
import org.patternfly.item
import org.patternfly.items
import org.patternfly.updateItems
import org.patternfly.tabs
import org.patternfly.textContent

internal interface TabsSample {

    fun tabs() {
        render {
            tabs<String> {
                items {
                    item("Users") { +"Users" }
                    item("Containers") { +"Containers" }
                    item("Database") { +"Database" }
                    item("Server") { +"Server" }
                    item("System") { +"System" }
                    item("Network") { +"Network" }
                }
            }
        }
    }

    fun store() {
        render {
            data class Category(
                val id: String,
                val name: String,
                val description: String
            )

            fun loadCategories(): List<Category> {
                // fetching categories from backend
                return emptyList()
            }

            val store = TabStore<Category> { it.id }
            tabs(store) {
                tabDisplay { +it.name }
                contentDisplay {
                    textContent {
                        dl {
                            dt { +it.name }
                            dd { +it.description }
                        }
                    }
                }
            }

            store.updateItems {
                loadCategories().forEach { item(it) }
            }
        }
    }
}
