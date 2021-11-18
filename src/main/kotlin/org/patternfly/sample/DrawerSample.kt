package org.patternfly.sample

import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.patternfly.ItemsStore
import org.patternfly.Severity.INFO
import org.patternfly.dataList
import org.patternfly.dataListItem
import org.patternfly.dataListRow
import org.patternfly.drawer
import org.patternfly.drawerAction
import org.patternfly.drawerBody
import org.patternfly.drawerBodyWithClose
import org.patternfly.drawerClose
import org.patternfly.drawerContent
import org.patternfly.drawerHead
import org.patternfly.drawerPanel
import org.patternfly.drawerSection
import org.patternfly.notification

internal class DrawerSample {

    fun drawerSetup() {
        render {
            val store = ItemsStore<String>()

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
                        store.singleSelection.asText()
                    }
                }
            }

            store.addAll(listOf("One", "Two", "Three"))
            store.singleSelection.filterNotNull().map { } handledBy drawer.expanded.expand
        }
    }

    fun expanded() {
        render {
            drawer {
                expanded.data handledBy notification(INFO) { expanded ->
                    title("Expanded state of drawer: $expanded.")
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
                            drawerAction {
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
