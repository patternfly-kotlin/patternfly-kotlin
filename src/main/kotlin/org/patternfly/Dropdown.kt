package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.DividerVariant.LI
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.debug
import org.patternfly.dom.matches

// ------------------------------------------------------ factory

/**
 * Creates a new [Dropdown] component.
 *
 * @param align the alignment of the dropdown
 * @param up controls the direction of the dropdown menu
 * @param grouped whether the dropdown contains groups
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.dropdown(
    align: Align? = null,
    up: Boolean = false,
    grouped: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Dropdown.() -> Unit = {}
) {
    Dropdown(align = align, up = up, grouped = grouped).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [dropdown](https://www.patternfly.org/v4/components/dropdown/design-guidelines) component.
 *
 * A dropdown presents a menu of actions in a constrained space that will trigger a process or navigate to a new location. A dropdown consists of a [toggle][DropdownToggle] to open and close a menu of [entries][DropdownEntry].
 *
 * You can choose between different toggle variations:
 * - [text toggle][DropdownToggle.text]
 * - [icon toggle][DropdownToggle.icon]
 * - [kebab toggle][DropdownToggle.kebab]
 * - [badge toggle][DropdownToggle.badge]
 * - [checkbox toggle][DropdownToggle.checkbox]
 * - [action toggle][DropdownToggle.action]
 *
 * The [dropdown entries][DropdownEntry] can be added statically or by using a store. See the samples for more details.
 *
 * @sample org.patternfly.sample.DropdownSample.staticEntries
 * @sample org.patternfly.sample.DropdownSample.dynamicEntries
 */
