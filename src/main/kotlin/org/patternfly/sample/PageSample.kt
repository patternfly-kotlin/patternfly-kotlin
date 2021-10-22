package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.routing.Router
import dev.fritz2.routing.StringRoute
import org.patternfly.ItemsStore
import org.patternfly.dom.Id
import org.patternfly.page

internal interface PageSample {

    fun typicalSetup() {
        val router = Router(StringRoute("#home"))
        val store = ItemsStore<String> { Id.build(it) }

        render {
            page {
                masthead {
                    toggle()
                    brand {
                    }
                    content {
                    }
                }
                sidebar {

                }
                main {

                }

//                pageHeader {
//                    brand {
//                        link {
//                            href("#home")
//                        }
//                        img {
//                            src("/assets/logo.svg")
//                        }
//                    }
//                    horizontalNavigation(router) {
//                        items {
//                            item("item1", "Item 1")
//                            item("item2", "Item 2")
//                        }
//                    }
//                    pageHeaderTools {
//                        pageHeaderToolsItem {
//                            notificationBadge()
//                        }
//                        avatar {
//                            src("/assets/images/img_avatar.svg")
//                        }
//                    }
//                }
//                pageSidebar {
//                    verticalNavigation(router) {
//                        items {
//                            item("item1", "Item 1")
//                            item("item2", "Item 2")
//                        }
//                    }
//                }
//                pageMain {
//                    pageGroup(Sticky.TOP) {
//                        pageNavigation(limitWidth = true) {
//                            tertiaryNavigation(router) {
//                                items {
//                                    item("item1", "Item 1")
//                                    item("item2", "Item 2")
//                                }
//                            }
//                        }
//                        pageBreadcrumb(limitWidth = true) {
//                            breadcrumb<String> {
//                                items {
//                                    item("item1", "Item 1")
//                                    item("item2", "Item 2")
//                                }
//                            }
//                        }
//                        pageSection(baseClass = "light".modifier()) {
//                            textContent {
//                                title { +"Main title" }
//                                p { +"Lorem ipsum dolor sit amet." }
//                            }
//                        }
//                    }
//                    pageSection(limitWidth = true) {
//                        cardView(store) {
//                            display { item ->
//                                card(item) {
//                                    cardBody { +item }
//                                }
//                            }
//                        }
//                    }
//                }
            }
        }
    }
}
