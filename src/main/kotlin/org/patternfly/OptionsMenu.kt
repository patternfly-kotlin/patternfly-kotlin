package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.debug
import org.patternfly.dom.matches

// ------------------------------------------------------ factory

/**
 * Creates a new [OptionsMenu] component.
 *
 * @param grouped whether the options menu contains groups
 * @param align the alignment of the options menu
 * @param up controls the direction of the options menu
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.optionsMenu(
    grouped: Boolean = false,
    align: Align? = null,
    up: Boolean = false,
    closeOnSelect: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: OptionsMenu.() -> Unit = {}
) {
    OptionsMenu(
        grouped = grouped,
        align = align,
        up = up,
        closeOnSelect = closeOnSelect
    ).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [options menu](https://www.patternfly.org/v4/components/options-menu/design-guidelines) component.
 *
 * An options menu is similar to a dropdown, but provides a way to select among a set of optional settings rather than trigger an action. An options menu consists of a [toggle][OptionsMenuToggle] to open and close a menu of [entries][OptionsMenuEntry].
 *
 * You can choose between different toggle variations:
 * - [text toggle][OptionsMenuToggle.text]
 * - [icon toggle][OptionsMenuToggle.icon]
 * - [kebab toggle][OptionsMenuToggle.kebab]
 *
 * The [options menu entries][OptionsMenuEntry] can be added statically or by using a store. See the samples for more details.
 *
 * @sample org.patternfly.sample.OptionsMenuSample.staticEntries
 * @sample org.patternfly.sample.OptionsMenuSample.dynamicEntries
 */
