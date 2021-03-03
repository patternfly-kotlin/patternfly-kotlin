package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.routing.Router
import dev.fritz2.routing.StringRoute
import org.patternfly.ItemsStore
import org.patternfly.Sticky
import org.patternfly.avatar
import org.patternfly.brand
import org.patternfly.breadcrumb
import org.patternfly.card
import org.patternfly.cardBody
import org.patternfly.cardView
import org.patternfly.dom.Id
import org.patternfly.horizontalNavigation
import org.patternfly.item
import org.patternfly.items
import org.patternfly.modifier
import org.patternfly.notificationBadge
import org.patternfly.page
import org.patternfly.pageBreadcrumb
import org.patternfly.pageGroup
import org.patternfly.pageHeader
import org.patternfly.pageHeaderTools
import org.patternfly.pageHeaderToolsItem
import org.patternfly.pageMain
import org.patternfly.pageNavigation
import org.patternfly.pageSection
import org.patternfly.pageSidebar
import org.patternfly.tertiaryNavigation
import org.patternfly.textContent
import org.patternfly.title
import org.patternfly.verticalNavigation

internal interface PageSample {

    fun typicalSetup() {
        val router = Router(StringRoute("#home"))
        val store = ItemsStore<String> { Id.build(it) }

        render {
            page {
                pageHeader {
                    brand {
                        link {
                            href("#home")
                        }
                        img {
                            src("/assets/logo.svg")
                        }
                    }
                    horizontalNavigation(router) {
                        items {
                            item("item1", "Item 1")
                            item("item2", "Item 2")
                        }
                    }
                    pageHeaderTools {
                        pageHeaderToolsItem {
                            notificationBadge()
                        }
                        avatar("/assets/images/img_avatar.svg")
                    }
                }
                pageSidebar {
                    verticalNavigation(router) {
                        items {
                            item("item1", "Item 1")
                            item("item2", "Item 2")
                        }
                    }
                }
                pageMain {
                    pageGroup(Sticky.TOP) {
                        pageNavigation(limitWidth = true) {
                            tertiaryNavigation(router) {
                                items {
                                    item("item1", "Item 1")
                                    item("item2", "Item 2")
                                }
                            }
                        }
                        pageBreadcrumb(limitWidth = true) {
                            breadcrumb<String> {
                                items {
                                    item("item1", "Item 1")
                                    item("item2", "Item 2")
                                }
                            }
                        }
                        pageSection(baseClass = "light".modifier()) {
                            textContent {
                                title { +"Main title" }
                                p { +"Lorem ipsum dolor sit amet." }
                            }
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
