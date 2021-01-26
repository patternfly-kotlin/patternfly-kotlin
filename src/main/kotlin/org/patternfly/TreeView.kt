package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.html.renderElement
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
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
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.set

// TODO Document me
// ------------------------------------------------------ dsl

public fun <T> RenderContext.treeView(
    selected: SelectedTreeItemStore<T>,
    expanded: ExpandedTreeItemStore<T>,
    idProvider: IdProvider<T, String>,
    checkboxes: Boolean = false,
    badges: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: TreeView<T>.() -> Unit = {}
): TreeView<T> = register(
    TreeView(
        selected,
        expanded,
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
    public val selected: SelectedTreeItemStore<T>,
    public val expanded: ExpandedTreeItemStore<T>,
    override val idProvider: IdProvider<T, String>,
    private val checkboxes: Boolean,
    private val badges: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>,
    WithIdProvider<T>,
    Div(id = id, baseClass = classes(ComponentType.TreeView, baseClass), job) {

    private val ul: Ul
    private var _tree: Tree<T> = Tree()
    private var display: ComponentDisplay<Span, T> = { +it.toString() }
    private var iconProvider: TreeIconProvider<T>? = null
    private var fetchItems: (suspend (TreeItem<T>) -> List<TreeItem<T>>)? = null

    init {
        markAs(ComponentType.TreeView)
        ul = ul {
            attr("role", "tree")
        }
        (MainScope() + job).launch {
            selected.data.collect {
                console.log("Selected tree item: ${it.item}")
            }
        }
    }

    public val tree: Tree<T>
        get() = _tree

    public fun display(display: ComponentDisplay<Span, T>) {
        this.display = display
    }

    public fun iconProvider(iconProvider: TreeIconProvider<T>) {
        this.iconProvider = iconProvider
    }

    public fun fetchItems(fetchItems: FetchItems<T>) {
        this.fetchItems = fetchItems
    }

    internal fun initTree(tree: Tree<T>) {
        ul.domNode.clear()
        _tree = tree
        _tree.roots.forEach { render(ul, it) }
    }

    @Suppress("LongMethod")
    private fun render(ul: Ul, treeItem: TreeItem<T>): Li {
        val li: Li
        with(ul) {
            li = li(
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
                        domNode.onclick = {
                            this@TreeView.toggle(this@li.domNode, treeItem)
                            this@TreeView.select(this.domNode, treeItem)
                        }
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
        return li
    }

    private fun toggle(li: Element, treeItem: TreeItem<T>) {
        if (li.aria["expanded"] == "true") {
            collapse(li, treeItem)
        } else {
            if (treeItem.hasChildren) {
                expand(li, treeItem)
            } else {
                if (fetchItems != null && !treeItem.fetched) {
                    MainScope().launch {
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
    }

    private fun expand(li: Element, treeItem: TreeItem<T>) {
        flipIcons(li, true)
        li.appendChild(
            renderElement {
                ul {
                    attr("role", "group")
                    for (childItem in treeItem.children) {
                        this@TreeView.render(this, childItem)
                    }
                }
            }.domNode
        )
        li.aria["expanded"] = true
        li.classList += "expanded".modifier()

        // update store
        expanded.expand(treeItem)
    }

    private fun collapse(li: Element, treeItem: TreeItem<T>) {
        flipIcons(li, false)
        li.aria["expanded"] = false
        li.classList -= "expanded".modifier()
        li.querySelector(By.element("ul").and(By.attribute("role", "group"))).removeFromParent()

        // update store
        expanded.collapse(treeItem)
    }

    private fun flipIcons(li: Element, expand: Boolean) {
        val collapsedIcon = li.querySelector(By.data(COLLAPSED_ICON))
        val expandedIcon = li.querySelector(By.data(EXPANDED_ICON))
        if (collapsedIcon != null && expandedIcon != null) {
            collapsedIcon.displayNone = expand
            expandedIcon.displayNone = !expand
        }
    }

    private fun select(button: Element, treeItem: TreeItem<T>) {
        // (1) deselect all
        domNode.querySelectorAll(By.classname("tree-view".component("node"))).asList().forEach {
            if (it is HTMLElement) {
                it.classList -= "current".modifier()
            }
        }
        // (2) select current
        button.classList += "current".modifier()

        // (3) update store
        selected.update(treeItem)
    }
}

// ------------------------------------------------------ type

public typealias FetchItems<T> = suspend (TreeItem<T>) -> List<TreeItem<T>>

public typealias TreeIconProvider<T> = (T) -> TreeIcon

public sealed class TreeIcon
public class SingleIcon(public val icon: RenderContext.() -> Tag<HTMLElement>) : TreeIcon()
public class DoubleIcon(
    public val collapsed: RenderContext.() -> Tag<HTMLElement>,
    public val expanded: RenderContext.() -> Tag<HTMLElement>
) : TreeIcon()

// ------------------------------------------------------ store

public class SelectedTreeItemStore<T>(initial: TreeItem<T>) : RootStore<TreeItem<T>>(initial) {

    public val path: Flow<List<TreeItem<T>>> = data.map { treeItem ->
        val path = mutableListOf<TreeItem<T>>()
        var current = treeItem
        path.add(current)
        while (current.hasParent) {
            current = current.parent!!
            path.add(current)
        }
        path.reversed()
    }
}

public class ExpandedTreeItemStore<T>(initial: TreeItem<T>) :
    RootStore<Pair<TreeItem<T>, Boolean>>(Pair(initial, false)) {

    internal val collapse: Handler<TreeItem<T>> = handle { _, treeItem ->
        Pair(treeItem, false)
    }

    internal val expand: Handler<TreeItem<T>> = handle { _, treeItem ->
        Pair(treeItem, true)
    }
}
