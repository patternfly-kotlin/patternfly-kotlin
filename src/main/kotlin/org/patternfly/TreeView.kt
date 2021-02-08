package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.EventType
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.html.renderElement
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.dom.clear
import org.patternfly.dom.By
import org.patternfly.dom.aria
import org.patternfly.dom.displayNone
import org.patternfly.dom.minusAssign
import org.patternfly.dom.plusAssign
import org.patternfly.dom.querySelector
import org.patternfly.dom.querySelectorAll
import org.patternfly.dom.removeFromParent
import org.w3c.dom.CustomEvent
import org.w3c.dom.CustomEventInit
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.dom.set

// TODO Document me
// ------------------------------------------------------ dsl

public fun <T> RenderContext.treeView(
    idProvider: IdProvider<T, String> = { it.toString() },
    checkboxes: Boolean = false,
    badges: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: TreeView<T>.() -> Unit = {}
): TreeView<T> = register(
    TreeView(
        idProvider,
        checkboxes = checkboxes,
        badges = badges,
        id = id,
        baseClass = baseClass,
        job
    ),
    content
)

// ------------------------------------------------------ tag

private const val TREE_ITEM = "ti"
private const val EXPANDED_ICON = TREE_ITEM + "ei"
private const val COLLAPSED_ICON = TREE_ITEM + "ci"

