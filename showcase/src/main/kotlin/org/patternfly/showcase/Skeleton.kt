package org.patternfly.showcase

import dev.fritz2.binding.const
import org.patternfly.Orientation.VERTICAL
import org.patternfly.layout
import org.patternfly.pfAlertGroup
import org.patternfly.pfBrand
import org.patternfly.pfBrandLink
import org.patternfly.pfHeader
import org.patternfly.pfHeaderTools
import org.patternfly.pfMain
import org.patternfly.pfNavigation
import org.patternfly.pfNavigationExpandableGroup
import org.patternfly.pfNavigationItem
import org.patternfly.pfNavigationItems
import org.patternfly.pfNotificationBadge
import org.patternfly.pfPage
import org.patternfly.pfSidebar

object Skeleton {
    val elements = renderAll(
        { pfAlertGroup(true) },
        {
            pfPage {
                pfHeader {
                    pfBrand {
                        pfBrandLink("#${Places.SERVER}") {
                            img {
                                src = const("/header-logo.svg")
                            }
                        }
                    }
                    pfHeaderTools {
                        div("toolbar".layout()) {
                            div("toolbar".layout("group")) {
                                div("toolbar".layout("item")) {
                                    pfNotificationBadge()
                                }
                                div("toolbar".layout("item")) {
                                }
                            }
                        }
                    }
                }
                pfSidebar {
                    pfNavigation(cdi().placeManager, { it.token }, VERTICAL) {
                        pfNavigationItems {
                            pfNavigationItem(PlaceRequest(Places.DEPLOYMENT), "Deployment")
//                            pfNavigationItem(PlaceRequest(Places.SERVER), "Servers")
//                            pfNavigationItem(PlaceRequest(Places.MANAGEMENT), "Management Model")
                            pfNavigationExpandableGroup("Group 1") {
//                                pfNavigationItem(PlaceRequest(Places.DEPLOYMENT), "Deployment")
                                pfNavigationItem(PlaceRequest(Places.SERVER), "Servers")
                                pfNavigationItem(PlaceRequest(Places.MANAGEMENT), "Management Model")
                            }
                            pfNavigationExpandableGroup("Group 2") {
                                pfNavigationItem(PlaceRequest("foo"), "Foo")
                                pfNavigationItem(PlaceRequest("bar"), "Bar")
                                pfNavigationItem(PlaceRequest("baz"), "Baz")
                            }
                        }
                    }
                }
                pfMain {
                    cdi().placeManager.manage(this)
                }
            }
        }
    )
}
