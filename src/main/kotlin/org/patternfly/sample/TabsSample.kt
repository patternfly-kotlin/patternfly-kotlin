package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import dev.fritz2.lenses.IdProvider
import org.patternfly.dom.Id
import org.patternfly.tabContent
import org.patternfly.tabs
import org.patternfly.textContent

internal class TabsSample {

    fun tabs() {
        render {
            tabs {
                item("users") { +"Users" }
                item("containers") { +"Containers" }
                item("database") { +"Database" }
                item("server") { +"Server" }
                item("system") { +"System" }
                item("network") { +"Network" }
            }
            tabContent("users") { +"Users" }
            tabContent("containers") { +"Containers" }
            tabContent("database") { +"Database" }
            tabContent("server") { +"Server" }
            tabContent("system") { +"System" }
            tabContent("network") { +"Network" }
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
                Id.build("tab", it.id)
            }

            tabs {
                items(categories, idProvider) { category ->
                    item { +category.name }
                }
            }
            categories.data.renderEach(idProvider) { category ->
                tabContent(category, idProvider) {
                    textContent {
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
