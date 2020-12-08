package org.patternfly

import dev.fritz2.dom.html.render
import dev.fritz2.routing.Router
import dev.fritz2.routing.StringRoute

internal interface PageSample {

    fun typicalSetup() {
        render {
            val router = Router(StringRoute("#home"))
            page {
                pageHeader(id = "foo") {
                    brand {
                        home("#home")
                        img("/assets/logo.svg")
                    }
                    horizontalNavigation(router) {
                        navigationItems {
                            navigationItem("#item1", "Item 1")
                            navigationItem("#item2", "Item 2")
                        }
                    }
                    headerTools {
                        notificationBadge()
                    }
                }
                pageSidebar {
                    sidebarBody {
                        verticalNavigation(router) {
                            navigationItems {
                                navigationItem("#item1", "Item 1")
                                navigationItem("#item2", "Item 2")
                            }
                        }
                    }
                }
                pageMain {
                    pageSection {
                        h1 { +"Welcome" }
                        p { +"Lorem ipsum" }
                    }
                    pageSection {
                        +"Another section"
                    }
                }
            }
        }
    }
}
