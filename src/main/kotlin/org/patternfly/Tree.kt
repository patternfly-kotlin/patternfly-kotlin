package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.action
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.html.render
import dev.fritz2.elemento.By
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.minusAssign
import dev.fritz2.elemento.plusAssign
import dev.fritz2.elemento.querySelector
import dev.fritz2.elemento.querySelectorAll
import dev.fritz2.elemento.removeFromParent
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.set

// ------------------------------------------------------ dsl

public fun <T> HtmlElements.pfTreeView(
    store: TreeStore<T>,
    checkboxes: Boolean = false,
    badges: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: TreeView<T>.() -> Unit = {}
): TreeView<T> = register(
    TreeView(store = store, checkboxes = checkboxes, badges = badges, id = id, baseClass = baseClass), content
)

public fun <T> pfTree(block: TreeBuilder<T>.() -> Unit = {}): Tree<T> = TreeBuilder<T>().apply(block).build()

public fun <T> pfTreeItem(item: T, block: TreeItemBuilder<T>.() -> Unit = {}): TreeItem<T> =
    TreeItemBuilder(item).apply(block).build()

public fun <T> TreeView<T>.pfTree(block: TreeBuilder<T>.() -> Unit = {}) {
    val treeItems = TreeBuilder<T>().apply(block).build()
    action(treeItems) handledBy this.store.update
}

public fun <T> TreeBuilder<T>.pfTreeItem(item: T, block: TreeItemBuilder<T>.() -> Unit = {}) {
    val builder = TreeItemBuilder(item).apply(block)
    builders.add(builder)
}

public fun <T> TreeItemBuilder<T>.pfChildren(block: TreeBuilder<T>.() -> Unit = {}) {
    childrenBuilder.apply(block)
}

// ------------------------------------------------------ tag

private const val TREE_ITEM = "treeItem"

public class TreeView<T> internal constructor(
    internal val store: TreeStore<T>,
    private val checkboxes: Boolean,
    private val badges: Boolean,
    id: String?,
    baseClass: String?
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.TreeView, baseClass)) {

    private val ul: Ul

    public var display: ComponentDisplay<Span, T> = {
        { +it.toString() }
    }

    public var fetchItems: suspend (TreeItem<T>) -> List<TreeItem<T>> = { emptyList() }

    init {
        markAs(ComponentType.TreeView)
        ul = ul {
            attr("role", "tree")
        }

        MainScope().launch {
            store.data.collect { items ->
                for (root in items.roots) {
                    with(ul) {
                        this@TreeView.renderTreeItem(this, root)
                    }
                }
            }
        }
    }

    private fun renderTreeItem(ul: Ul, treeItem: TreeItem<T>): Li {
        val li: Li
        with(ul) {
            li = li(baseClass = classes {
                +"tree-view".component("list-item")
                +("expandable".modifier() `when` treeItem.hasChildren)
                +("expanded".modifier() `when` (treeItem.hasChildren && treeItem.expanded))
            }) {
                attr("role", "treeitem")
                attr("tabindex", "0")
                domNode.dataset[TREE_ITEM] = this@TreeView.store.identifier(treeItem.item)
                if (treeItem.hasChildren) {
                    aria["expanded"] = treeItem.expanded
                }
                div(baseClass = "tree-view".component("content")) {
                    button(baseClass = "tree-view".component("node")) {
                        domNode.onclick = {
                            this@TreeView.toggle(this@li.domNode, treeItem)
                            this@TreeView.select(this.domNode)
                        }
                        if (treeItem.hasChildren) {
                            div(baseClass = "tree-view".component("node", "toggle")) {
                                span(baseClass = "tree-view".component("node", "toggle", "icon")) {
                                    pfIcon("angle-right".fas())
                                }
                            }
                        }
                        if (this@TreeView.checkboxes) {
                            span(baseClass = "tree-view".component("node", "check")) {
                                input {
                                    domNode.type = "checkbox"
                                    domNode.onclick = { it.stopPropagation() }
                                }
                            }
                        }
                        if (treeItem.icon != null) {
                            span(baseClass = "tree-view".component("node", "icon")) {
                                if (treeItem.expanded && treeItem.expandedIcon != null) {
                                    treeItem.expandedIcon.invoke(this)
                                } else {
                                    treeItem.icon.invoke(this)
                                }
                            }
                        }
                        span(baseClass = "tree-view".component("node", "text")) {
                            val content = this@TreeView.display(treeItem.item)
                            content(this)
                        }
                        if (this@TreeView.badges && treeItem.childCount > 0) {
                            span(baseClass = "tree-view".component("node", "count")) {
                                pfBadge {
                                    +treeItem.childCount.toString()
                                }
                            }
                        }
                    }
                }
                if (treeItem.children.isNotEmpty() && treeItem.expanded) {
                    this@TreeView.expand(this.domNode, treeItem, treeItem.children)
                }
            }
        }
        return li
    }

    private fun toggle(li: Element, treeItem: TreeItem<T>) {
        if (li.aria["expanded"] == "true") {
            collapse(li, treeItem)
        } else {
            MainScope().launch {
                val treeItems = fetchItems(treeItem)
                if (treeItems.isNotEmpty()) {
                    expand(li, treeItem, treeItems)
                }
            }
        }
    }

    private fun expand(li: Element, treeItem: TreeItem<T>, treeItems: List<TreeItem<T>>) {
        treeItem.expanded = true
//        treeItem.addChildren(treeItems)
        li.appendChild(render {
            ul {
                attr("role", "group")
                for (childItem in treeItems) {
                    this@TreeView.renderTreeItem(this@ul, childItem)
                }
            }
        }.domNode)
        li.aria["expanded"] = true
        li.classList += "expanded".modifier()
    }

    private fun collapse(li: Element, treeItem: TreeItem<T>) {
        treeItem.expanded = false
//        treeItem.removeChildren()
        li.aria["expanded"] = false
        li.classList -= "expanded".modifier()
        li.querySelector(By.element("ul").and(By.attribute("role", "group"))).removeFromParent()
    }

    private fun select(button: Element) {
        // (1) deselect all
        domNode.querySelectorAll(By.classname("tree-view".component("node"))).asList().forEach {
            if (it is HTMLElement) {
                it.classList -= "current".modifier()
            }
        }
        // (2) select the current element and its parent
        button.classList += "current".modifier()
    }
}