@Suppress("TooManyFunctions")
public class OptionsMenu(
    private val grouped: Boolean,
    private val align: Align?,
    private val up: Boolean,
    private val closeOnSelect: Boolean
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    /**
     * The store which holds the expanded / collapse state.
     */
    public val expandedStore: ExpandedStore = ExpandedStore { target ->
        !root.domNode.contains(target) && !target.matches(By.classname("options-menu".component("menu")))
    }

    private var storeItems: Boolean = false
    private val itemStore: OptionsMenuEntryStore = OptionsMenuEntryStore()
    private val headEntries: MutableList<OptionsMenuEntry> = mutableListOf()
    private val tailEntries: MutableList<OptionsMenuEntry> = mutableListOf()
    private val toggle: OptionsMenuToggle = OptionsMenuToggle(OptionsMenuTextToggleKind(null, null) {}, expandedStore)
    private lateinit var root: Div

    /**
     * The current expanded / collapsed state.
     *
     * @sample org.patternfly.sample.OptionsMenuSample.excos
     */
    public val excos: Flow<Boolean> = expandedStore.data.drop(1)

    /**
     * Disables or enables the options menu.
     */
    public fun disabled(value: Boolean) {
        toggle.disabled = flowOf(value)
    }

    /**
     * Disables or enables the options menu based on the values from the flow.
     */
    public fun disabled(value: Flow<Boolean>) {
        toggle.disabled = value
    }

    /**
     * Sets the toggle for this options menu.
     */
    public fun toggle(context: OptionsMenuToggle.() -> Unit) {
        toggle.apply(context)
    }

    /**
     * Adds a group to this options menu.
     */
    public fun group(title: String, context: OptionsMenuGroup.() -> Unit) {
        (if (storeItems) tailEntries else headEntries).add(
            OptionsMenuGroup(
                Id.unique(ComponentType.OptionsMenu.id, "grp"),
                title,
                emptyList()
            ).apply(context)
        )
    }

    /**
     * Adds an item to this options menu.
     */
    public fun item(title: String, context: OptionsMenuItem.() -> Unit = {}) {
        (if (storeItems) tailEntries else headEntries).add(
            OptionsMenuItem(
                Id.unique(ComponentType.OptionsMenu.id, "itm"),
                title
            ).apply(context)
        )
    }

    /**
     * Adds a separator.
     */
    public fun separator() {
        (if (storeItems) tailEntries else headEntries).add(OptionsMenuSeparator())
    }

    /**
     * Adds the items from the specified store.
     */
    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: OptionsMenuEntries.(T) -> OptionsMenuEntry
    ) {
        items(values.data, idProvider, display)
    }

    /**
     * Adds the items from the specified flow.
     */
    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: OptionsMenuEntries.(T) -> OptionsMenuEntry
    ) {
        (MainScope() + NotificationStore.job).launch {
            values.collect { values ->
                itemStore.update(
                    values.map { value ->
                        OptionsMenuEntries(idProvider(value)).run {
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
                    +ComponentType.OptionsMenu
                    +("top".modifier() `when` up)
                    +align?.modifier
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.OptionsMenu)
                applyElement(this)
                applyEvents(this)

                with(expandedStore) { toggleExpanded() }
                toggle.render(this)
                renderEntries(this)
            }
        }
    }

    private fun renderEntries(context: RenderContext) {
        val groups = grouped || (headEntries + tailEntries).filterIsInstance<OptionsMenuGroup>().isNotEmpty()
        with(context) {
            val classes = classes {
                +"options-menu".component("menu")
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
                }.map { entries ->
                    normalizeEntries(entries)
                }.renderEach(into = this, idProvider = { it.id }) { entry ->
                    renderEntry(this, entry, groups, 0)
                }
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun normalizeEntries(entries: List<OptionsMenuEntry>): List<OptionsMenuEntry> =
        if (entries.filterIsInstance<OptionsMenuGroup>().isNotEmpty()) {
            // add all top level items and separators to unnamed groups
            val normalized = mutableListOf<OptionsMenuEntry>()
            var unnamedGroup: OptionsMenuGroup? = null
            entries.forEach { entry ->
                when (entry) {
                    is OptionsMenuGroup -> {
                        unnamedGroup = null
                        normalized.add(entry)
                    }
                    is OptionsMenuItem, is OptionsMenuSeparator -> {
                        if (unnamedGroup == null) {
                            unnamedGroup = OptionsMenuGroup(
                                Id.unique(ComponentType.OptionsMenu.id, "top", "lvl", "grp"),
                                null,
                                emptyList()
                            )
                            normalized.add(unnamedGroup!!)
                        }
                        unnamedGroup?.entries?.add(entry)
                    }
                }
            }
            normalized
        } else {
            entries
        }

    private fun renderEntry(
        context: RenderContext,
        entry: OptionsMenuEntry,
        groups: Boolean,
        depth: Int
    ): RenderContext = with(context) {
        when (entry) {
            is OptionsMenuGroup -> {
                section(baseClass = "options-menu".component("group")) {
                    if (depth > 0) {
                        val message = "Nested groups are not supported in options menu"
                        !message
                        console.warn("$message: ${root.domNode.debug()}")
                    } else {
                        if (entry.hasTitle) {
                            h1(baseClass = "options-menu".component("group", "title")) {
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
            is OptionsMenuItem -> {
                li {
                    attr("role", "menuitem")
                    renderItem(this, entry)
                }
            }
            is OptionsMenuSeparator -> {
                if (groups && depth == 0) {
                    divider(DividerVariant.HR)
                } else {
                    divider(DividerVariant.LI)
                }
            }
        }
    }

    private fun renderItem(context: RenderContext, item: OptionsMenuItem): RenderContext =
        with(context) {
            button(
                baseClass = classes {
                    +"options-menu".component("menu", "item")
                    +("disabled".modifier() `when` item.disabled)
                }
            ) {
                attr("tabindex", "-1")
                if (item.disabled) {
                    aria["disabled"] = true
                    attr("disabled", "true")
                }
                if (closeOnSelect) {
                    clicks handledBy expandedStore.collapse
                }
                item.applyEvents(this)
                item.applyTitle(this)
                item.selected.filter { it }.render(this) {
                    span(baseClass = "options-menu".component("menu", "item", "icon")) {
                        icon("check".fas())
                    }
                }
            }
        }
}

// ------------------------------------------------------ toggle

internal sealed interface OptionsMenuToggleKind

internal class OptionsMenuTextToggleKind(
    val title: String?,
    val variant: ButtonVariant?,
    val context: Span.() -> Unit
) : OptionsMenuToggleKind

internal class OptionsMenuIconToggleKind(
    val iconClass: String,
    val baseClass: String?,
    val id: String?,
    val context: Icon.() -> Unit
) : OptionsMenuToggleKind

/**
 * The options menu toggle.
 *
 * You can choose between different toggle variations:
 * - [text toggle][OptionsMenuToggle.text]
 * - [icon toggle][OptionsMenuToggle.icon]
 * - [kebab toggle][OptionsMenuToggle.kebab]
 */
public class OptionsMenuToggle internal constructor(
    private var kind: OptionsMenuToggleKind,
    private val expandedStore: ExpandedStore
) {

    internal val id: String = Id.unique(ComponentType.OptionsMenu.id, "tgl")
    internal var disabled: Flow<Boolean> = flowOf(false)

    /**
     * A (plain) text toggle.
     *
     * @sample org.patternfly.sample.OptionsMenuSample.textToggle
     * @sample org.patternfly.sample.OptionsMenuSample.plainTextToggle
     */
    public fun text(
        title: String? = null,
        variant: ButtonVariant? = null,
        context: Span.() -> Unit = {}
    ) {
        kind = OptionsMenuTextToggleKind(title = title, variant = variant, context = context)
    }

    /**
     * An icon toggle.
     *
     * @sample org.patternfly.sample.OptionsMenuSample.iconToggle
     */
    public fun icon(
        iconClass: String = "",
        baseClass: String? = null,
        id: String? = null,
        context: Icon.() -> Unit = {}
    ) {
        kind = OptionsMenuIconToggleKind(iconClass = iconClass, baseClass = baseClass, id = id, context = context)
    }

    /**
     * An icon toggle with a predefined "kebab" icon.
     *
     * @sample org.patternfly.sample.OptionsMenuSample.kebabToggle
     */
    public fun kebab(
        baseClass: String? = null,
        id: String? = null,
        context: Icon.() -> Unit = {}
    ) {
        icon(iconClass = "ellipsis-v".fas(), baseClass = baseClass, id = id, context = context)
    }

    @Suppress("LongMethod")
    internal fun render(context: RenderContext) {
        with(context) {
            when (val kind = this@OptionsMenuToggle.kind) {
                is OptionsMenuTextToggleKind -> {
                    button(
                        baseClass = classes {
                            +"options-menu".component("toggle")
                            +("text".modifier() `when` (kind.variant == ButtonVariant.plain))
                            +kind.variant?.modifier
                        }
                    ) {
                        setupToggleButton(this)
                        span(baseClass = "options-menu".component("toggle", "text")) {
                            kind.title?.let { +it }
                            kind.context(this)
                        }
                        span(baseClass = "options-menu".component("toggle", "icon")) {
                            icon("caret-down".fas())
                        }
                    }
                }

                is OptionsMenuIconToggleKind -> {
                    button(
                        baseClass = classes(
                            "options-menu".component("toggle"),
                            "plain".modifier()
                        )
                    ) {
                        setupToggleButton(this)
                        icon(
                            iconClass = kind.iconClass,
                            baseClass = kind.baseClass,
                            id = kind.id,
                            context = kind.context
                        )
                    }
                }
            }
        }
    }

    private fun setupToggleButton(button: Button) {
        with(button) {
            domNode.id = this@OptionsMenuToggle.id
            aria["haspopup"] = "listbox"
            aria["expanded"] = expandedStore.data.map { it.toString() }
            disabled(disabled)
            clicks handledBy expandedStore.toggle
        }
    }
}

// ------------------------------------------------------ item & store

public class OptionsMenuEntries(internal val id: String) {

    public fun group(title: String, context: OptionsMenuGroup.() -> Unit): OptionsMenuGroup =
        OptionsMenuGroup(Id.build(id, "grp"), title, emptyList()).apply(context)

    public fun item(title: String? = null, context: OptionsMenuItem.() -> Unit = {}): OptionsMenuItem =
        OptionsMenuItem(Id.build(id, "itm"), title).apply(context)

    public fun separator(): OptionsMenuSeparator = OptionsMenuSeparator()
}

/**
 * Base class for groups and items.
 */
public sealed class OptionsMenuEntry(internal val id: String)

/**
 * A options menu group with a title and nested items.
 *
 * Please note that nested groups are *not* supported!
 */
public class OptionsMenuGroup internal constructor(
    id: String,
    title: String?,
    initialEntries: List<OptionsMenuEntry>
) : OptionsMenuEntry(id),
    WithTitle by TitleMixin() {

    internal val entries: MutableList<OptionsMenuEntry> = mutableListOf()

    init {
        title?.let { this.title(it) }
        this.entries.addAll(initialEntries)
    }

    /**
     * Adds an item to this group.
     */
    public fun item(title: String? = null, context: OptionsMenuItem.() -> Unit = {}) {
        OptionsMenuItem(Id.unique(id, "itm"), title).apply(context).run {
            group = this@OptionsMenuGroup
            entries.add(this)
        }
    }

    /**
     * Adds a separator to this group.
     */
    public fun separator() {
        entries.add(OptionsMenuSeparator())
    }
}

/**
 * An options menu item. An item can be disabled and initially selected.
 */
public class OptionsMenuItem internal constructor(id: String, title: String?) :
    OptionsMenuEntry(id),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal var group: OptionsMenuGroup? = null
    internal var disabled: Boolean = false
    internal var selected: Flow<Boolean> = flowOf(false)

    init {
        title?.let { this.title(it) }
    }

    /**
     * Whether the item is disabled.
     */
    public fun disabled(disabled: Boolean) {
        this.disabled = disabled
    }

    /**
     * Whether the item is selected.
     */
    public fun selected(selected: Flow<Boolean>) {
        this.selected = selected
    }
}

/**
 * A options menu separator.
 */
public class OptionsMenuSeparator : OptionsMenuEntry(Id.unique(ComponentType.OptionsMenu.id, "sep"))

internal class OptionsMenuEntryStore : RootStore<List<OptionsMenuEntry>>(emptyList())
