@file:Suppress("DuplicatedCode", "SpellCheckingInspection")

package org.patternfly.showcase.component

import dev.fritz2.binding.const
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.render
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.flowOf
import org.patternfly.Align
import org.patternfly.DataListDisplay
import org.patternfly.DataListStore
import org.patternfly.DropdownStore
import org.patternfly.Id
import org.patternfly.Modifier
import org.patternfly.Modifier.primary
import org.patternfly.Modifier.secondary
import org.patternfly.Size
import org.patternfly.component
import org.patternfly.modifier
import org.patternfly.pfButton
import org.patternfly.pfContent
import org.patternfly.pfDataList
import org.patternfly.pfDataListAction
import org.patternfly.pfDataListCell
import org.patternfly.pfDataListCheck
import org.patternfly.pfDataListContent
import org.patternfly.pfDataListControl
import org.patternfly.pfDataListRow
import org.patternfly.pfDropdown
import org.patternfly.pfDropdownItem
import org.patternfly.pfDropdownItems
import org.patternfly.pfDropdownKebab
import org.patternfly.pfIcon
import org.patternfly.pfSection
import org.patternfly.pfTitle
import org.patternfly.plusAssign
import org.patternfly.showcase.Places.behaviour
import org.patternfly.util
import org.w3c.dom.HTMLElement

