package org.patternfly.showcase

import dev.fritz2.binding.const
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.render
import dev.fritz2.routing.Router
import kotlinx.coroutines.flow.map
import org.patternfly.layout
import org.patternfly.pfAlertGroup
import org.patternfly.pfBrand
import org.patternfly.pfBrandLink
import org.patternfly.pfHeader
import org.patternfly.pfHeaderTools
import org.patternfly.pfHorizontalNavigation
import org.patternfly.pfMain
import org.patternfly.pfNavigationExpandableGroup
import org.patternfly.pfNavigationItem
import org.patternfly.pfNavigationItems
import org.patternfly.pfNotificationBadge
import org.patternfly.pfPage
import org.patternfly.pfSidebar
import org.patternfly.pfSidebarBody
import org.patternfly.pfVerticalNavigation
import org.patternfly.util
import org.w3c.dom.HTMLElement

class Skeleton(private val router: Router<String>) : Iterable<Tag<HTMLElement>> {
    override fun iterator(): Iterator<Tag<HTMLElement>> = iterator {
        yield(render { pfAlertGroup(true) })
        yield(render {
            pfPage {
                pfHeader {
                    pfBrand {
                        pfBrandLink("#${Places.HOME}") {
                            img {
                                src = const("/header-logo.svg")
                            }
                        }
                    }
                    pfHorizontalNavigation(router) {
                        pfNavigationItems {
                            pfNavigationItem(Places.GET_STARTED, "Get Started")
                            pfNavigationItem(Places.component("alert"), "Documentation") {
                                it.startsWith(Places.DOCUMENTATION)
                            }
                            pfNavigationItem(Places.CONTRIBUTE, "Contribute")
                            pfNavigationItem(Places.GET_IN_TOUCH, "Get in Touch")
                        }
                    }
                    pfHeaderTools {
                        div("toolbar".layout()) {
                            div("toolbar".layout("group")) {
                                div("toolbar".layout("item")) {
                                    pfNotificationBadge()
                                }
                            }
                        }
                    }
                }
                pfSidebar {
                    classMap = router.routes.map {
                        mapOf("display-none".util() to !it.startsWith(Places.DOCUMENTATION))
                    }
                    pfSidebarBody {
                        pfVerticalNavigation(router) {
                            pfNavigationItems {
                                pfNavigationExpandableGroup("Components") {
                                    pfNavigationItem(Places.component("alert"), "Alert")
                                    pfNavigationItem(Places.component("avatar"), "Avatar")
                                    pfNavigationItem(Places.component("badge"), "Badge")
                                    pfNavigationItem(Places.component("brand"), "Brand")
                                    pfNavigationItem(Places.component("button"), "Button")
                                    pfNavigationItem(Places.component("card"), "Card")
                                    pfNavigationItem(Places.component("chip"), "Chip")
                                    pfNavigationItem(Places.component("chip-group"), "Chip group")
                                    pfNavigationItem(Places.component("content"), "Content")
                                    pfNavigationItem(Places.component("context-selector"), "Context selector")
                                    pfNavigationItem(Places.component("data-list"), "Data list")
                                    pfNavigationItem(Places.component("data-table"), "Data table")
                                    pfNavigationItem(Places.component("dropdown"), "Dropdown")
                                    pfNavigationItem(Places.component("empty-state"), "Empty state")
                                    pfNavigationItem(Places.component("expandable"), "Expandable")
                                    pfNavigationItem(Places.component("label"), "Label")
                                    pfNavigationItem(Places.component("options-menu"), "Options menu")
                                    pfNavigationItem(Places.component("select"), "Select")
                                    pfNavigationItem(Places.component("tabs"), "Tabs")
                                    pfNavigationItem(Places.component("title"), "Title")
                                    pfNavigationItem(Places.component("toolbar"), "Toolbar")
                                }
                                pfNavigationExpandableGroup("Demos") {
                                    pfNavigationItem(Places.component("server-demo"), "Servers")
                                    pfNavigationItem(Places.component("user-demo"), "Users")
                                }
                            }
                        }
                    }
                }
                pfMain {
                    domNode.id = "main"
                }
            }
        })
    }
}