// ------------------------------------------------------ store

public class TreeStore<T>(public val identifier: IdProvider<T, String>) : RootStore<Tree<T>>(Tree())

// ------------------------------------------------------ type

public class Tree<T> {

    private val _roots: MutableList<TreeItem<T>> = mutableListOf()

    public val roots: List<TreeItem<T>>
        get() = _roots

    public fun add(treeItem: TreeItem<T>): Boolean = _roots.add(treeItem)

    public fun find(predicate: (TreeItem<T>) -> Boolean): TreeItem<T>? {
        var result: TreeItem<T>? = null
        val iterator = _roots.iterator()
        while (iterator.hasNext() && result == null) {
            val root = iterator.next()
            result = root.find(predicate)
        }
        return result
    }

    public fun findAll(predicate: (TreeItem<T>) -> Boolean): List<TreeItem<T>> {
        val result: MutableList<TreeItem<T>> = mutableListOf()
        for (root in _roots) {
            result.addAll(root.findAll(predicate))
        }
        return result
    }
}

public class TreeItem<T>(
    override val item: T,
    public var expanded: Boolean,
    public var selected: Boolean,
    public var mightHaveChildren: Boolean,
    public var childCount: Int,
    public val icon: (Span.() -> Unit)?,
    public val expandedIcon: (Span.() -> Unit)?
) : HasItem<T> {

    private var _parent: TreeItem<T>? = null
    private val _children: MutableList<TreeItem<T>> = mutableListOf()

    public val parent: TreeItem<T>?
        get() = _parent

    public val hasParent: Boolean
        get() = _parent != null

    public val children: List<TreeItem<T>>
        get() = _children

    public val hasChildren: Boolean
        get() = mightHaveChildren || childCount > 0 || _children.isNotEmpty()

    public fun addChild(treeItem: TreeItem<T>) {
        treeItem._parent = this
        _children.add(treeItem)
    }

    public fun addChildren(treeItems: List<TreeItem<T>>) {
        treeItems.forEach { addChild(it) }
    }

    public fun removeChildren() {
        _children.forEach {
            it._parent = null
        }
        _children.clear()
    }

    public fun find(predicate: (TreeItem<T>) -> Boolean): TreeItem<T>? {
        var result: TreeItem<T>? = null
        if (predicate(this)) {
            result = this
        } else {
            val iterator = _children.iterator()
            while (iterator.hasNext() && result == null) {
                val child = iterator.next()
                result = child.find(predicate)
            }
        }
        return result
    }

    public fun findAll(predicate: (TreeItem<T>) -> Boolean): List<TreeItem<T>> {
        val result: MutableList<TreeItem<T>> = mutableListOf()
        collect(predicate, result)
        return result
    }

    private fun collect(predicate: (TreeItem<T>) -> Boolean, collector: MutableList<TreeItem<T>>) {
        if (predicate(this)) {
            collector.add(this)
        }
        for (child in _children) {
            child.collect(predicate, collector)
        }
    }

    override fun toString(): String = "TreeItem(item=$item, expanded=$expanded, selected=$selected)"
}

// ------------------------------------------------------ builder

public class TreeBuilder<T> {
    internal val builders: MutableList<TreeItemBuilder<T>> = mutableListOf()

    internal fun build(): Tree<T> {
        val tree = Tree<T>()
        for (builder in builders) {
            tree.add(builder.build())
        }
        // make sure expanded is true from deepest child up to the root
        tree.findAll { it.expanded }.forEach {
            var current = it.parent
            while (current != null) {
                current.expanded = true
                current = current.parent
            }
        }
        return tree
    }
}

public class TreeItemBuilder<T>(private val item: T) {
    public var expanded: Boolean = false
    public var selected: Boolean = false
    public var mightHaveChildren: Boolean = false
    public var childCount: Int = 0
    public var icon: (Span.() -> Unit)? = null
    public var expandedIcon: (Span.() -> Unit)? = null
    internal val childrenBuilder: TreeBuilder<T> = TreeBuilder()

    internal fun build(): TreeItem<T> {
        val treeItem = TreeItem(
            item = item,
            expanded = expanded,
            selected = selected,
            mightHaveChildren = mightHaveChildren,
            childCount = childCount,
            icon = icon,
            expandedIcon = expandedIcon
        )
        for (cb in childrenBuilder.builders) {
            val child = cb.build()
            treeItem.addChild(child)
        }
        return treeItem
    }
}
