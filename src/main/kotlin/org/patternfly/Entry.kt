package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.debug
import org.patternfly.dom.displayNone
import org.patternfly.dom.matches
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ component

@Suppress("TooManyFunctions")
public abstract class EntriesComponent<G : Toggle, I : Item<I>> internal constructor(
    private val componentType: ComponentType,
    private val componentBaseClass: String,
    private val grouped: Boolean,
    private val align: Align?,
    private val up: Boolean,
    private val itemProvider: (String, String?) -> I
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var storeItems: Boolean = false
    private val itemStore: EntryStore = EntryStore()
    private val headEntries: MutableList<Entry> = mutableListOf()
    private val tailEntries: MutableList<Entry> = mutableListOf()
    private lateinit var root: Div
    internal abstract val toggle: G

    internal val expandedStore: ExpandedStore = ExpandedStore { target ->
        !root.domNode.contains(target) && !target.matches(By.classname(componentBaseClass.component("menu-item")))
    }

    public val excos: Flow<Boolean>
        get() = expandedStore.data.drop(1)

    /**
     * Disables or enables the dropdown.
     */
    public fun disabled(value: Boolean) {
        toggle.disabled = flowOf(value)
    }

    /**
     * Disables or enables the dropdown based on the values from the flow.
     */
    public fun disabled(value: Flow<Boolean>) {
        toggle.disabled = value
    }

    /**
     * Sets the toggle for this dropdown.
     */
    public fun toggle(context: G.() -> Unit) {
        toggle.apply(context)
    }

    /**
     * Adds a group to this dropdown.
     */
    public fun group(
        title: String? = null,
        id: String = entryId(componentType, "grp"),
        context: Group<I>.() -> Unit
    ) {
        (if (storeItems) tailEntries else headEntries).add(
            Group(componentType, id, title, emptyList(), itemProvider).apply(context)
        )
    }

    /**
     * Adds an item to this dropdown.
     */
    public fun item(
        title: String,
        id: String = entryId(componentType, "itm"),
        context: I.() -> Unit = {}
    ) {
        (if (storeItems) tailEntries else headEntries).add(
            itemProvider(id, title).apply(context)
        )
    }

    /**
     * Adds a separator.
     */
    public fun separator() {
        (if (storeItems) tailEntries else headEntries).add(Separator(componentType))
    }

    /**
     * Adds the items from the specified store.
     */
    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: Entries<I, T>.(T) -> Entry
    ) {
        items(values.data, idProvider, display)
    }

    /**
     * Adds the items from the specified flow.
     */
    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: Entries<I, T>.(T) -> Entry
    ) {
        (MainScope() + NotificationStore.job).launch {
            values.collect { values ->
                itemStore.update(
                    values.map { value ->
                        Entries(componentType, idProvider, itemProvider).run {
                            display.invoke(this, value)
                        }
                    }
                )
            }
        }
        storeItems = true
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            root = div(
                baseClass = classes {
                    +componentType
                    +("top".modifier() `when` up)
                    +baseClass
                },
                id = id
            ) {
                markAs(componentType)
                applyElement(this)
                applyEvents(this)

                with(expandedStore) { toggleExpanded() }
                toggle.render(this)
                renderEntries(this)
            }
        }
    }

    private fun renderEntries(context: RenderContext) {
        val groups = grouped || (headEntries + tailEntries).filterIsInstance<Group<I>>().isNotEmpty()
        with(context) {
            val classes = classes {
                +componentBaseClass.component("menu")
                +align?.modifier
            }
            val menu = if (groups) {
                div(baseClass = classes) {
                    attr("hidden", true)
                }
            } else {
                ul(baseClass = classes) {
                    attr("hidden", true)
                }
            }
            with(menu) {
                attr("role", "menu")
                aria["labelledby"] = toggle.id
                with(expandedStore) { hideIfCollapsed() }

                itemStore.data.map { entries ->
                    headEntries + entries + tailEntries
                }.renderEach(into = this, idProvider = { it.id }) { entry ->
                    renderEntry(this, entry, groups, 0)
                }
            }
        }
    }

    private fun renderEntry(context: RenderContext, entry: Entry, groups: Boolean, depth: Int): RenderContext =
        with(context) {
            when (entry) {
                is Group<*> -> {
                    section(baseClass = componentBaseClass.component("group")) {
                        if (depth > 0) {
                            val message = "Nested groups are not supported in ${componentType.name}"
                            !message
                            console.warn("$message: ${root.domNode.debug()}")
                        } else {
                            if (entry.hasTitle) {
                                h1(baseClass = componentBaseClass.component("group", "title")) {
                                    entry.applyTitle(this)
                                }
                            }
                            ul {
                                attr("role", "none")
                                entry.entries.forEach { renderEntry(this, it, groups, depth + 1) }
                            }
                        }
                    }
                }
                is Separator -> {
                    if (groups && depth == 0) {
                        divider(DividerVariant.HR)
                    } else {
                        divider(DividerVariant.LI)
                    }
                }
                else -> {
                    renderItem(context, entry)
                }
            }
        }

    internal abstract fun renderItem(context: RenderContext, entry: Entry): RenderContext

    internal fun unsupportedItem(context: RenderContext, element: HTMLElement, entry: Entry) {
        with(element) {
            hidden = true
            displayNone = true
        }
        with(context) {
            val message = "Unsupported entry $entry"
            console.log(message)
            !message
        }
    }
}