@Suppress("LongParameterList")
public class TreeView<T> internal constructor(
    private val idProvider: IdProvider<T, String>,
    private val checkboxes: Boolean,
    private val badges: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.TreeView, baseClass), job) {

    private var ul: Ul
    private var _tree: Tree<T> = Tree(emptyList())
    private var display: ComponentDisplay<Span, T> = { +it.toString() }
    private var iconProvider: TreeIconProvider<T>? = null
    private var fetchItems: (suspend (TreeItem<T>) -> List<TreeItem<T>>)? = null

    public val treeItemSelects: Flow<TreeItem<T>> by lazy {
        val type = EventType<CustomEvent>(TREE_ITEM_SELECT)
        Listener<CustomEvent, HTMLElement>(
            callbackFlow {
                val listener: (Event) -> Unit = { offer(it.unsafeCast<CustomEvent>()) }
                domNode.addEventListener(type.name, listener)
                awaitClose { domNode.removeEventListener(type.name, listener) }
            }
        ).map { it.detail.unsafeCast<TreeItem<T>>() }
    }

    init {
        markAs(ComponentType.TreeView)
        ul = ul(baseClass = "tree-view".component("list")) {
            attr("role", "tree")
        }
    }

    public val tree: Tree<T>
        get() = _tree

    public fun show(tree: Tree<T>) {
        ul.domNode.clear()
        tree.roots.forEach { treeItem ->
            this@TreeView.renderTreeItem(ul, treeItem)
        }
        this._tree = tree
    }

    @Suppress("NestedBlockDepth")
    public fun select(item: T) {
        tree.find { idProvider(item) == idProvider(it.unwrap()) }?.let { match ->
            val lastIndex = match.path.lastIndex
            match.path.forEachIndexed { index, treeItem ->
                val identifier = idProvider(treeItem.unwrap())
                domNode.querySelector(By.data(TREE_ITEM, identifier))?.let { li ->
                    if (index != lastIndex) {
                        if (li.aria["expanded"] != "true") {
                            expand(li, treeItem)
                        }
                    } else {
                        li.querySelector(By.classname("tree-view".component("node")))?.let { button ->
                            selectInternal(li, button, treeItem)
                            button.scrollIntoView()
                        }
                    }
                }
            }
        }
    }

    public fun display(display: ComponentDisplay<Span, T>) {
        this.display = display
    }

    public fun iconProvider(iconProvider: TreeIconProvider<T>) {
        this.iconProvider = iconProvider
    }

    public fun fetchItems(fetchItems: FetchItems<T>) {
        this.fetchItems = fetchItems
    }

    @Suppress("LongMethod", "ComplexMethod")
    private fun renderTreeItem(ul: Ul, treeItem: TreeItem<T>) {
        with(ul) {
            li(
                baseClass = classes {
                    +"tree-view".component("list-item")
                    +("expandable".modifier() `when` treeItem.hasChildren)
                }
            ) {
                attr("role", "treeitem")
                attr("tabindex", "0")
                domNode.dataset[TREE_ITEM] = this@TreeView.idProvider(treeItem.item)
                div(baseClass = "tree-view".component("content")) {
                    button(baseClass = "tree-view".component("node")) {
                        domNode.addEventListener(
                            Events.click.name,
                            {
                                this@TreeView.selectInternal(this@li.domNode, this.domNode, treeItem)
                            }
                        )
                        if (treeItem.hasChildren || (this@TreeView.fetchItems != null && !treeItem.fetched)) {
                            div(baseClass = "tree-view".component("node", "toggle")) {
                                span(baseClass = "tree-view".component("node", "toggle", "icon")) {
                                    icon("angle-right".fas())
                                }
                            }
                        }
                        if (this@TreeView.checkboxes) {
                            span(baseClass = "tree-view".component("node", "check")) {
                                input {
                                    domNode.type = "checkbox"
                                    domNode.onclick = { it.stopPropagation() }
                                    domNode.onchange = {
                                        // TODO trigger checked event
                                    }
                                }
                            }
                        }
                        this@TreeView.iconProvider?.let { tip ->
                            span(baseClass = "tree-view".component("node", "icon")) {
                                when (val icons = tip(treeItem.item)) {
                                    is SingleIcon -> icons.icon(this)
                                    is DoubleIcon -> {
                                        icons.collapsed(this).domNode.apply {
                                            dataset[COLLAPSED_ICON] = ""
                                        }
                                        icons.expanded(this).domNode.apply {
                                            displayNone = true
                                            dataset[EXPANDED_ICON] = ""
                                        }
                                    }
                                }
                            }
                        }
                        span(baseClass = "tree-view".component("node", "text")) {
                            this@TreeView.display(this, treeItem.item)
                        }
                        if (this@TreeView.badges && treeItem.hasChildren) {
                            span(baseClass = "tree-view".component("node", "count")) {
                                badge {
                                    +treeItem.children.size.toString()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun selectInternal(li: Element, button: Element, treeItem: TreeItem<T>) {
        // (1) deselect all
        domNode.querySelectorAll(By.classname("tree-view".component("node"))).asList().forEach {
            if (it is HTMLElement) {
                it.classList -= "current".modifier()
            }
        }
        // (2) select current
        button.classList += "current".modifier()

        // (3) toggle
        if (li.aria["expanded"] == "true") {
            collapse(li)
        } else {
            if (treeItem.hasChildren) {
                expand(li, treeItem)
            } else {
                if (fetchItems != null && !treeItem.fetched) {
                    (MainScope() + job).launch {
                        fetchItems?.let { fetch ->
                            val treeItems = fetch(treeItem)
                            treeItem.fetched = true
                            treeItems.forEach { treeItem.addChild(it) }
                            if (treeItems.isNotEmpty()) {
                                expand(li, treeItem)
                            } else {
                                li.querySelector(By.classname("tree-view".component("node", "toggle")))
                                    ?.removeFromParent()
                            }
                        }
                    }
                }
            }
        }

        // (4) fire select event
        domNode.dispatchEvent(CustomEvent(TREE_ITEM_SELECT, CustomEventInit(treeItem)))
    }

    private fun expand(li: Element, treeItem: TreeItem<T>) {
        flipIcons(li, true)
        li.appendChild(
            renderElement {
                ul {
                    attr("role", "group")
                    for (childItem in treeItem.children) {
                        this@TreeView.renderTreeItem(this, childItem)
                    }
                }
            }.domNode
        )
        li.aria["expanded"] = true
        li.classList += "expanded".modifier()
    }

    private fun collapse(li: Element) {
        flipIcons(li, false)
        li.aria["expanded"] = false
        li.classList -= "expanded".modifier()
        li.querySelector(By.element("ul").and(By.attribute("role", "group"))).removeFromParent()
    }

    private fun flipIcons(li: Element, expand: Boolean) {
        val collapsedIcon = li.querySelector(By.data(COLLAPSED_ICON))
        val expandedIcon = li.querySelector(By.data(EXPANDED_ICON))
        if (collapsedIcon != null && expandedIcon != null) {
            collapsedIcon.displayNone = expand
            expandedIcon.displayNone = !expand
        }
    }
}

// ------------------------------------------------------ type

@Suppress("SpellCheckingInspection")
private const val TREE_ITEM_SELECT = "treeitemselect"

public typealias FetchItems<T> = suspend (TreeItem<T>) -> List<TreeItem<T>>

public typealias TreeIconProvider<T> = (T) -> TreeIcon

public sealed class TreeIcon
public class SingleIcon(public val icon: RenderContext.() -> Tag<HTMLElement>) : TreeIcon()
public class DoubleIcon(
    public val collapsed: RenderContext.() -> Tag<HTMLElement>,
    public val expanded: RenderContext.() -> Tag<HTMLElement>
) : TreeIcon()
