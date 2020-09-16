package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.action
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithText
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfOptionsMenu(
    store: OptionStore<T> = OptionStore(),
    align: Align? = null,
    up: Boolean = false,
    classes: String? = null,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, align, up, classes), content)

fun <T> OptionsMenu<T>.pfOptionsMenuItems(
    classes: String? = null,
    block: ItemsBuilder<T>.() -> Unit = {}
): OptionsMenuEntries<HTMLUListElement, T> {
    val element = this.register(
        OptionsMenuEntries<HTMLUListElement, T>(this, "ul", classes), {})
    val items = ItemsBuilder<T>().apply(block).build()
    action(items) handledBy this.store.update
    return element
}

fun <T> OptionsMenu<T>.pfOptionsMenuGroups(
    classes: String? = null,
    block: GroupsBuilder<T>.() -> Unit = {}
): OptionsMenuEntries<HTMLDivElement, T> {
    val element = this.register(
        OptionsMenuEntries<HTMLDivElement, T>(this, "div", classes), {})
    val groups = GroupsBuilder<T>().apply(block).build()
    action(groups) handledBy this.store.update
    return element
}

// ------------------------------------------------------ tag

class OptionsMenu<T> internal constructor(
    val store: OptionStore<T>,
    internal val optionsMenuAlign: Align?,
    up: Boolean,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +ComponentType.OptionsMenu
    +optionsMenuAlign?.modifier
    +("top".modifier() `when` up)
    +classes
}) {

    internal var toggle: OptionsMenuToggleBase<out HTMLElement, T>? = null

    val ces = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("options-menu".component("menu-item")))
    }

    var display: ComponentDisplay<Button, Item<T>> = {
        {
            +it.item.toString()
        }
    }

    init {
        markAs(ComponentType.OptionsMenu)
        classMap = ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
    }
}

sealed class OptionsMenuToggleBase<E : HTMLElement, T>(
    private val optionsMenu: OptionsMenu<T>,
    tagName: String,
    id: String? = null,
    baseClass: String? = null,
) : Tag<E>(tagName, id, baseClass), WithText<E> {

    internal var toggleId: String? = null
    abstract var disabled: Flow<Boolean>

    internal fun initToggle(toggleTag: Tag<HTMLElement>) {
        with(toggleTag) {
            aria["haspopup"] = "listbox"
            clicks handledBy this@OptionsMenuToggleBase.optionsMenu.ces.toggle
            this@OptionsMenuToggleBase.toggleId = id
            this@OptionsMenuToggleBase.optionsMenu.ces.data.map { it.toString() }.bindAttr("aria-expanded")
        }
        optionsMenu.toggle = this
    }
}

class OptionsMenuEntries<E : HTMLElement, T> internal constructor(
    private val optionsMenu: OptionsMenu<T>,
    tagName: String,
    classes: String?
) : Tag<E>(tagName = tagName, baseClass = classes {
    +"dropdown".component("menu")
    +optionsMenu.optionsMenuAlign?.modifier
    +classes
}) {
    init {
        attr("role", "menu")
        optionsMenu.toggle?.toggleId?.let {
            aria["labelledby"] = it
        }
        optionsMenu.ces.data.map { !it }.bindAttr("hidden")
        optionsMenu.store.data.each().render { entry ->
            when (entry) {
                is Item<T> -> {
                    li(content = itemContent(entry))
                }
                is Group<T> -> {
                    section(baseClass = "dropdown".component("group")) {
                        entry.title?.let {
                            h1(baseClass = "dropdown".component("group", "title")) { +it }
                        }
                        ul {
                            entry.items.forEach { groupEntry ->
                                when (groupEntry) {
                                    is Item<T> -> {
                                        li(content = this@OptionsMenuEntries.itemContent(groupEntry))
                                    }
                                    is Separator<T> -> {
                                        pfDivider(DividerVariant.LI)
                                    }
                                    else -> {
                                        console.warn("Nested groups are not supported for ${this@OptionsMenuEntries.optionsMenu.domNode.debug()}")
                                    }
                                }
                            }
                        }
                    }
                }
                is Separator<T> -> {
                    if (domNode.tagName.toLowerCase() == "ul") {
                        pfDivider(DividerVariant.LI)
                    } else {
                        pfDivider(DividerVariant.DIV)
                    }
                }
            }
        }.bind()
    }

    private fun itemContent(item: Item<T>): Li.() -> Unit = {
        attr("role", "menuitem")
        button(baseClass = "options-menu".component("menu-item")) {
            attr("tabindex", "-1")
            if (item.disabled) {
                aria["disabled"] = true
                attr("disabled", "true")
                domNode.classList += "disabled".modifier()
            }
            clicks.map { item } handledBy this@OptionsMenuEntries.optionsMenu.store.toggle
            this@OptionsMenuEntries.optionsMenu.display(item).invoke(this)
            if (item.selected) {
                span(baseClass = "options-menu".component("menu-item", "icon")) {
                    pfIcon("check".fas())
                }
            }
        }
    }
}

// ------------------------------------------------------ store

class OptionStore<T> : RootStore<List<Entry<T>>>(listOf()) {

    internal val toggle = handle<Item<T>> { items, item ->
        items.map { entry ->
            when (entry) {
                is Item<T> -> {
                    if (entry.item == item.item) {
                        if (entry.selected) {
                            entry
                        } else {
                            entry.copy(selected = true)
                        }
                    } else {
                        entry.copy(selected = false)
                    }
                }
                is Group<T> -> {
                    if (entry.id == item.group?.id) {
                        val groupItems = entry.items.map { groupEntry ->
                            when (groupEntry) {
                                is Item<T> -> {
                                    if (groupEntry.item == item.item) {
                                        if (groupEntry.selected) {
                                            groupEntry
                                        } else {
                                            groupEntry.copy(selected = true)
                                        }
                                    } else {
                                        groupEntry.copy(selected = false)
                                    }
                                }
                                else -> {
                                    groupEntry
                                }
                            }
                        }
                        entry.copy(items = groupItems)
                    } else {
                        entry
                    }
                }
                else -> {
                    entry
                }
            }
        }
    }

    val items: Flow<List<T>> = data.map {
        it.filterIsInstance<Item<T>>()
    }.map { it.map { item -> item.item } }
    val groups: Flow<List<Group<T>>> = data.map { it.filterIsInstance<Group<T>>() }
}