@Suppress("TooManyFunctions")
public open class Dropdown(
    private val align: Align?,
    private val up: Boolean,
    private val grouped: Boolean
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    /**
     * The store which holds the expanded / collapse state.
     */
    public val expandedStore: ExpandedStore = ExpandedStore { target ->
        !root.domNode.contains(target) && !target.matches(By.classname("dropdown".component("menu-item")))
    }

    private var storeItems: Boolean = false
    private val itemStore: DropdownEntryStore = DropdownEntryStore()
    private val headEntries: MutableList<DropdownEntry> = mutableListOf()
    private val tailEntries: MutableList<DropdownEntry> = mutableListOf()
    private val toggle: DropdownToggle = DropdownToggle(TextToggleKind(null, null) {}, expandedStore)
    private lateinit var root: Div

    /**
     * The current expanded / collapsed state.
     *
     * @sample org.patternfly.sample.DropdownSample.excos
     */
    public val excos: Flow<Boolean> = expandedStore.data.drop(1)

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
    public fun toggle(context: DropdownToggle.() -> Unit) {
        toggle.apply(context)
    }

    /**
     * Adds a group to this dropdown.
     */
    public fun group(title: String, context: DropdownGroup.() -> Unit) {
        (if (storeItems) tailEntries else headEntries).add(
            DropdownGroup(
                Id.unique(ComponentType.Dropdown.id, "grp"),
                title,
                emptyList()
            ).apply(context)
        )
    }

    /**
     * Adds an item to this dropdown.
     */
    public fun item(title: String, context: DropdownItem.() -> Unit = {}) {
        (if (storeItems) tailEntries else headEntries).add(
            DropdownItem(
                Id.unique(ComponentType.Dropdown.id, "itm"),
                title
            ).apply(context)
        )
    }

    /**
     * Adds a separator.
     */
    public fun separator() {
        (if (storeItems) tailEntries else headEntries).add(DropdownSeparator())
    }

    /**
     * Adds the items from the specified store.
     */
    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: DropdownEntries.(T) -> DropdownEntry
    ) {
        items(values.data, idProvider, display)
    }

    /**
     * Adds the items from the specified flow.
     */
    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: DropdownEntries.(T) -> DropdownEntry
    ) {
        (MainScope() + NotificationStore.job).launch {
            values.collect { values ->
                itemStore.update(
                    values.map { value ->
                        DropdownEntries(idProvider(value)).run {
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
                    +ComponentType.Dropdown
                    +("top".modifier() `when` up)
                    +align?.modifier
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Dropdown)
                applyElement(this)
                applyEvents(this)

                with(expandedStore) { toggleExpanded() }
                toggle.render(this)
                renderEntries(this)
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun renderEntries(context: RenderContext) {
        val groups = grouped || (headEntries + tailEntries).filterIsInstance<DropdownGroup>().isNotEmpty()
        with(context) {
            val classes = classes {
                +"dropdown".component("menu")
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
                    renderEntry(this, entry, 0)
                }
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun normalizeEntries(entries: List<DropdownEntry>): List<DropdownEntry> =
        if (entries.filterIsInstance<DropdownGroup>().isNotEmpty()) {
            // add all top level items and separators to unnamed groups
            val normalized = mutableListOf<DropdownEntry>()
            var unnamedGroup: DropdownGroup? = null
            entries.forEach { entry ->
                when (entry) {
                    is DropdownGroup -> {
                        unnamedGroup = null
                        normalized.add(entry)
                    }
                    is DropdownItem, is DropdownSeparator -> {
                        if (unnamedGroup == null) {
                            unnamedGroup = DropdownGroup(
                                Id.unique(ComponentType.Dropdown.id, "top", "lvl", "grp"),
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

    private fun renderEntry(context: RenderContext, entry: DropdownEntry, depth: Int): RenderContext =
        with(context) {
            when (entry) {
                is DropdownGroup -> {
                    section(baseClass = "dropdown".component("group")) {
                        if (depth > 0) {
                            val message = "Nested groups are not supported in dropdown"
                            !message
                            console.warn("$message: ${root.domNode.debug()}")
                        } else {
                            if (entry.hasTitle) {
                                h1(baseClass = "dropdown".component("group", "title")) {
                                    entry.applyTitle(this)
                                }
                            }
                            ul {
                                attr("role", "none")
                                entry.entries.forEach { renderEntry(this, it, depth + 1) }
                            }
                        }
                    }
                }
                is DropdownItem -> {
                    li {
                        attr("role", "menuitem")
                        renderItem(this, entry)
                    }
                }
                is DropdownSeparator -> {
                    divider(LI)
                }
            }
        }

    private fun renderItem(context: RenderContext, item: DropdownItem): RenderContext =
        with(context) {
            button(
                baseClass = classes {
                    +"dropdown".component("menu", "item")
                    +("icon".modifier() `when` (item.iconContext != null))
                    +("description".modifier() `when` (item.description != null))
                    +("disabled".modifier() `when` item.disabled)
                }
            ) {
                attr("tabindex", "-1")
                if (item.disabled) {
                    aria["disabled"] = true
                    attr("disabled", "true")
                }
                if (item.selected) {
                    domNode.autofocus = true
                }
                clicks handledBy expandedStore.collapse
                item.applyEvents(this)

                if (item.content != null) {
                    item.content?.let { content -> content.context(this) }
                } else {
                    if (item.description != null) {
                        div(baseClass = "dropdown".component("menu", "item", "main")) {
                            item.iconContext?.let { renderIcon(this, item.iconClass, it) }
                            item.applyTitle(this)
                        }
                        div(baseClass = "dropdown".component("menu", "item", "description")) {
                            +item.description!!
                        }
                    } else {
                        item.iconContext?.let { renderIcon(this, item.iconClass, it) }
                        item.applyTitle(this)
                    }
                }
            }
        }

    private fun renderIcon(context: RenderContext, iconClass: String, iconContext: Icon.() -> Unit) {
        with(context) {
            span(baseClass = "dropdown".component("menu", "item", "icon")) {
                icon(iconClass = iconClass) {
                    iconContext(this)
                }
            }
        }
    }
}

// ------------------------------------------------------ toggle

internal sealed interface ToggleKind

internal class TextToggleKind(
    val title: String?,
    val variant: ButtonVariant?,
    val context: Span.() -> Unit
) : ToggleKind

internal class IconToggleKind(
    val iconClass: String,
    val baseClass: String?,
    val id: String?,
    val context: Icon.() -> Unit
) : ToggleKind

internal class BadgeToggleKind(
    val count: Int,
    val min: Int,
    val max: Int,
    val read: Boolean,
    val baseClass: String?,
    val id: String?,
    val context: Badge.() -> Unit
) : ToggleKind

internal class DropdownBadge(kind: BadgeToggleKind) : Badge(
    count = kind.count,
    min = kind.min,
    max = kind.max,
    read = kind.read
) {
    override fun tail(context: RenderContext) {
        with(context) {
            span(baseClass = "dropdown".component("toggle", "icon")) {
                icon("caret-down".fas())
            }
        }
    }
}

internal class CheckboxToggleKind(
    val title: String?,
    val baseClass: String?,
    val id: String?,
    val context: Input.() -> Unit
) : ToggleKind

internal class ActionToggleKind(
    val title: String?,
    val variant: ButtonVariant?,
    val baseClass: String?,
    val id: String?,
    val context: Button.() -> Unit
) : ToggleKind

internal class ImageToggleKind(
    val title: String,
    val src: String,
    val baseClass: String?,
    val id: String?,
    val context: Img.() -> Unit
) : ToggleKind

/**
 * The dropdown toggle.
 *
 * You can choose between different toggle variations:
 * - [text toggle][DropdownToggle.text]
 * - [icon toggle][DropdownToggle.icon]
 * - [kebab toggle][DropdownToggle.kebab]
 * - [badge toggle][DropdownToggle.badge]
 * - [checkbox toggle][DropdownToggle.checkbox]
 * - [action toggle][DropdownToggle.action]
 */
public class DropdownToggle internal constructor(
    private var kind: ToggleKind,
    private val expandedStore: ExpandedStore
) {

    internal val id: String = Id.unique(ComponentType.Dropdown.id, "tgl")
    internal var disabled: Flow<Boolean> = flowOf(false)

    /**
     * A text toggle.
     *
     * @sample org.patternfly.sample.DropdownSample.textToggle
     */
    public fun text(
        title: String? = null,
        variant: ButtonVariant? = null,
        context: Span.() -> Unit = {}
    ) {
        kind = TextToggleKind(title = title, variant = variant, context = context)
    }

    /**
     * An icon toggle.
     *
     * @sample org.patternfly.sample.DropdownSample.iconToggle
     */
    public fun icon(
        iconClass: String = "",
        baseClass: String? = null,
        id: String? = null,
        context: Icon.() -> Unit = {}
    ) {
        kind = IconToggleKind(iconClass = iconClass, baseClass = baseClass, id = id, context = context)
    }

    /**
     * An icon toggle with a predefined "kebab" icon.
     *
     * @sample org.patternfly.sample.DropdownSample.kebabToggle
     */
    public fun kebab(
        baseClass: String? = null,
        id: String? = null,
        context: Icon.() -> Unit = {}
    ) {
        icon(iconClass = "ellipsis-v".fas(), baseClass = baseClass, id = id, context = context)
    }

    /**
     * A badge toggle.
     *
     * @sample org.patternfly.sample.DropdownSample.badgeToggle
     */
    public fun badge(
        count: Int = 0,
        min: Int = Badge.BADGE_MIN,
        max: Int = Badge.BADGE_MAX,
        read: Boolean = false,
        baseClass: String? = null,
        id: String? = null,
        context: Badge.() -> Unit = {}
    ) {
        kind = BadgeToggleKind(
            count = count,
            min = min,
            max = max,
            read = read,
            baseClass = baseClass,
            id = id,
            context = context
        )
    }

    /**
     * A checkbox toggle.
     *
     * @sample org.patternfly.sample.DropdownSample.checkboxToggle
     */
    public fun checkbox(
        title: String? = null,
        baseClass: String? = null,
        id: String? = null,
        context: Input.() -> Unit = {}
    ) {
        kind = CheckboxToggleKind(title = title, baseClass = baseClass, id = id, context = context)
    }

    /**
     * An action toggle.
     *
     * @sample org.patternfly.sample.DropdownSample.actionToggle
     */
    public fun action(
        title: String? = null,
        variant: ButtonVariant? = null,
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit = {}
    ) {
        kind = ActionToggleKind(title = title, variant = variant, baseClass = baseClass, id = id, context)
    }

    /**
     * An image toggle.
     *
     * @sample org.patternfly.sample.DropdownSample.imgToggle
     */
    public fun img(
        title: String = "",
        src: String = "",
        baseClass: String? = null,
        id: String? = null,
        context: Img.() -> Unit = {}
    ) {
        kind = ImageToggleKind(title = title, src = src, baseClass = baseClass, id = id, context = context)
    }

    @Suppress("LongMethod")
    internal fun render(context: RenderContext) {
        with(context) {
            when (val kind = this@DropdownToggle.kind) {
                is TextToggleKind -> {
                    button(
                        baseClass = classes {
                            +"dropdown".component("toggle")
                            +kind.variant?.modifier
                        }
                    ) {
                        setupToggleButton(this)
                        span(baseClass = "dropdown".component("toggle", "text")) {
                            kind.title?.let { +it }
                            kind.context(this)
                        }
                        span(baseClass = "dropdown".component("toggle", "icon")) {
                            icon("caret-down".fas())
                        }
                    }
                }

                is IconToggleKind -> {
                    button(
                        baseClass = classes(
                            "dropdown".component("toggle"),
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

                is BadgeToggleKind -> {
                    button(
                        baseClass = classes(
                            "dropdown".component("toggle"),
                            "plain".modifier()
                        )
                    ) {
                        setupToggleButton(this)
                        DropdownBadge(kind).apply(kind.context).render(
                            context = this,
                            baseClass = kind.baseClass,
                            id = kind.id
                        )
                    }
                }

                is CheckboxToggleKind -> {
                    div(
                        baseClass = classes(
                            "dropdown".component("toggle"),
                            "split-button".modifier()
                        )
                    ) {
                        val inputId =
                            kind.id ?: Id.unique(ComponentType.Dropdown.id, "tgl", "chk")
                        val titleId = if (kind.title != null) {
                            Id.unique(ComponentType.Dropdown.id, "tgl", "txt")
                        } else null

                        classMap(disabled.map { mapOf("disabled".modifier() to it) })
                        label(baseClass = "dropdown".component("toggle", "check")) {
                            `for`(inputId)
                            input(id = inputId, baseClass = kind.baseClass) {
                                titleId?.let { id ->
                                    aria["labelledby"] = id
                                }
                                type("checkbox")
                                kind.context(this)
                            }
                            kind.title?.let { title ->
                                span(baseClass = "dropdown".component("toggle", "text")) {
                                    aria["hidden"] = true
                                    +title
                                }
                            }
                        }
                        button(baseClass = "dropdown".component("toggle", "button")) {
                            setupToggleButton(this)
                            icon("caret-down".fas())
                        }
                    }
                }

                is ActionToggleKind -> {
                    div(
                        baseClass = classes {
                            +"dropdown".component("toggle")
                            +"split-button".modifier()
                            +"action".modifier()
                            +kind.variant?.modifier
                        }
                    ) {
                        button(
                            baseClass = classes(
                                "dropdown".component("toggle", "button"),
                                kind.baseClass
                            ),
                            id = kind.id
                        ) {
                            kind.title?.let { +it }
                            disabled(disabled)
                            kind.context(this)
                        }
                        button(baseClass = "dropdown".component("toggle", "button")) {
                            setupToggleButton(this)
                            icon("caret-down".fas())
                        }
                    }
                }

                is ImageToggleKind -> {
                    button(baseClass = "dropdown".component("toggle")) {
                        setupToggleButton(this)
                        span(baseClass = "dropdown".component("toggle", "image")) {
                            img(baseClass = kind.baseClass, id = kind.id) {
                                kind.context(this)
                            }
                        }
                        span(baseClass = "dropdown".component("toggle", "text")) {
                            +kind.title
                        }
                        span(baseClass = "dropdown".component("toggle", "icon")) {
                            icon("caret-down".fas())
                        }
                    }
                }
            }
        }
    }

    private fun setupToggleButton(button: Button) {
        with(button) {
            domNode.id = this@DropdownToggle.id
            aria["haspopup"] = true
            aria["expanded"] = expandedStore.data.map { it.toString() }
            disabled(disabled)
            clicks handledBy expandedStore.toggle
        }
    }
}

// ------------------------------------------------------ item & store

public class DropdownEntries(internal val id: String) {

    public fun group(title: String, context: DropdownGroup.() -> Unit): DropdownGroup =
        DropdownGroup(Id.build(id, "grp"), title, emptyList()).apply(context)

    public fun item(title: String? = null, context: DropdownItem.() -> Unit = {}): DropdownItem =
        DropdownItem(Id.build(id, "itm"), title).apply(context)

    public fun separator(): DropdownSeparator = DropdownSeparator()
}

/**
 * Base class for groups and items.
 */
public sealed class DropdownEntry(internal val id: String)

/**
 * A dropdown group with a title and nested items.
 *
 * Please note that nested groups are *not* supported!
 */
public class DropdownGroup internal constructor(
    id: String,
    title: String?,
    initialEntries: List<DropdownEntry>
) : DropdownEntry(id),
    WithTitle by TitleMixin() {

    internal val entries: MutableList<DropdownEntry> = mutableListOf()

    init {
        title?.let { this.title(it) }
        this.entries.addAll(initialEntries)
    }

    /**
     * Adds an item to this group.
     */
    public fun item(title: String? = null, context: DropdownItem.() -> Unit = {}) {
        DropdownItem(Id.unique(id, "itm"), title).apply(context).run {
            group = this@DropdownGroup
            entries.add(this)
        }
    }

    /**
     * Adds a separator to this group.
     */
    public fun separator() {
        entries.add(DropdownSeparator())
    }
}

/**
 * A dropdown item. An item can be disabled and initially selected. In addition, an item can have an optional description and icons. Finally, an item can have a custom layout.
 *
 * @sample org.patternfly.sample.DropdownSample.customEntries
 */
public class DropdownItem internal constructor(id: String, title: String?) :
    DropdownEntry(id),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal var group: DropdownGroup? = null
    internal var disabled: Boolean = false
    internal var selected: Boolean = false
    internal var iconClass: String = ""
    internal var iconContext: (Icon.() -> Unit)? = null
    internal var description: String? = null
    internal var content: (SubComponent<Button>)? = null

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
     * Whether the item is initially selected.
     */
    public fun selected(selected: Boolean) {
        this.selected = selected
    }

    /**
     * An optional icon for the item.
     */
    public fun icon(iconClass: String = "", context: Icon.() -> Unit = {}) {
        this.iconClass = iconClass
        this.iconContext = context
    }

    /**
     * An optional description for the item.
     */
    public fun description(description: String) {
        this.description = description
    }

    /**
     * Defines a custom layout for the item.
     */
    public fun content(
        id: String? = null,
        baseClass: String? = null,
        context: Button.() -> Unit
    ) {
        this.content = SubComponent(baseClass, id, context)
    }
}

/**
 * A dropdown separator.
 */
public class DropdownSeparator : DropdownEntry(Id.unique(ComponentType.Dropdown.id, "sep"))

internal class DropdownEntryStore : RootStore<List<DropdownEntry>>(emptyList())
