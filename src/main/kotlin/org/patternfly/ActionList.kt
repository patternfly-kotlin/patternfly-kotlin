package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext

// ------------------------------------------------------ factory

/**
 * Creates an [ActionList] component.
 *
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.actionList(
    icons: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: ActionList.() -> Unit = {}
) {
    ActionList(icons).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [action list](https://www.patternfly.org/v4/components/action-list/design-guidelines) component.
 *
 * An action list is a group of actions with set spacing. An action list can contain items, groups and a mix of both.
 *
 * @sample org.patternfly.sample.ActionListSample.items
 * @sample org.patternfly.sample.ActionListSample.mixed
 */
public open class ActionList(private val icons: Boolean) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin() {

    private val elements: MutableList<ActionListElement> = mutableListOf()

    /**
     * Adds a new group.
     */
    public fun group(
        baseClass: String? = null,
        id: String? = null,
        context: ActionListGroup.() -> Unit
    ) {
        elements.add(ActionListGroup(baseClass, id).also(context))
    }

    /**
     * Adds a new item.
     */
    public fun item(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        elements.add(ActionListItem(baseClass, id, context))
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(
                baseClass = classes {
                    +ComponentType.ActionList
                    +("icons".modifier() `when` icons)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.ActionList)
                applyElement(this)
                for (element in elements) {
                    when (element) {
                        is ActionListGroup -> {
                            div(baseClass = element.baseClass, id = element.id) {
                                for (item in element.items) {
                                    div(baseClass = item.baseClass, id = item.id) {
                                        item.context(this)
                                    }
                                }
                            }
                        }
                        is ActionListItem -> {
                            div(baseClass = element.baseClass, id = element.id) {
                                element.context(this)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Common interface for action list items and groups.
 */
public sealed interface ActionListElement

/**
 * An action list group.
 */
public class ActionListGroup internal constructor(baseClass: String?, id: String?) :
    ActionListElement, SubComponent<Div>(classes("action-list".component("group"), baseClass), id, {}) {

    internal val items: MutableList<ActionListItem> = mutableListOf()

    public fun item(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        items.add(ActionListItem(baseClass, id, context))
    }
}

/**
 * An action list item.
 */
public class ActionListItem internal constructor(baseClass: String?, id: String?, context: Div.() -> Unit) :
    ActionListElement, SubComponent<Div>(classes("action-list".component("item"), baseClass), id, context)
