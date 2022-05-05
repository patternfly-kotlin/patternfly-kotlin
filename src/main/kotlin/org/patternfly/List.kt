package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.dom.Id
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ factory

/**
 * @param type the list type
 * @param inline whether to render the list items horizontally
 * @param bordered whether to show a border between the list items
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.list(
    type: ListType = ListType.PLAIN,
    inline: Boolean = false,
    bordered: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: FormattedList.() -> Unit = {}
) {
    FormattedList(type, inline, bordered).apply(context).render(this, baseClass, id)
}

internal fun test() {
    render {
        list {
            item { +"First item" }
            items(storeOf(listOf("1", "2", "2"))) {data ->
                item { +data }
            }
            item { +"Lat item" }
        }
    }
}

// ------------------------------------------------------ component

/**
 * PatternFly [list](https://www.patternfly.org/v4/components/list/design-guidelines) component.
 *
 * A list component embeds a formatted list (bulleted or numbered list) into page content.
 */
public open class FormattedList(
    private val type: ListType,
    private val inline: Boolean,
    private val bordered: Boolean
) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var itemsInStore: Boolean = false
    private val itemStore: ListItemStore = ListItemStore()
    private val headItems: MutableList<ListItem> = mutableListOf()
    private val tailItems: MutableList<ListItem> = mutableListOf()

    public fun item(
        baseClass: String? = null,
        id: String? = null,
        context: ListItem.() -> Unit = {}
    ) {
        (if (itemsInStore) tailItems else headItems).add(
            ListItem(baseClass, id ?: Id.unique(ComponentType.List.id)).apply(context),
        )
    }

    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: ListItemScope.(T) -> ListItem
    ) {
        items(values.data, idProvider, display)
    }

    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: ListItemScope.(T) -> ListItem
    ) {
        (MainScope() + itemStore.job).launch {
            values.collect { values ->
                itemStore.update(
                    values.map { value ->
                        ListItemScope(idProvider(value)).run {
                            display.invoke(this, value)
                        }
                    }
                )
            }
        }
        itemsInStore = true
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            val classes = classes {
                +ComponentType.List
                +("plain".modifier() `when` type.plain())
                +("inline".modifier() `when` inline)
                +("bordered".modifier() `when` bordered)
                +("icon-lg".modifier() `when` (type == ListType.LARGE_ICONS))
                +baseClass
            }
            if (type == ListType.ORDERED) {
                ol(baseClass = classes, id = id) {
                    markAs(ComponentType.ChipGroup)
                    applyElement(this)
                    applyEvents(this)
                    renderItems(this)
                }
            } else {
                ul(baseClass = classes, id = id) {
                    markAs(ComponentType.ChipGroup)
                    applyElement(this)
                    applyEvents(this)
                    renderItems(this)
                }
            }
        }
    }

    private fun renderItems(context: Tag<HTMLElement>) {
        with(context) {
            itemStore.data.map { items ->
                headItems + items + tailItems
            }.render(into = this) { items ->
                for (item in items) {
                    li(
                        baseClass = classes {
                            +("list".component("item") `when` type.icon())
                            +item.baseClass
                        },
                        id = item.id
                    ) {
                        if (item.icon != null) {
                            span(baseClass = "list".component("item", "icon")) {
                                item.icon?.invoke(this)
                            }
                        }
                        if (item.content != null) {
                            item.content?.invoke(this)
                        } else {
                            if (item.icon != null) {
                                span(baseClass = "list".component("item", "text")) {
                                    item.applyTitle(this)
                                }
                            } else {
                                item.applyTitle(this)
                            }
                        }
                    }
                }
            }
        }
    }
}

public enum class ListType {
    /** No bullet points, no numbers */
    PLAIN,

    /** Bullet points */
    UNORDERED,

    /** Numbers */
    ORDERED,

    /** No bullet points, no numbers, but custom icons */
    ICONS,

    /** No bullet points, no numbers, but custom large icons */
    LARGE_ICONS;

    public fun plain(): Boolean = this == PLAIN || this == ICONS || this == LARGE_ICONS

    public fun icon(): Boolean = this == ICONS || this == LARGE_ICONS
}

// ------------------------------------------------------ item & store

public class ListItemScope(internal val id: String) {

    public fun item(
        baseClass: String? = null,
        id: String? = null,
        context: ListItem.() -> Unit = {}
    ): ListItem = ListItem(baseClass, id ?: this.id).apply(context)
}

public class ListItem internal constructor(
    internal val baseClass: String?,
    internal val id: String
) : WithTitle by TitleMixin() {

    internal var icon: (RenderContext.() -> Unit)? = null
    internal var content: (Li.() -> Unit)? = null

    public fun icon(iconClass: String = "", context: Icon.() -> Unit = {}) {
        this.icon = {
            icon(iconClass = iconClass) {
                context(this)
            }
        }
    }

    public fun content(content: RenderContext.() -> Unit) {
        this.content = content
    }
}

internal class ListItemStore : RootStore<List<ListItem>>(emptyList())