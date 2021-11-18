package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.routing.Router
import dev.fritz2.routing.StringRoute
import org.patternfly.ItemsStore
import org.patternfly.breadcrumb
import org.patternfly.card
import org.patternfly.cardBody
import org.patternfly.cardView
import org.patternfly.dom.Id
import org.patternfly.item
import org.patternfly.items
import org.patternfly.modifier
import org.patternfly.navigation
import org.patternfly.page
import org.patternfly.pageBreadcrumb
import org.patternfly.pageGroup
import org.patternfly.pageSection
import org.patternfly.textContent
import org.patternfly.title

internal class PageSample {

    fun typicalSetup() {
        val router = Router(StringRoute("#home"))
        val store = ItemsStore<String> { Id.build(it) }

        render {
            page {
                masthead {
                    toggle()
                    brand("#") {
                        src("/assets/logo.svg")
                    }
                    content {
                        navigation(router) {
                            item("#welcome", "Welcome")
                            item("#getting-started", "Getting Started")
                            item("#contribute", "Contribute")
                        }
                        // TODO toolbar
                    }
                }
                sidebar {
                    navigation(router) {
                        item("#welcome", "Welcome")
                        group("Documentation") {
                            item("#install", "Install")
                            item("#setup", "Setup")
                        }
                    }
                }
                main {
                    pageGroup(baseClass = "sticky-top".modifier()) {
                        pageBreadcrumb {
                            breadcrumb<String> {
                                items {
                                    item("item1", "Item 1")
                                    item("item2", "Item 2")
                                }
                            }
                        }
                    }
                    pageSection {
                        textContent {
                            title { +"Main title" }
                            p { +"Lorem ipsum dolor sit amet." }
                        }
                    }
                    pageSection(limitWidth = true) {
                        cardView(store) {
                            display { item ->
                                card(item) {
                                    cardBody { +item }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
