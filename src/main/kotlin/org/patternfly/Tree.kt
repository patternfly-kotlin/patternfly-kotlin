package org.patternfly

import dev.fritz2.dom.Tag
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
import dev.fritz2.elemento.styleHidden
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.set

// ------------------------------------------------------ dsl

public fun <T> HtmlElements.pfTreeView(
    identifier: IdProvider<T, String>,
    checkboxes: Boolean = false,
    badges: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: TreeView<T>.() -> Unit = {}
): TreeView<T> = register(
    TreeView(identifier = identifier, checkboxes = checkboxes, badges = badges, id = id, baseClass = baseClass), content
)

public fun <T> pfTree(block: TreeBuilder<T>.() -> Unit = {}): Tree<T> = TreeBuilder<T>().apply(block).build()

public fun <T> pfTreeItem(item: T, block: TreeItemBuilder<T>.() -> Unit = {}): TreeItem<T> =
    TreeItemBuilder(item).apply(block).build()

public fun <T> TreeView<T>.pfTree(block: TreeBuilder<T>.() -> Unit = {}) {
    setTree(TreeBuilder<T>().apply(block).build())
}

public fun <T> TreeBuilder<T>.pfTreeItem(item: T, block: TreeItemBuilder<T>.() -> Unit = {}) {
    val builder = TreeItemBuilder(item).apply(block)
    builders.add(builder)
}

public fun <T> TreeItemBuilder<T>.pfChildren(block: TreeBuilder<T>.() -> Unit = {}) {
    childrenBuilder.apply(block)
}

private fun test() {
    render {
        pfTreeView<String>({ it }) {
            pfTree {
                pfTreeItem("Foo")
                pfTreeItem("Bar") {
                    pfChildren {
                        pfTreeItem("Child 1")
                        pfTreeItem("Child 2")
                    }
                }
            }
        }
    }

    render {
        pfTreeView<String>({ it }) {
            icons = {
                SingleIcon {
                    pfIcon("user".fas())
                }
            }
            fetchItems = { (1..10).map { pfTreeItem(it.toString()) } }
            pfTree {
                pfTreeItem("Root")
            }
        }
    }
}

// ------------------------------------------------------ tag

private const val TREE_ITEM = "treeItem"

public class TreeView<T> internal constructor(
    private val identifier: IdProvider<T, String>,
    private val checkboxes: Boolean,
    private val badges: Boolean,
    id: String?,
    baseClass: String?
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.TreeView, baseClass)) {

    private val ul: Ul
    public var display: ComponentDisplay<Span, T> = {
        { +it.toString() }
    }
    public var icons: TreeIconProvider<T>? = null
    public var fetchItems: (suspend (TreeItem<T>) -> List<TreeItem<T>>)? = null

    init {
        markAs(ComponentType.TreeView)
        ul = ul {
            attr("role", "tree")
        }
    }

    internal fun setTree(tree: Tree<T>) {
        ul.domNode.clear()
        tree.roots.forEach { render(ul, it) }
    }

    private fun render(ul: Ul, treeItem: TreeItem<T>): Li {
        val li: Li
        with(ul) {
            li = li(baseClass = classes {
                +"tree-view".component("list-item")
                +("expandable".modifier() `when` treeItem.hasChildren)
            }) {
                attr("role", "treeitem")
                attr("tabindex", "0")
                domNode.dataset[TREE_ITEM] = this@TreeView.identifier(treeItem.item)
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
                                    domNode.onchange = {
                                        // TODO trigger checked event
                                    }
                                }
                            }
                        }
                        this@TreeView.icons?.let {
                            span(baseClass = "tree-view".component("node", "icon")) {
                                when (val icons = it(treeItem.item)) {
                                    is SingleIcon -> icons.icon(this)
                                    is ColExIcon -> {
                                        icons.collapsed(this)
                                        icons.expanded(this).domNode.apply {
                                            styleHidden = true
                                            dataset["${TREE_ITEM}ExpandedIcon"] = ""
                                        }
                                    }
                                }
                            }
                        }
                        span(baseClass = "tree-view".component("node", "text")) {
                            val content = this@TreeView.display(treeItem.item)
                            content(this)
                        }
                        if (this@TreeView.badges && treeItem.hasChildren) {
                            span(baseClass = "tree-view".component("node", "count")) {
                                pfBadge {
                                    +treeItem.childCount.toString()
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
            collapse(li)
        } else {
            if (fetchItems != null) {
                MainScope().launch {
                    fetchItems?.let { fetch ->
                        val treeItems = fetch(treeItem)
                        if (treeItems.isNotEmpty()) {
                            expand(li, treeItems)
                        }
                    }
                }
            } else {
                if (treeItem.children.isNotEmpty()) {
                    expand(li, treeItem.children)
                }
            }
        }
    }

    private fun expand(li: Element, treeItems: List<TreeItem<T>>) {
        // TODO Change icon
        li.appendChild(render {
            ul {
                attr("role", "group")
                for (childItem in treeItems) {
                    this@TreeView.render(this, childItem)
                }
            }
        }.domNode)
        li.aria["expanded"] = true
        li.classList += "expanded".modifier()

        // TODO trigger expand event
    }

    private fun collapse(li: Element) {
        // TODO Change icon
        li.aria["expanded"] = false
        li.classList -= "expanded".modifier()
        li.querySelector(By.element("ul").and(By.attribute("role", "group"))).removeFromParent()

        // TODO trigger collapse event
    }

    private fun select(button: Element) {
        // (1) deselect all
        domNode.querySelectorAll(By.classname("tree-view".component("node"))).asList().forEach {
            if (it is HTMLElement) {
                it.classList -= "current".modifier()
            }
        }
        // (2) select current
        button.classList += "current".modifier()

        // TODO (3) trigger select event
    }
}

// ------------------------------------------------------ type

public typealias TreeIconProvider<T> = (T) -> TreeIcon

public sealed class TreeIcon
public class SingleIcon(public val icon: HtmlElements.() -> Tag<HTMLElement>) : TreeIcon()
public class ColExIcon(
    public val collapsed: HtmlElements.() -> Tag<HTMLElement>,
    public val expanded: HtmlElements.() -> Tag<HTMLElement>
) : TreeIcon()

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

public class TreeItem<T>(override val item: T, public var childCount: Int) : HasItem<T> {
    private var _parent: TreeItem<T>? = null
    private val _children: MutableList<TreeItem<T>> = mutableListOf()

    public val parent: TreeItem<T>?
        get() = _parent

    public val hasParent: Boolean
        get() = _parent != null

    public val children: List<TreeItem<T>>
        get() = _children

    public val hasChildren: Boolean
        get() = childCount > 0 || _children.isNotEmpty()

    public fun addChild(treeItem: TreeItem<T>) {
        treeItem._parent = this
        _children.add(treeItem)
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

    override fun toString(): String = "TreeItem(item=$item)"
}

// ------------------------------------------------------ builder

public class TreeBuilder<T> {
    internal val builders: MutableList<TreeItemBuilder<T>> = mutableListOf()

    internal fun build(): Tree<T> {
        val tree = Tree<T>()
        for (builder in builders) {
            tree.add(builder.build())
        }
        return tree
    }
}

public class TreeItemBuilder<T>(private val item: T) {
    public var childCount: Int = 0
    internal val childrenBuilder: TreeBuilder<T> = TreeBuilder()

    internal fun build(): TreeItem<T> {
        val treeItem = TreeItem(item, childCount)
        for (cb in childrenBuilder.builders) {
            val child = cb.build()
            treeItem.addChild(child)
        }
        return treeItem
    }
}
