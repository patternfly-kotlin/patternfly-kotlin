package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import dev.fritz2.lenses.IdProvider
import org.patternfly.dom.Id
import org.patternfly.tabs

internal class TabsSample {

    fun tabs() {
        render {
            tabs {
                item {
                    tab("Users")
                    content { +"Users" }
                }
                item {
                    tab("Containers")
                    content { +"Containers" }
                }
                item {
                    tab("Database")
                    content { +"Database" }
                }
                item {
                    tab("Server")
                    content { +"Server" }
                }
                item {
                    tab("System")
                    content { +"System" }
                }
                item {
                    tab("Network")
                    content { +"Network" }
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

            val categories = storeOf(listOf<Category>())
            val idProvider: IdProvider<Category, String> = {
                Id.build("tab", "item", it.id)
            }

            tabs {
                items(categories, idProvider) { category ->
                    item {
                        tab { +category.name }
                        content {
                            dl {
                                dt { +category.name }
                                dd { +category.description }
                            }
                        }
                    }
                }
            }
        }
    }
}