// ------------------------------------------------------ entries & store

/**
 * DSL scope class to create [Group]s and [Item]s.
 */
public class Entries<I : Item<I>, T> internal constructor(
    private val componentType: ComponentType,
    override val idProvider: IdProvider<T, String>,
    private val itemProvider: (String, String?) -> I,
) : WithIdProvider<T> {

    public fun group(
        title: String? = null,
        id: String = entryId(componentType, "grp"),
        context: Group<I>.() -> Unit
    ): Group<I> = Group(componentType, id, title, emptyList(), itemProvider).apply(context)

    public fun item(
        title: String? = null,
        id: String = entryId(componentType, "itm"),
        context: I.() -> Unit = {}
    ): I = itemProvider(id, title).apply(context)

    public fun separator(): Separator = Separator(componentType)
}

/**
 * Base class for groups and items.
 */
public sealed class Entry(internal val id: String)

/**
 * A group with a title, an id and nested items.
 *
 * Please note that nested groups are *not* supported!
 */
public class Group<I : Item<I>> internal constructor(
    private val componentType: ComponentType,
    id: String,
    title: String?,
    initialEntries: List<Entry>,
    private val itemProvider: (String, String?) -> I
) : Entry(id), WithTitle by TitleMixin() {

    internal val entries: MutableList<Entry> = mutableListOf()

    init {
        title?.let { this.title(it) }
        this.entries.addAll(initialEntries)
    }

    /**
     * Adds an item to this group.
     */
    public fun item(
        title: String? = null,
        id: String = entryId(componentType, "itm"),
        context: I.() -> Unit = {}
    ) {
        itemProvider(id, title).apply(context).run {
            group = this@Group
            entries.add(this)
        }
    }

    /**
     * Adds a separator to this group.
     */
    public fun separator() {
        entries.add(Separator(componentType))
    }
}

/**
 * A dropdown item. An item can be disabled and initially selected. In addition, an item can have an optional description and icons. Finally, an item can have a custom layout.
 */
public abstract class Item<I : Item<I>> internal constructor(id: String, title: String?) :
    Entry(id),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal var group: Group<I>? = null
    internal var disabled: Boolean = false

    init {
        title?.let { this.title(it) }
    }

    /**
     * Whether the item is disabled.
     */
    public fun disabled(disabled: Boolean) {
        this.disabled = disabled
    }
}

/**
 * A separator.
 */
public class Separator internal constructor(componentType: ComponentType) : Entry(entryId(componentType, "sep"))

internal class EntryStore : RootStore<List<Entry>>(emptyList())

internal fun entryId(componentType: ComponentType, suffix: String) = Id.unique(componentType.id, suffix)
