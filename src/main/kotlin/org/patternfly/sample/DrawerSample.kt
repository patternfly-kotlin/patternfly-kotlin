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
import org.patternfly.drawer2
import org.patternfly.drawerAction
import org.patternfly.drawerBody
import org.patternfly.drawerBodyWithClose
import org.patternfly.drawerClose
import org.patternfly.drawerContent
import org.patternfly.drawerHead
import org.patternfly.drawerPanel
import org.patternfly.notification

internal class DrawerSample {

    fun drawerSetup() {
        render {
            drawer2 {
                primary {
                    content { +"Primary content" }
                    content { +"More content" }
                }
                detail {
                    head {
                        h2 { +"Details" }
                    }
                    content { +"Some details" }
                    content { +"More details" }
                }
            }
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
