package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.action
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.html.render
import dev.fritz2.elemento.By
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.debug
import dev.fritz2.elemento.querySelector
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.set

// ------------------------------------------------------ dsl

public fun <T> HtmlElements.pfTreeView(
    store: TreeStore<T>,
    id: String? = null,
    baseClass: String? = null,
    content: TreeView<T>.() -> Unit = {}
): TreeView<T> = register(TreeView(store, id = id, baseClass = baseClass), content)

public fun <T> TreeView<T>.pfTreeItems(block: TreeItemsBuilder<T>.() -> Unit = {}) {
    val treeItems = TreeItemsBuilder<T>().apply(block).build(store.identifier)
    action(treeItems) handledBy this.store.update
}

public fun <T> TreeItemsBuilder<T>.pfTreeItem(item: T, block: TreeItemBuilder<T>.() -> Unit = {}) {
    val builder = TreeItemBuilder(item).apply(block)
    builders.add(builder)
}

public fun <T> TreeItemBuilder<T>.pfTreeItems(block: TreeItemsBuilder<T>.() -> Unit = {}) {
    treeItemsBuilder.apply(block)
}

internal fun test() {
    render {
        val store = TreeStore<String> { Id.build(it) }
        pfTreeView(store) {
            pfTreeItems {
                pfTreeItem("Application Launcher") {
                    pfTreeItems {
                        pfTreeItem("Application 1") {
                            pfTreeItems {
                                pfTreeItem("Settings")
                                pfTreeItem("Current")
                            }
                        }
                        pfTreeItem("Application 2") {
                            pfTreeItems {
                                pfTreeItem("Settings")
                                pfTreeItem("Loader") {
                                    pfTreeItems {
                                        pfTreeItem("Loading App 1")
                                        pfTreeItem("Loading App 2")
                                        pfTreeItem("Loading App 3")
                                    }
                                }
                            }
                        }
                    }
                }
                pfTreeItem("Cost Management") {
                    pfTreeItems {
                        pfTreeItem("Application 3") {
                            pfTreeItems {
                                pfTreeItem("Settings")
                                pfTreeItem("Current")
                            }
                        }
                    }
                }
                pfTreeItem("Sources") {
                    pfTreeItem("Application 4")
                }
                pfTreeItem("Really long folder name that overflows the container it is in") {
                    pfTreeItem("Application 5")
                }
            }
        }
    }
}

// ------------------------------------------------------ tag

private const val TREE_ITEM = "treeItem"

public class TreeView<T> internal constructor(internal val store: TreeStore<T>, id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.TreeView, baseClass)) {

    private val tus: TreeUpdateStore = TreeUpdateStore()

    init {
        markAs(ComponentType.TreeView)
        ul {
            attr("role", "tree")
            this@TreeView.store.rootItems.each().render { treeItem ->
                this@TreeView.renderTreeItem(this@ul, treeItem)
            }.bind()
        }

        MainScope().launch {
            tus.data.collect {
                domNode.querySelector(By.data(TREE_ITEM, it))?.let { element ->
                    console.log("Receive update for $it: ${element.debug()}")
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
                        if (treeItem.hasChildren) {
                            span(baseClass = "tree-view".component("node", "toggle", "icon")) {
                                pfIcon("angle-right".fas())
                                clicks.map {
                                    this@TreeView.store.identifier(treeItem.item)
                                } handledBy this@TreeView.tus.update
                            }
                        }
                        span(baseClass = "tree-view".component("node", "text")) {
                            +treeItem.item.toString()
                        }
                    }
                }
                if (treeItem.children.isNotEmpty() && treeItem.expanded) {
                    ul {
                        attr("role", "group")
                        for (childItem in treeItem.children) {
                            this@TreeView.renderTreeItem(this@ul, childItem)
                        }
                    }
                }
            }
        }
        return li
    }
}

// ------------------------------------------------------ store

public class TreeStore<T>(public val identifier: IdProvider<T, String>) :
    RootStore<TreeItems<T>>(TreeItems(identifier)) {

    public val rootItems: Flow<List<TreeItem<T>>> = data.map { it.rootItems }
}

internal class TreeUpdateStore() : RootStore<String>("", dropInitialData = true) {
}

// ------------------------------------------------------ type

public class TreeItems<T>(public val identifier: IdProvider<T, String>) {

    private val _rootItems: MutableList<TreeItem<T>> = mutableListOf()

    public val rootItems: List<TreeItem<T>>
        get() = _rootItems

    public fun add(treeItem: TreeItem<T>): Boolean = _rootItems.add(treeItem)

    public fun find(item: T): TreeItem<T>? {
        var result: TreeItem<T>? = null
        val iterator = _rootItems.iterator()
        while (iterator.hasNext() && result == null) {
            val rootItem = iterator.next()
            result = rootItem.find(item, identifier)
        }
        return result
    }
}

public class TreeItem<T>(
    override val item: T,
    public var expanded: Boolean,
    public var selected: Boolean,
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
        get() = _children.isNotEmpty()

    public fun addChild(treeItem: TreeItem<T>) {
        treeItem._parent = this
        _children.add(treeItem)
    }

    public fun find(item: T, identifier: IdProvider<T, String>): TreeItem<T>? {
        var result: TreeItem<T>? = null
        if (identifier(item) == identifier(this.item)) {
            result = this
        } else {
            val iterator = _children.iterator()
            while (iterator.hasNext() && result == null) {
                val child = iterator.next()
                result = find(child.item, identifier)
            }
        }
        return result
    }

    public fun all(predicate: (TreeItem<T>) -> Boolean): Boolean {
        return if (predicate(this)) {
            for (child in _children) {
                if (!child.all(predicate)) {
                    return false
                }
            }
            true
        } else {
            false
        }
    }

    override fun toString(): String = "TreeItem(item=$item, expanded=$expanded, selected=$selected)"

    internal fun treeId(identifier: IdProvider<T, String>): String = buildString {
        append(identifier(item))
        append(if (expanded) "1" else "0")
        append(if (selected) "1" else "0")
        for (child in children) {
            append(child.treeId(identifier))
        }
    }
}

// ------------------------------------------------------ builder

public class TreeItemsBuilder<T> {
    internal val builders: MutableList<TreeItemBuilder<T>> = mutableListOf()

    internal fun build(identifier: IdProvider<T, String>): TreeItems<T> {
        val treeItems = TreeItems(identifier)
        for (builder in builders) {
            treeItems.add(builder.build())
        }
        return treeItems
    }
}

public class TreeItemBuilder<T>(private val item: T) {
    public var expanded: Boolean = false
    public var selected: Boolean = false
    public var icon: (Span.() -> Unit)? = null
    public var expandedIcon: (Span.() -> Unit)? = null
    internal val treeItemsBuilder: TreeItemsBuilder<T> = TreeItemsBuilder()

    internal fun build(): TreeItem<T> {
        val treeItem = TreeItem(item, expanded, selected, icon, expandedIcon)
        for (builder in treeItemsBuilder.builders) {
            val child = builder.build()
            treeItem.addChild(child)
        }
        return treeItem
    }
}
