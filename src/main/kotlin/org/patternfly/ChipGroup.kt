package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.removeFromParent
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.dom.clear
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

public fun <T> RenderContext.pfChipGroup(
    store: ChipGroupStore<T> = ChipGroupStore(),
    text: String? = null,
    limit: Int = 3,
    closable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: ChipGroup<T>.() -> Unit = {}
): ChipGroup<T> = register(ChipGroup(store, text, limit, closable, id = id, baseClass = baseClass, job), content)

public fun <T> ChipGroup<T>.pfChips(block: ChipBuilder<T>.() -> Unit) {
    store.update(ChipBuilder<T>().apply(block).build())
}

// ------------------------------------------------------ tag

public class ChipGroup<T> internal constructor(
    public val store: ChipGroupStore<T>,
    text: String?,
    limit: Int,
    closable: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes {
        +ComponentType.ChipGroup
        +("category".modifier() `when` (text != null))
        +baseClass
    }, job) {

    private var closeButton: PushButton? = null
    private val expanded = CollapseExpandStore()
    public val closes: Listener<MouseEvent, HTMLButtonElement> by lazy { subscribe(closeButton, Events.click) }

    public var display: (T) -> Chip = {
        pfChip {
            +it.toString()
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
                            val chip = this@ChipGroup.display(item)
                            register(chip) {
                                val chipId = this@ChipGroup.store.identifier(item)
                                it.closes.map { chipId } handledBy this@ChipGroup.store.remove
                            }
                        }
                    }
                    if (items.size > limit) {
                        li(baseClass = "chip-group".component("list-item")) {
                            button(baseClass = classes("chip".component(), "overflow".modifier())) {
                                span(baseClass = "chip".component("text")) {
                                    +(if (expanded) "Shoe less" else "${items.size - limit} more")
                                }
                                clicks handledBy this@ChipGroup.expanded.toggle
                            }
                        }
                    } else {
                        // reset to collapsed, so that "... more" is shown next time (items.size > limit)
                        this@ChipGroup.expanded.collapse(Unit)
                    }
                }.launchIn(MainScope() + job)
        }
        if (closable) {
            div(baseClass = "chip-group".component("close")) {
                this@ChipGroup.closeButton = button("plain".modifier()) {
                    icon("times-circle".fas())
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

public class ChipGroupStore<T>(internal val identifier: IdProvider<T, String> = { Id.build(it.toString()) }) :
    RootStore<List<T>>(listOf()) {

    public val add: SimpleHandler<T> = handle { items, item -> items + item }

    public val remove: EmittingHandler<String, Int> = handleAndEmit { items, id ->
        val removed = items.filterNot { identifier(it) == id }
        emit(removed.size)
        removed
    }
}

// ------------------------------------------------------ builder

public class ChipBuilder<T> {
    private val entries: MutableList<T> = mutableListOf()

    public operator fun T.unaryPlus() {
        entries.add(this)
    }

    public operator fun Iterable<T>.unaryPlus() {
        entries.addAll(this)
    }

    internal fun build() = entries.toList()
}
