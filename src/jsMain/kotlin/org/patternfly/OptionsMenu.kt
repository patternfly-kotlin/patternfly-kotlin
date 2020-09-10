package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.action
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.TextElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfOptionsMenu(
    store: OptionStore<T> = OptionStore(),
    text: String,
    grouped: Boolean = false,
    align: Align? = null,
    up: Boolean = false,
    classes: String? = null,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, Either.Left(text), grouped, align, up, classes), content)

fun <T> HtmlElements.pfOptionsMenu(
    store: OptionStore<T> = OptionStore(),
    text: String,
    grouped: Boolean = false,
    align: Align? = null,
    up: Boolean = false,
    modifier: Modifier,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, Either.Left(text), grouped, align, up, modifier.value), content)

fun <T> HtmlElements.pfOptionsMenu(
    store: OptionStore<T> = OptionStore(),
    icon: Icon,
    grouped: Boolean = false,
    align: Align? = null,
    up: Boolean = false,
    classes: String? = null,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, Either.Right(icon), grouped, align, up, classes), content)

fun <T> HtmlElements.pfOptionsMenu(
    store: OptionStore<T> = OptionStore(),
    icon: Icon,
    grouped: Boolean = false,
    align: Align? = null,
    up: Boolean = false,
    modifier: Modifier,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, Either.Right(icon), grouped, align, up, modifier.value), content)

fun <T> OptionsMenu<T>.pfEntries(block: EntriesBuilder<T>.() -> Unit) {
    val entries = EntriesBuilder<T>().apply(block).build()
    action(entries) handledBy this.store.update
}

// ------------------------------------------------------ tag

class OptionsMenu<T> internal constructor(
    val store: OptionStore<T>,
    private val textOrIcon: Either<String, Icon>,
    grouped: Boolean,
    align: Align?,
    up: Boolean,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +ComponentType.OptionsMenu
    +align?.modifier
    +("top".modifier() `when` up)
    +classes
}) {

    private val button: Button
    val ces = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("options-menu".component("menu-item")))
    }
    var asText: AsText<T> = { it.toString() }
    var display: ComponentDisplay<Button, T> = {
        {
            +this@OptionsMenu.asText(it)
        }
    }
    var disabled: Flow<Boolean>
        get() = button.disabled
        set(value) {
            button.disabled = value
        }

    init {
        markAs(ComponentType.OptionsMenu)
        classMap = ces.data.map { expanded -> mapOf(Modifier.expanded.value to expanded) }
        val buttonId = Id.unique(ComponentType.OptionsMenu.id, "btn")
        button = button(id = id, baseClass = "options-menu".component("toggle")) {
            aria["label"] = "Options menu"
            aria["haspopup"] = "listbox"
            clicks handledBy this@OptionsMenu.ces.toggle
            this@OptionsMenu.ces.data.map { it.toString() }.bindAttr("aria-expanded")
            when (this@OptionsMenu.textOrIcon) {
                is Either.Left -> {
                    span(baseClass = "options-menu".component("toggle", "text")) {
                        +this@OptionsMenu.textOrIcon.value
                    }
                    span(baseClass = "options-menu".component("toggle", "icon")) {
                        pfIcon("caret-down".fas())
                    }
                }
                is Either.Right -> {
                    domNode.classList += Modifier.plain
                    register(this@OptionsMenu.textOrIcon.value) {}
                }
            }
        }

        val baseClass = classes {
            +"options-menu".component("menu")
            +align?.modifier
        }
        val menuContent: TextElement.() -> Unit = {
            aria["labelledby"] = buttonId
            attr("role", "menu")
            this@OptionsMenu.ces.data.map { !it }.bindAttr("hidden")
            this@OptionsMenu.store.data.each().render { entry ->
                when (entry) {
                    is Item<T> -> {
                        li(content = this@OptionsMenu.itemContent(entry))
                    }
                    is Group<T> -> {
                        section(baseClass = "options-menu".component("group")) {
                            entry.title?.let {
                                h1(baseClass = "options-menu".component("group", "title")) { +it }
                            }
                            ul {
                                entry.items.forEach { groupEntry ->
                                    when (groupEntry) {
                                        is Item<T> -> {
                                            li(content = this@OptionsMenu.itemContent(groupEntry))
                                        }
                                        is Separator<T> -> {
                                            pfDivider(DividerVariant.LI)
                                        }
                                        else -> {
                                            console.warn("Nested groups are not supported for ${this@OptionsMenu.domNode.debug()}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is Separator<T> -> {
                        if (grouped) {
                            pfDivider(DividerVariant.DIV)
                        } else {
                            pfDivider(DividerVariant.LI)
                        }
                    }
                }
            }.bind()
        }
        if (grouped) {
            register(TextElement("div", baseClass = baseClass), menuContent)
        } else {
            register(TextElement("ul", baseClass = baseClass), menuContent)
        }
    }

    private fun itemContent(entry: Item<T>): Li.() -> Unit = {
        attr("role", "menuitem")
        button(baseClass = "options-menu".component("menu-item")) {
            attr("tabindex", "-1")
            if (entry.disabled) {
                aria["disabled"] = true
                attr("disabled", "true")
                domNode.classList += Modifier.disabled
            }

            this@OptionsMenu.display(entry.item).invoke(this)
            clicks.map { entry } handledBy this@OptionsMenu.store.toggle

            if (entry.selected) {
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
