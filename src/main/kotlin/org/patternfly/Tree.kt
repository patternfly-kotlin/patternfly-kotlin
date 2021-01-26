@file:Suppress("TooManyFunctions")

package org.patternfly

// ------------------------------------------------------ dsl

public fun <T> tree(block: TreeBuilder<T>.() -> Unit = {}): Tree<T> = TreeBuilder<T>().apply(block).build()

public fun <T> treeItem(item: T, block: TreeItemBuilder<T>.() -> Unit = {}): TreeItem<T> =
    TreeItemBuilder(item).apply(block).build()

public fun <T> TreeView<T>.tree(block: TreeBuilder<T>.() -> Unit = {}) {
    initTree(TreeBuilder<T>().apply(block).build())
}

public fun <T> TreeBuilder<T>.treeItem(item: T, block: TreeItemBuilder<T>.() -> Unit = {}) {
    val builder = TreeItemBuilder(item).apply(block)
    builders.add(builder)
}

public fun <T> TreeItemBuilder<T>.children(block: TreeBuilder<T>.() -> Unit = {}) {
    childrenBuilder.apply(block)
}

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

public class TreeItem<T>(override val item: T) : HasItem<T> {
    private var _parent: TreeItem<T>? = null
    private val _children: MutableList<TreeItem<T>> = mutableListOf()
    internal var fetched: Boolean = false

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
    internal val childrenBuilder: TreeBuilder<T> = TreeBuilder()

    internal fun build(): TreeItem<T> {
        val treeItem = TreeItem(item)
        for (cb in childrenBuilder.builders) {
            val child = cb.build()
            treeItem.addChild(child)
        }
        return treeItem
    }
}
