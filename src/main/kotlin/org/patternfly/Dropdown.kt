package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id
import org.patternfly.dom.displayNone
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement

// ------------------------------------------------------ factory

/**
 * Creates a new [Dropdown] component.
 *
 * @param grouped whether the dropdown contains groups
 * @param align the alignment of the dropdown
 * @param up controls the direction of the dropdown menu
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.dropdown(
    grouped: Boolean = false,
    align: Align? = null,
    up: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Dropdown.() -> Unit = {}
) {
    Dropdown(grouped = grouped, align = align, up = up).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [dropdown](https://www.patternfly.org/v4/components/dropdown/design-guidelines) component.
 *
 * A dropdown presents a menu of actions in a constrained space that will trigger a process or navigate to a new location. A dropdown consists of a [toggle][DropdownToggle] to open and close a menu of [entries][Entry].
 *
 * You can choose between different toggle variations:
 * - [text toggle][Toggle.text]
 * - [icon toggle][Toggle.icon]
 * - [kebab toggle][Toggle.kebab]
 * - [badge toggle][Toggle.badge]
 * - [checkbox toggle][Toggle.checkbox]
 * - [action toggle][Toggle.action]
 *
 * The [dropdown entries][Entry] can be added statically or by using a store. See the samples for more details.
 *
 * @sample org.patternfly.sample.DropdownSample.staticEntries
 * @sample org.patternfly.sample.DropdownSample.dynamicEntries
 */
public open class Dropdown(grouped: Boolean, align: Align?, up: Boolean) :
    EntriesComponent<DropdownToggle, DropdownItem>(
        ComponentType.Dropdown,
        "dropdown",
        grouped,
        align,
        up,
        ::DropdownItem
    ) {

    override val toggle: DropdownToggle = DropdownToggle(TextToggleKind(null, null) {}, expandedStore)

    override fun renderItem(context: RenderContext, entry: Entry): Tag<HTMLLIElement> =
        with(context) {
            li {
                if (entry is DropdownItem) {
                    attr("role", "menuitem")
                    renderDropdownItem(this, entry)
                } else {
                    with(domNode) {
                        hidden = true
                        displayNone = true
                    }
                    val message = "Unsupported entry $entry"
                    console.log(message)
                    !message
                }
            }
        }

    private fun renderDropdownItem(context: RenderContext, item: DropdownItem): RenderContext =
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

/**
 * The dropdown toggle. The dropdown component supports the following toggles:
 *
 * - [text toggle][Toggle.text]
 * - [icon toggle][Toggle.icon]
 * - [kebab toggle][Toggle.kebab]
 * - [badge toggle][Toggle.badge]
 * - [checkbox toggle][Toggle.checkbox]
 * - [action toggle][Toggle.action]
 */
public class DropdownToggle internal constructor(kind: ToggleKind, expandedStore: ExpandedStore) :
    Toggle(ComponentType.Dropdown, "dropdown", kind, expandedStore) {

    override fun renderBadgeToggle(tag: Tag<HTMLElement>, kind: BadgeToggleKind) {
        with(tag) {
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
    }

    override fun renderCheckboxToggle(tag: Tag<HTMLElement>, kind: CheckboxToggleKind) {
        with(tag) {
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
    }

    override fun renderActionToggle(tag: Tag<HTMLElement>, kind: ActionToggleKind) {
        with(tag) {
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
    }

    override fun renderImageToggle(tag: Tag<HTMLElement>, kind: ImageToggleKind) {
        with(tag) {
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

    override fun setupToggleButton(button: Button) {
        super.setupToggleButton(button)
        with(button) {
            aria["haspopup"] = true
        }
    }
}

// ------------------------------------------------------ entries

public class DropdownItem internal constructor(id: String, title: String?) :
    Item<DropdownItem>(id, title) {

    internal var iconClass: String = ""
    internal var iconContext: (Icon.() -> Unit)? = null
    internal var description: String? = null
    internal var content: (SubComponent<Button>)? = null

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
