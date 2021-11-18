package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.patternfly.DividerVariant.LI
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.debug
import org.patternfly.dom.matches

// ------------------------------------------------------ factory

/**
 * Creates a new [Dropdown] component with static [dropdown entries][DropdownEntry].
 *
 * @param align the alignment of the dropdown
 * @param up controls the direction of the dropdown menu
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.dropdown(
    align: Align? = null,
    up: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Dropdown<T>.() -> Unit = {}
) {
    Dropdown<T>(null, null, align, up).apply(context).render(this, baseClass, id)
}

/**
 * Creates a new [Dropdown] component with [dropdown entries][DropdownEntry] from the specified [store]. Use [Dropdown.display] to specify how to turn the data in the [store] into [dropdown entries][DropdownEntry].
 *
 * @param store the source for the [dropdown entries][DropdownEntry]
 * @param align the alignment of the dropdown
 * @param up controls the direction of the dropdown menu
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.dropdown(
    store: Store<List<T>>,
    idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
    align: Align? = null,
    up: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Dropdown<T>.() -> Unit = {}
) {
    Dropdown(store, idProvider, align, up).apply(context).render(this, baseClass, id)
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
 * @sample org.patternfly.sample.DropdownSample.storeEntries
 */
@Suppress("TooManyFunctions")
public class Dropdown<T> internal constructor(
    private val store: Store<List<T>>?,
    private val idProvider: IdProvider<T, String>?,
    private val align: Align? = null,
    private val up: Boolean = false
) : PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val entries: MutableList<DropdownEntry<T>> = mutableListOf()
    private var display: ((T) -> DropdownEntry<T>)? = null
    private var selectStore: SelectStore<T> = SelectStore()
    private val toggle: DropdownToggle = DropdownToggle(TextToggleKind(null, null) {})
    private lateinit var root: Div

    // custom implementation of WithExpandedStore since we need
    // to reference root.domNode

    /**
     * The store which holds the expanded / collapse state.
     */
    public val expandedStore: ExpandedStore = ExpandedStore { target ->
        !root.domNode.contains(target) && !target.matches(By.classname("dropdown".component("menu-item")))
    }

    /**
     * The current expanded / collapsed state.
     *
     * @sample org.patternfly.sample.DropdownSample.expos
     */
    public val expos: Flow<Boolean> = expandedStore.data.drop(1)

    /**
     * The selections of this dropdown.
     *
     * @sample org.patternfly.sample.DropdownSample.selections
     */
    public val selections: Flow<T>
        get() = selectStore.data.mapNotNull { it } // don't know why filterNotNull cannot be used here!?

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
    public fun group(title: String, context: DropdownGroup<T>.() -> Unit): DropdownEntry<T> =
        DropdownGroup<T>(title).apply(context).also {
            entries.add(it)
        }

    /**
     * Adds an item to this dropdown.
     */
    public fun item(data: T, context: DropdownItem<T>.() -> Unit = {}): DropdownEntry<T> =
        DropdownItem(data).apply(context).also {
            entries.add(it)
        }

    /**
     * Adds a separator.
     */
    public fun separator(): DropdownEntry<T> = DropdownSeparator<T>().also { entries.add(it) }

    /**
     * Defines how to render [dropdown entries][DropdownEntry] when using a store.
     */
    public fun display(display: (T) -> DropdownEntry<T>) {
        this.display = display
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            root = div(
                baseClass = classes {
                    +ComponentType.Dropdown
                    +("top".modifier() `when` up)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Dropdown)
                aria(this)
                element(this)
                events(this)

                classMap(expandedStore.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
                renderToggle(this, toggle.kind)
                renderEntries(this)
            }
        }
    }

    @Suppress("LongMethod")
    private fun renderToggle(context: RenderContext, kind: ToggleKind) {
        with(context) {
            when (kind) {
                is TextToggleKind -> {
                    button(
                        baseClass = classes {
                            +"dropdown".component("toggle")
                            +kind.variation?.modifier
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

                        classMap(toggle.disabled.map { mapOf("disabled".modifier() to it) })
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
                            +kind.variation?.modifier
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
                            disabled(toggle.disabled)
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
            domNode.id = toggle.id
            aria["haspopup"] = true
            aria["expanded"] = expandedStore.data.map { it.toString() }
            disabled(toggle.disabled)
            clicks handledBy expandedStore.toggle
        }
    }

    @Suppress("NestedBlockDepth")
    private fun renderEntries(context: RenderContext) {
        val groups = entries.filterIsInstance<DropdownGroup<T>>().isNotEmpty()
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
                attr("hidden", expandedStore.data.map { !it })
                aria["labelledby"] = toggle.id

                if (store != null && idProvider != null) {
                    store.data.renderEach(idProvider) { data ->
                        val display = this@Dropdown.display ?: { item(data) }
                        renderEntry(this, display(data), 0)
                    }
                } else {
                    normalizeEntries(entries).forEach { renderEntry(this, it, 0) }
                }
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun normalizeEntries(entries: List<DropdownEntry<T>>): List<DropdownEntry<T>> =
        if (entries.filterIsInstance<DropdownGroup<T>>().isNotEmpty()) {
            // add all top level items and separators to unnamed groups
            val normalized = mutableListOf<DropdownEntry<T>>()
            var unnamedGroup: DropdownGroup<T>? = null
            entries.forEach { entry ->
                when (entry) {
                    is DropdownGroup -> {
                        unnamedGroup?.let {
                            normalized.add(it)
                            unnamedGroup = null
                        }
                        normalized.add(entry)
                    }
                    is DropdownItem, is DropdownSeparator -> {
                        if (unnamedGroup == null) {
                            unnamedGroup = DropdownGroup(null)
                        }
                        unnamedGroup?.entries?.add(entry)
                    }
                }
            }
            normalized
        } else {
            entries
        }

    private fun renderEntry(context: RenderContext, entry: DropdownEntry<T>, depth: Int): RenderContext =
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
                                    entry.title.asText()
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

    private fun renderItem(context: RenderContext, item: DropdownItem<T>): RenderContext =
        with(context) {
            button(
                baseClass = classes {
                    +"dropdown".component("menu", "item")
                    +("icon".modifier() `when` (item.icon != null))
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
                clicks.map { item.data } handledBy selectStore.select
                item.events(this)

                if (item.custom != null) {
                    item.custom?.let { content -> content(this) }
                } else {
                    if (item.description != null) {
                        div(baseClass = "dropdown".component("menu", "item", "main")) {
                            item.icon?.let { renderIcon(this, item.iconClass, it) }
                            item.title.asText()
                        }
                        div(baseClass = "dropdown".component("menu", "item", "description")) {
                            +item.description!!
                        }
                    } else {
                        item.icon?.let { renderIcon(this, item.iconClass, it) }
                        item.title.asText()
                    }
                }
            }
        }

    private fun renderIcon(context: RenderContext, iconClass: String, icon: SubComponent<Icon>) {
        with(context) {
            span(baseClass = "dropdown".component("menu", "item", "icon")) {
                icon(iconClass = iconClass, baseClass = icon.baseClass, id = icon.id) {
                    icon.context(this)
                }
            }
        }
    }
}

// ------------------------------------------------------ toggle

internal sealed interface ToggleKind

internal class TextToggleKind(
    val title: String?,
    val variation: ButtonVariation?,
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
    val variation: ButtonVariation?,
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
public class DropdownToggle internal constructor(internal var kind: ToggleKind) {

    internal val id: String = Id.unique(ComponentType.Dropdown.id, "tgl")
    internal var disabled: Flow<Boolean> = flowOf(false)

    /**
     * A text toggle.
     *
     * @sample org.patternfly.sample.DropdownSample.textToggle
     */
    public fun text(
        title: String? = null,
        variation: ButtonVariation? = null,
        context: Span.() -> Unit = {}
    ) {
        kind = TextToggleKind(title = title, variation = variation, context = context)
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
     * A check box toggle.
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
        variation: ButtonVariation? = null,
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit = {}
    ) {
        kind = ActionToggleKind(title = title, variation = variation, baseClass = baseClass, id = id, context)
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
}

// ------------------------------------------------------ item

/**
 * Base class for groups and items.
 */
public sealed class DropdownEntry<T>

/**
 * A dropdown group with a title and nested items.
 *
 * Please note that nested groups are *not* supported!
 */
public class DropdownGroup<T> internal constructor(
    title: String?,
    initialEntries: List<DropdownEntry<T>> = emptyList()
) : DropdownEntry<T>(),
    WithTitle by TitleMixin() {

    internal val entries: MutableList<DropdownEntry<T>> = mutableListOf()

    init {
        title?.let { this.title(it) }
        this.entries.addAll(initialEntries)
    }

    /**
     * Adds an item to this group.
     */
    public fun item(data: T, context: DropdownItem<T>.() -> Unit = {}) {
        DropdownItem<T>(data).apply(context).run {
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
public class DropdownItem<T> internal constructor(public val data: T) :
    DropdownEntry<T>(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal val id: String = Id.unique(ComponentType.Dropdown.id, "itm")
    internal var group: DropdownGroup<T>? = null
    internal var disabled: Boolean = false
    internal var selected: Boolean = false
    internal var iconClass: String = ""
    internal var icon: SubComponent<Icon>? = null
    internal var description: String? = null
    internal var custom: (Button.() -> Unit)? = null

    init {
        this.title(data.toString())
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
    public fun icon(
        iconClass: String = "",
        baseClass: String? = null,
        id: String? = null,
        context: Icon.() -> Unit = {}
    ) {
        this.iconClass = iconClass
        this.icon = SubComponent(baseClass, id, context)
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
    public fun custom(context: Button.() -> Unit) {
        this.custom = context
    }
}

/**
 * A dropdown separator.
 */
public class DropdownSeparator<T> internal constructor(@Suppress("unused") private val id: Int = 23) :
    DropdownEntry<T>()

// ------------------------------------------------------ store

internal class SelectStore<T> : RootStore<T?>(null) {
    val select: Handler<T> = handle { _, data -> data }
}
