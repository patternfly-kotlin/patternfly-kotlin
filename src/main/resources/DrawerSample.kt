package org.patternfly

import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.map

internal interface DrawerSample {

    fun drawerSetup() {
        render {
            val store = ItemStore<String>()

            val drawer = drawer {
                drawerSection {
                    +"Primary detail demo"
                }
                drawerContent {
                    drawerBody {
                        dataList(store, selectableRows = true) {
                            display { item ->
                                dataListItem(item) {
                                    dataListRow { +item }
                                }
                            }
                        }
                    }
                }
                drawerPanel {
                    drawerBodyWithClose {
                        h2 { +"Details of selected item" }
                    }
                    drawerBody {
                        store.selectItem.asText()
                    }
                }
            }

            store.addAll(listOf("One", "Two", "Three"))
            store.selectItem.map { } handledBy drawer.ces.expand
        }
    }

    fun ces() {
        render {
            drawer {
                ces.data handledBy Notification.add { expanded ->
                    info("Expanded state of drawer: $expanded.")
                }
                drawerContent {
                    drawerBody { +"Drawer content" }
                }
                drawerPanel {
                    drawerBodyWithClose {
                        +"Drawer panel"
                    }
                }
            }
        }
    }

    fun drawerContents() {
        render {
            drawer {
                drawerContent {
                    drawerBody { +"Actual" }
                    drawerBody { +"content" }
                    drawerBody { +"goes here" }
                }
            }
        }
    }

    fun drawerPanels() {
        render {
            drawer {
                drawerPanel {
                    drawerBodyWithClose { +"Title" }
                    drawerBody { +"additional" }
                    drawerBody { +"content" }
                }
            }
            // is the same as
            drawer {
                drawerPanel {
                    drawerBody {
                        drawerHead {
                            +"Title"
                            drawerActions {
                                drawerClose()
                            }
                        }
                    }
                    drawerBody { +"additional" }
                    drawerBody { +"content" }
                }
            }
        }
    }
}
