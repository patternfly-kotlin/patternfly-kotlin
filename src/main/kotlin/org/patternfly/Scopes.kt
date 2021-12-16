package org.patternfly

import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.keyOf
import org.patternfly.dom.Id

internal object Scopes {
    val ALERT_GROUP: Scope.Key<Boolean> = keyOf(ComponentType.AlertGroup.id)
    val CHIP_GROUP: Scope.Key<Boolean> = keyOf(ComponentType.ChipGroup.id)
    val MASTHEAD: Scope.Key<Boolean> = keyOf(ComponentType.Masthead.id)
    val PAGE_SUBNAV: Scope.Key<Boolean> = keyOf(Id.build(ComponentType.Page.id, "subnav"))
    val SIDEBAR: Scope.Key<Boolean> = keyOf(ComponentType.Sidebar.id)
    val SIDEBAR_STORE: Scope.Key<SidebarStore> = keyOf(Id.build(ComponentType.Sidebar.id, "store"))
}
