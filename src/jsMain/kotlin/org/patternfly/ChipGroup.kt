package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.action
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import org.patternfly.Modifier.plain
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

typealias ChipGroupDisplay<T> = (T) -> Chip.() -> Unit

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfChipGroup(
    store: ChipGroupStore<T> = ChipGroupStore(),
    text: String? = null,
    limit: Int = 3,
    closable: Boolean = false,
    classes: String? = null,
    content: ChipGroup<T>.() -> Unit = {}
): ChipGroup<T> = register(ChipGroup(store, text, limit, closable, classes), content)

fun <T> HtmlElements.pfChipGroup(
    store: ChipGroupStore<T> = ChipGroupStore(),
    text: String? = null,
    limit: Int = 3,
    closable: Boolean = false,
    modifier: Modifier,
    content: ChipGroup<T>.() -> Unit = {}
): ChipGroup<T> = register(ChipGroup(store, text, limit, closable, modifier.value), content)

fun <T> ChipGroup<T>.pfChips(block: ChipBuilder<T>.() -> Unit) {
    val entries = ChipBuilder<T>().apply(block).build()
    action(entries) handledBy this.store.update
}

// ------------------------------------------------------ tag

@OptIn(ExperimentalCoroutinesApi::class)
class ChipGroup<T> internal constructor(
    val store: ChipGroupStore<T>,
    text: String?,
    limit: Int,
    closable: Boolean,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>,
    Div(baseClass = classes {
        +ComponentType.ChipGroup
        +("category".modifier() `when` (text != null))
        +classes
    }) {

    private var closeButton: Button? = null
    private val expanded = object : RootStore<Boolean>(false) {
        val collapse = handle { false }
        val flip = handle { !it }
    }
    val closes: Listener<MouseEvent, HTMLButtonElement> by lazy {
        if (closeButton != null) {
            Listener(callbackFlow {
                val listener: (Event) -> Unit = {
                    offer(it.unsafeCast<MouseEvent>())
                }
                this@ChipGroup.closeButton?.domNode?.addEventListener(Events.click.name, listener)
                awaitClose { this@ChipGroup.closeButton?.domNode?.removeEventListener(Events.click.name, listener) }
            })
        } else {
            Listener(emptyFlow())
        }
    }
    var asText: AsText<T> = { it.toString() }
    var display: ChipGroupDisplay<T> = {
        {
            +this@ChipGroup.asText(it)
        }
    }

    init {
        markAs(ComponentType.ChipGroup)
        var labelId: String? = null
        if (text != null) {
            labelId = Id.unique("cgl")
            span(id = labelId, baseClass = "chip-group".component("label")) {
                aria["hidden"] = true
                +text
            }
        }
        ul(baseClass = "chip-group".component("list")) {
            attr("role", "list")
            if (labelId != null) {
                aria["labelledby"] = labelId
            }
            this@ChipGroup.store.data
                .combine(this@ChipGroup.expanded.data) { items, expanded -> Pair(items, expanded) }
                .onEach { (items, expanded) ->
                    domNode.clear()
                    val visibleItems = if (expanded) items else items.take(limit)
                    visibleItems.forEach { item ->
                        li(baseClass = "chip-group".component("list-item")) {
                            pfChip {
                                val content = this@ChipGroup.display(item)
                                content.invoke(this)
                                val id = this@ChipGroup.store.identifier(item)
                                closes.map { id } handledBy this@ChipGroup.store.remove
                            }
                        }
                    }
                    if (items.size > limit) {
                        li(baseClass = "chip-group".component("list-item")) {
                            button(baseClass = classes("chip".component(), "overflow".modifier())) {
                                span(baseClass = "chip".component("text")) {
                                    +(if (expanded) "Shoe less" else "${items.size - limit} more")
                                }
                                clicks handledBy this@ChipGroup.expanded.flip
                            }
                        }
                    } else {
                        // reset to collapsed, so that "... more" is shown next time (items.size > limit)
                        action() handledBy this@ChipGroup.expanded.collapse
                    }
                }.launchIn(MainScope())
        }
        if (closable) {
            div(baseClass = "chip-group".component("close")) {
                this@ChipGroup.closeButton = pfButton(plain) {
                    pfIcon("times-circle".fas())
                    aria["label"] = "Close chip group"
                    if (labelId != null) {
                        aria["labelledby"] = labelId
                    }
                    domNode.addEventListener(Events.click.name, { this@ChipGroup.domNode.removeFromParent() })
                }
            }
        }
        MainScope().launch {
            store.remove.distinctUntilChanged().collect {
                if (it == 0) {
                    domNode.removeFromParent()
                }
            }
        }
    }
}

// ------------------------------------------------------ store

class ChipBuilder<T> {
    private val entries: MutableList<T> = mutableListOf()

    operator fun T.unaryPlus() {
        entries.add(this)
    }

    operator fun Iterable<T>.unaryPlus() {
        entries.addAll(this)
    }

    internal fun build() = entries.toList()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ChipGroupStore<T>(internal val identifier: IdProvider<T, String> = { Id.asId(it.toString()) }) :
    RootStore<List<T>>(listOf()) {

    val add = handle<T> { items, item -> items + item }

    val remove = handleAndOffer<String, Int> { items, id ->
        val removed = items.filterNot { identifier(it) == id }
        offer(removed.size)
        removed
    }
}