object DataListComponent : Iterable<Tag<HTMLElement>> {
    override fun iterator(): Iterator<Tag<HTMLElement>> = iterator {
        yield(render {
            pfSection("pb-0".util()) {
                pfContent {
                    pfTitle("Data list", size = Size.XL_3)
                    p {
                        +"A "
                        strong { +"content" }
                        +" is used to display large data sets when you need a flexible layout or need to include interactive content like charts. Related design guidelines: "
                        a {
                            href = const(behaviour("lists-and-tables"))
                            target = const("pf4")
                            +"Lists and tables"
                        }
                    }
                }
            }
        })
        yield(render {
            pfSection {
                pfContent {
                    h2 { +"Examples" }
                }
                snippet("Basic", DataListCode.BASIC) {
                    // Just a fake item data w/ a display function
                    data class DisplayData(val id: String = Id.unique(), val display: DataListDisplay<DisplayData>)

                    val data = listOf(
                        DisplayData {
                            {
                                pfDataListRow {
                                    pfDataListContent {
                                        pfDataListCell {
                                            span(id = it.id) { +"Primary content" }
                                        }
                                        pfDataListCell { +"Secondary content" }
                                    }
                                }
                            }
                        },
                        DisplayData {
                            {
                                pfDataListRow {
                                    pfDataListContent {
                                        pfDataListCell("no-fill".modifier()) {
                                            span(id = it.id) { +"Secondary content (pf-m-no-fill)" }
                                        }
                                        pfDataListCell("no-fill".modifier(), "align-right".modifier()) {
                                            +"Secondary content (pf-m-align-right pf-m-no-fill)"
                                        }
                                    }
                                }
                            }
                        }
                    )
                    val identifier: IdProvider<DisplayData, String> = { Id.asId(it.id) }
                    val store: DataListStore<DisplayData> = DataListStore(identifier)

                    pfDataList(identifier, store) {
                        display = {
                            it.display(it)
                        }
                    }
                    flowOf(data) handledBy store.update
                }
                snippet("Compact", DataListCode.COMPACT) {
                    // Just a fake item data w/ a display function
                    data class DisplayData(val id: String = Id.unique(), val display: DataListDisplay<DisplayData>)

                    val data = listOf(
                        DisplayData {
                            {
                                pfDataListRow {
                                    pfDataListContent {
                                        pfDataListCell {
                                            span(id = it.id) { +"Primary content" }
                                        }
                                        pfDataListCell { +"Secondary content" }
                                    }
                                }
                            }
                        },
                        DisplayData {
                            {
                                pfDataListRow {
                                    pfDataListContent {
                                        pfDataListCell("no-fill".modifier()) {
                                            span(id = it.id) { +"Secondary content (pf-m-no-fill)" }
                                        }
                                        pfDataListCell("no-fill".modifier(), "align-right".modifier()) {
                                            +"Secondary content (pf-m-align-right pf-m-no-fill)"
                                        }
                                    }
                                }
                            }
                        }
                    )
                    val identifier: IdProvider<DisplayData, String> = { Id.asId(it.id) }
                    val store: DataListStore<DisplayData> = DataListStore(identifier)

                    pfDataList(identifier, store) {
                        domNode.classList += "compact".modifier()
                        display = {
                            it.display(it)
                        }
                    }
                    flowOf(data) handledBy store.update
                }
                snippet("Checkboxes, actions and additional cells", DataListCode.CHECKBOXES) {
                    // Just a fake item data w/ a display function
                    data class DisplayData(val id: String = Id.unique(), val display: DataListDisplay<DisplayData>)

                    val data = listOf(
                        DisplayData {
                            {
                                pfDataListRow {
                                    pfDataListControl {
                                        pfDataListCheck {}
                                    }
                                    pfDataListContent {
                                        pfDataListCell {
                                            span(id = it.id) { +"Primary content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                                        }
                                        pfDataListCell { +"Secondary content. Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                                        pfDataListCell { +"Tertiary content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                                        pfDataListCell { +"More content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                                        pfDataListCell { +"More content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                                    }
                                    pfDataListAction {
                                        div(baseClass = "data-list".component("action")) {
                                            pfDropdownKebab(DropdownStore<String>(), Align.RIGHT) {
                                                pfDropdownItems {
                                                    pfDropdownItem("Link")
                                                    pfDropdownItem("Action")
                                                    pfDropdownItem("Disabled Link") {
                                                        disabled = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        DisplayData {
                            {
                                pfDataListRow {
                                    pfDataListControl {
                                        pfDataListCheck {}
                                    }
                                    pfDataListContent {
                                        pfDataListCell {
                                            span(id = it.id) { +"Primary content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                                        }
                                        pfDataListCell { +"Secondary content. Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                                    }
                                    pfDataListAction("hidden-on-lg".modifier()) {
                                        div(baseClass = "data-list".component("action")) {
                                            pfDropdownKebab(DropdownStore<String>(), Align.RIGHT) {
                                                pfDropdownItems {
                                                    pfDropdownItem("Link")
                                                    pfDropdownItem("Action")
                                                    pfDropdownItem("Disabled Link") {
                                                        disabled = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    pfDataListAction("hidden".modifier(), "visible-on-lg".modifier()) {
                                        pfButton(primary) { +"Primary" }
                                        pfButton(secondary) { +"Secondary" }
                                    }
                                }
                            }
                        },
                        DisplayData {
                            {
                                pfDataListRow {
                                    pfDataListControl {
                                        pfDataListCheck {}
                                    }
                                    pfDataListContent {
                                        pfDataListCell {
                                            span(id = it.id) { +"Primary content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                                        }
                                        pfDataListCell { +"Secondary content. Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                                    }
                                    pfDataListAction("hidden-on-xl".modifier()) {
                                        div(baseClass = "data-list".component("action")) {
                                            pfDropdownKebab(DropdownStore<String>(), Align.RIGHT) {
                                                pfDropdownItems {
                                                    pfDropdownItem("Link")
                                                    pfDropdownItem("Action")
                                                    pfDropdownItem("Disabled Link") {
                                                        disabled = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    pfDataListAction("hidden".modifier(), "visible-on-xl".modifier()) {
                                        pfButton(primary) { +"Primary" }
                                        pfButton(secondary) { +"Secondary" }
                                        pfButton(secondary) { +"Secondary" }
                                        pfButton(secondary) { +"Secondary" }
                                    }
                                }
                            }
                        }
                    )
                    val identifier: IdProvider<DisplayData, String> = { Id.asId(it.id) }
                    val store: DataListStore<DisplayData> = DataListStore(identifier)

                    pfDataList(identifier, store) {
                        display = {
                            it.display(it)
                        }
                    }
                    flowOf(data) handledBy store.update
                }
            }
        })
    }
}

internal object DataListCode {

    //language=kotlin
    const val BASIC: String = """fun main() {
    render {
        // Just a fake item data w/ a display function
        data class DisplayData(val id: String = Id.unique(), val display: DataListDisplay<DisplayData>)

        val data = listOf(
            DisplayData {
                {
                    pfDataListRow {
                        pfDataListContent {
                            pfDataListCell {
                                span(id = it.id) { +"Primary content" }
                            }
                            pfDataListCell { +"Secondary content" }
                        }
                    }
                }
            },
            DisplayData {
                {
                    pfDataListRow {
                        pfDataListContent {
                            pfDataListCell("no-fill".modifier()) {
                                span(id = it.id) { +"Secondary content (pf-m-no-fill)" }
                            }
                            pfDataListCell("no-fill".modifier(), "align-right".modifier()) {
                                +"Secondary content (pf-m-align-right pf-m-no-fill)"
                            }
                        }
                    }
                }
            }
        )
        val identifier: IdProvider<DisplayData, String> = { Id.asId(it.id) }
        val store: DataListStore<DisplayData> = DataListStore(identifier)

        pfDataList(identifier, store) {
            display = {
                it.display(it)
            }
        }
        flowOf(data) handledBy store.update
    }
}
"""

    //language=kotlin
    const val COMPACT: String = """fun main() {
    render {
        // Just a fake item data w/ a display function
        data class DisplayData(val id: String = Id.unique(), val display: DataListDisplay<DisplayData>)

        val data = listOf(
            DisplayData {
                {
                    pfDataListRow {
                        pfDataListContent {
                            pfDataListCell {
                                span(id = it.id) { +"Primary content" }
                            }
                            pfDataListCell { +"Secondary content" }
                        }
                    }
                }
            },
            DisplayData {
                {
                    pfDataListRow {
                        pfDataListContent {
                            pfDataListCell("no-fill".modifier()) {
                                span(id = it.id) { +"Secondary content (pf-m-no-fill)" }
                            }
                            pfDataListCell("no-fill".modifier(), "align-right".modifier()) {
                                +"Secondary content (pf-m-align-right pf-m-no-fill)"
                            }
                        }
                    }
                }
            }
        )
        val identifier: IdProvider<DisplayData, String> = { Id.asId(it.id) }
        val store: DataListStore<DisplayData> = DataListStore(identifier)

        pfDataList(identifier, store) {
            domNode.classList += "compact".modifier()
            display = {
                it.display(it)
            }
        }
        flowOf(data) handledBy store.update
    }
}
"""

    //language=kotlin
    const val CHECKBOXES: String = """fun main() {
    render {
        // Just a fake item data w/ a display function
        data class DisplayData(val id: String = Id.unique(), val display: DataListDisplay<DisplayData>)

        val data = listOf(
            DisplayData {
                {
                    pfDataListRow {
                        pfDataListControl {
                            pfDataListCheck {}
                        }
                        pfDataListContent {
                            pfDataListCell {
                                span(id = it.id) { +"Primary content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                            }
                            pfDataListCell { +"Secondary content. Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                            pfDataListCell { +"Tertiary content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                            pfDataListCell { +"More content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                            pfDataListCell { +"More content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                        }
                        pfDataListAction {
                            div(baseClass = "data-list".component("action")) {
                                pfDropdownKebab(DropdownStore<String>(), Align.RIGHT) {
                                    pfDropdownItems {
                                        pfDropdownItem("Link")
                                        pfDropdownItem("Action")
                                        pfDropdownItem("Disabled Link") {
                                            disabled = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            DisplayData {
                {
                    pfDataListRow {
                        pfDataListControl {
                            pfDataListCheck {}
                        }
                        pfDataListContent {
                            pfDataListCell {
                                span(id = it.id) { +"Primary content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                            }
                            pfDataListCell { +"Secondary content. Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                        }
                        pfDataListAction("hidden-on-lg".modifier()) {
                            div(baseClass = "data-list".component("action")) {
                                pfDropdownKebab(DropdownStore<String>(), Align.RIGHT) {
                                    pfDropdownItems {
                                        pfDropdownItem("Link")
                                        pfDropdownItem("Action")
                                        pfDropdownItem("Disabled Link") {
                                            disabled = true
                                        }
                                    }
                                }
                            }
                        }
                        pfDataListAction("hidden".modifier(), "visible-on-lg".modifier()) {
                            pfButton(primary) { +"Primary" }
                            pfButton(secondary) { +"Secondary" }
                        }
                    }
                }
            },
            DisplayData {
                {
                    pfDataListRow {
                        pfDataListControl {
                            pfDataListCheck {}
                        }
                        pfDataListContent {
                            pfDataListCell {
                                span(id = it.id) { +"Primary content Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                            }
                            pfDataListCell { +"Secondary content. Dolor sit amet, consectetur adipisicing elit, sed do eiusmod." }
                        }
                        pfDataListAction("hidden-on-xl".modifier()) {
                            div(baseClass = "data-list".component("action")) {
                                pfDropdownKebab(DropdownStore<String>(), Align.RIGHT) {
                                    pfDropdownItems {
                                        pfDropdownItem("Link")
                                        pfDropdownItem("Action")
                                        pfDropdownItem("Disabled Link") {
                                            disabled = true
                                        }
                                    }
                                }
                            }
                        }
                        pfDataListAction("hidden".modifier(), "visible-on-xl".modifier()) {
                            pfButton(primary) { +"Primary" }
                            pfButton(secondary) { +"Secondary" }
                            pfButton(secondary) { +"Secondary" }
                            pfButton(secondary) { +"Secondary" }
                        }
                    }
                }
            }
        )
        val identifier: IdProvider<DisplayData, String> = { Id.asId(it.id) }
        val store: DataListStore<DisplayData> = DataListStore(identifier)

        pfDataList(identifier, store) {
            display = {
                it.display(it)
            }
        }
        flowOf(data) handledBy store.update
    }
}
"""
}
