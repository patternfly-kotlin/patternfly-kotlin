package org.patternfly

import dev.fritz2.binding.OfferingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.action
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

typealias OptionDisplay<T> = (T) -> Button.() -> Unit

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfOptionsMenu(
    store: OptionStore<T> = OptionStore(),
    text: String,
    align: Align? = null,
    classes: String? = null,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, Either.Left(text), align, classes), content)

fun <T> HtmlElements.pfOptionsMenu(
    store: OptionStore<T> = OptionStore(),
    text: String,
    align: Align? = null,
    modifier: Modifier,
    content: OptionsMenu<T>.() -> Unit = {}
): OptionsMenu<T> = register(OptionsMenu(store, Either.Left(text), align, modifier.value), content)

fun <T> OptionsMenu<T>.pfEntries(block: EntryBuilder<T>.() -> Unit) {
    val entries = EntryBuilder<T>().apply(block).build()
    action(entries) handledBy this.store.update
}

// ------------------------------------------------------ tag

class OptionsMenu<T> internal constructor(
    val store: OptionStore<T>,
    private val textOrIcon: Either<String, Icon>,
    align: Align?,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +ComponentType.OptionsMenu
    +align?.modifier
    +classes
}) {

    val ces = CollapseExpandStore(domNode)
    var asText: AsText<T> = { it.toString() }
    var display: OptionDisplay<T> = {
        {
            +this@OptionsMenu.asText.invoke(it)
        }
    }

    init {
        markAs(ComponentType.OptionsMenu)
        classMap = ces.data.map { expanded -> mapOf(Modifier.expanded.value to expanded) }
        val buttonId = Id.unique(ComponentType.Dropdown.id, "btn")
        button(id = id, baseClass = "options-menu".component("toggle")) {
            aria["label"] = "Options menu"
            aria["haspopup"] = "listbox"
            clicks handledBy this@OptionsMenu.ces.expand
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
            ul(baseClass = classes {
                +"dropdown".component("menu")
                +align?.modifier
            }) {
                aria["labelledby"] = buttonId
                attr("role", "menu")
                this@OptionsMenu.ces.data.map { !it }.bindAttr("hidden")
                this@OptionsMenu.store.data.each().render { entry ->
                    when (entry) {
                        is Item<T> -> {
                            li {
                                attr("role", "menuitem")
                                button(baseClass = "options-menu".component("menu-item")) {
                                    attr("tabindex", "-1")
                                    if (entry.disabled) {
                                        attr("disabled", "true")
                                        domNode.classList += Modifier.disabled
                                    }
                                    val content = this@OptionsMenu.display(entry.item)
                                    content.invoke(this)
                                    clicks.map { entry.item } handledBy this@OptionsMenu.store.offerItem
                                }
                            }
                        }
                        is Group<T> -> {
                            li(baseClass = "display-none".util()) {
                                !"OptionsMenu groups not yet implemented"
                            }
                        }
                        is Separator<T> -> {
                            li {
                                attr("role", "separator")
                                pfDivider(DividerVariant.DIV)
                            }
                        }
                    }
                }.bind()
            }
        }
    }
}

// ------------------------------------------------------ store

class OptionStore<T> : RootStore<List<Entry<T>>>(listOf()) {
    internal val offerItem: OfferingHandler<T, T> = handleAndOffer { items, item ->
        offer(item)
        items
    }
    val clicks: Flow<T> = offerItem.map { it }
}

