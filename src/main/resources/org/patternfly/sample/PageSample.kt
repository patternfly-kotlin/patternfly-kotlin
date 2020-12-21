package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import dev.fritz2.routing.Router
import dev.fritz2.routing.StringRoute
import org.patternfly.brand
import org.patternfly.headerTools
import org.patternfly.horizontalNavigation
import org.patternfly.navigationItem
import org.patternfly.navigationItems
import org.patternfly.notificationBadge
import org.patternfly.page
import org.patternfly.pageHeader
import org.patternfly.pageMain
import org.patternfly.pageSection
import org.patternfly.pageSidebar
import org.patternfly.sidebarBody
import org.patternfly.verticalNavigation

internal interface PageSample {

    fun typicalSetup() {
        val router = Router(StringRoute("#home"))
        render {
            page {
                pageHeader {
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
        }.mount("entrypoint")
    }
}
