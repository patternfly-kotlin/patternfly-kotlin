package org.patternfly

import dev.fritz2.binding.OfferingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.render
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.patternfly.DividerVariant.DIV
import org.w3c.dom.HTMLDivElement

typealias DropdownDisplay<T> = (T) -> Button.() -> Unit

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDropdown(
    store: DropdownStore<T>,
    text: String,
    align: Align? = null,
    classes: String? = null,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, Either.Left(text), align, classes), content)

fun <T> HtmlElements.pfDropdown(
    store: DropdownStore<T>,
    text: String,
    align: Align? = null,
    modifier: Modifier,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, Either.Left(text), align, modifier.value), content)

fun <T> HtmlElements.pfDropdownIcon(
    store: DropdownStore<T>,
    icon: Icon,
    align: Align? = null,
    classes: String? = null,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, Either.Right(icon), align, classes), content)

fun <T> HtmlElements.pfDropdownIcon(
    store: DropdownStore<T>,
    icon: Icon,
    align: Align? = null,
    modifier: Modifier,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, Either.Right(icon), align, modifier.value), content)

fun <T> HtmlElements.pfDropdownKebab(
    store: DropdownStore<T>,
    align: Align? = null,
    classes: String? = null,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> =
    register(Dropdown(store, Either.Right(pfIcon("ellipsis-v".fas())), align, classes), content)

fun <T> HtmlElements.pfDropdownKebab(
    store: DropdownStore<T>,
    align: Align? = null,
    modifier: Modifier,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> =
    register(Dropdown(store, Either.Right(pfIcon("ellipsis-v".fas())), align, modifier.value), content)

fun <T> Dropdown<T>.pfDropdownItems(block: DropdownEntryBuilder<T>.() -> Unit) {
    val entries = DropdownEntryBuilder<T>().apply(block).build()
    flowOf(entries) handledBy this.store.update
}

fun <T> pfDropdownItems(block: DropdownEntryBuilder<T>.() -> Unit): List<DropdownEntry<T>> =
    DropdownEntryBuilder<T>().apply(block).build()

fun <T> DropdownEntryBuilder<T>.pfDropdownItem(item: T, block: DropdownItemBuilder<T>.() -> Unit = {}) {
    entries.add(DropdownItemBuilder(item).apply(block).build())
}

fun <T> DropdownEntryBuilder<T>.pfDropdownSeparator() {
    entries.add(DropdownSeparator())
}

fun <T> DropdownEntryBuilder<T>.pfDropdownGroup(title: String, block: DropdownGroupBuilder<T>.() -> Unit) {
    entries.add(DropdownGroupBuilder<T>(title).apply(block).build())
}

fun <T> DropdownGroupBuilder<T>.pfDropdownItem(item: T, block: DropdownItemBuilder<T>.() -> Unit = {}) {
    entries.add(DropdownItemBuilder(item).apply(block).build())
}

fun <T> DropdownGroupBuilder<T>.pfDropdownSeparator() {
    entries.add(DropdownSeparator())
}

// ------------------------------------------------------ tag

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class Dropdown<T> internal constructor(
    val store: DropdownStore<T>,
    private val textOrIcon: Either<String, Icon>,
    align: Align?,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +ComponentType.Dropdown
    +align?.modifier
    +classes
}) {

    val ces = CollapseExpandStore<T>(domNode)
    var asText: AsText<T> = { it.toString() }
    var display: DropdownDisplay<T> = {
        {
            +this@Dropdown.asText.invoke(it)
        }
    }

    init {
        markAs(ComponentType.Dropdown)
        classMap = ces.data.map { expanded -> mapOf(Modifier.expanded.value to expanded) }
        val buttonId = Id.unique("dd-button")
        button("dropdown".component("toggle"), buttonId) {
            aria["haspopup"] = true
            this@Dropdown.ces.data.map { it.toString() }.bindAttr("aria-expanded")
            clicks handledBy this@Dropdown.ces.expand
            when (this@Dropdown.textOrIcon) {
                is Either.Left -> {
                    span("dropdown".component("toggle", "text")) {
                        +this@Dropdown.textOrIcon.value
                    }
                    span("dropdown".component("toggle", "icon")) {
                        pfIcon("caret-down".fas())
                    }
                }
                is Either.Right -> {
                    domNode.classList += Modifier.plain
                    register(this@Dropdown.textOrIcon.value) {}
                }
            }
        }
        ul(baseClass = buildString {
            append("dropdown".component("menu"))
            align?.let {
                append(" ").append(it.modifier)
            }
        }) {
            aria["labelledby"] = buttonId
            attr("role", "menu")
            this@Dropdown.ces.data.map { !it }.bindAttr("hidden")
            this@Dropdown.store.data.each().map { item ->
                render {
                    when (item) {
                        is DropdownItem<T> -> {
                            li {
                                attr("role", "menuitem")
                                button("dropdown".component("menu-item")) {
                                    attr("tabindex", "-1")
                                    if (item.disabled) {
                                        attr("disabled", "true")
                                        domNode.classList += Modifier.disabled
                                    }
                                    if (item.selected) {
                                        domNode.autofocus = true
                                    }
                                    val content = this@Dropdown.display(item.item)
                                    content.invoke(this)

                                    this@Dropdown.store.offerItem handledBy this@Dropdown.ces.collapse
                                    clicks.map { item.item } handledBy this@Dropdown.store.offerItem
                                }
                            }
                        }
                        is DropdownGroup<T> -> {
                            li(baseClass = "display-none".util()) {
                                !"Dropdown groups not yet implemented"
                            }
                        }
                        is DropdownSeparator<T> -> {
                            li {
                                attr("role", "separator")
                                pfDivider(DIV)
                            }
                        }
                    }
                }
            }.bind()
        }
    }
}

// ------------------------------------------------------ store

sealed class DropdownEntry<T>

data class DropdownGroup<T>(val title: String, val items: List<DropdownEntry<T>>) : DropdownEntry<T>()

data class DropdownItem<T>(val item: T, val disabled: Boolean = false, val selected: Boolean = false) :
    DropdownEntry<T>()

class DropdownSeparator<T> : DropdownEntry<T>()

class DropdownEntryBuilder<T> {
    val entries: MutableList<DropdownEntry<T>> = mutableListOf()

    internal fun build(): List<DropdownEntry<T>> = entries
}

class DropdownGroupBuilder<T>(private val title: String) {
    val entries: MutableList<DropdownEntry<T>> = mutableListOf()

    internal fun build(): DropdownGroup<T> = DropdownGroup(title, entries)
}

class DropdownItemBuilder<T>(private val item: T) {
    var disabled: Boolean = false
    var selected: Boolean = false

    internal fun build(): DropdownItem<T> = DropdownItem(item, disabled, selected)
}

class DropdownStore<T> : RootStore<List<DropdownEntry<T>>>(listOf()) {
    internal val offerItem: OfferingHandler<T, T> = handleAndOffer { items, item ->
        offer(item)
        items
    }
    val clicks: Flow<T> = offerItem.map { it }
}
