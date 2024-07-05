package com.example.StockDrinks.Controller

import android.content.Context

class Tree(val obj: Any) {  // Changed name to obj and type to Any
    private var value: Int
        get() = values[obj] ?: 0  // Updated to use obj
        set(value) {
            values[obj] = value  // Updated to use obj
        }

    private var superNode: Tree?
        get() = superNodes[obj]
        set(value) {
            superNodes[obj] = value
        }

    companion object {
        private val leafs = mutableSetOf<Tree>()
        private val subNodes = mutableMapOf<Any, MutableSet<Tree>>()  // Updated to use Any
        private val superNodes = mutableMapOf<Any, Tree?>()  // Updated to use Any
        private var values = mutableMapOf<Any, Int>()  // Updated to use Any
        private var isLeaf = mutableMapOf<Any, Boolean>()  // Updated to use Any
    }

    init {
        if (obj.toString().isNotEmpty()) {
            leafs.add(this)
            isLeaf[obj] = true
        } else {
            resetTree()
        }
    }

    fun setValueInternal(value: Int) {
        values[obj] = value  // Updated to use obj
    }

    private fun resetTree() {
        leafs.clear()
        subNodes.clear()
        superNodes.clear()
        values.clear()
        isLeaf.clear()
    }

    fun addNode(subTree: Tree) {
        if (obj.toString().isNotEmpty()) {
            subTree.superNode = this
            if (!subNodes.containsKey(this.obj)) {
                subNodes[this.obj] = mutableSetOf()
            }
            subNodes[this.obj]!!.add(subTree)
            isLeaf[this.obj] = false
            leafs.remove(this)
        }
    }

    fun getLeafs(): Set<Tree> {
        return leafs
    }

    private fun sumChildren() {
        var sum = 0
        subNodes[obj]?.forEach { child -> sum += child.value }
        values[obj] = sum
    }

    private fun sumNodes(nodeList: Set<Tree>) {
        val newNodeList = mutableSetOf<Tree>()
        nodeList.forEach { node ->
            node.superNode?.let { superNode ->
                if (superNode !in newNodeList) {
                    superNode.sumChildren()
                    newNodeList.add(superNode)
                }
            }
        }
        if (newNodeList.isNotEmpty()) {
            sumNodes(newNodeList)
        }
    }

    fun sumAllNodes() {
        sumNodes(leafs)
    }

    fun searchTree(nodeName: Any): Tree? {  // Updated to use Any
        val visitedNodes = mutableSetOf<Tree>()
        fun searchNodes(nodeList: Set<Tree>): Tree? {
            val newNodeList = mutableSetOf<Tree>()
            nodeList.forEach { node ->
                if (node.obj == nodeName) {  // Updated to use obj
                    return node
                }
                node.superNode?.let { superNode ->
                    if (superNode !in newNodeList && superNode !in visitedNodes) {
                        newNodeList.add(superNode)
                    }
                }
                visitedNodes.add(node)
            }
            return if (newNodeList.isNotEmpty()) {
                searchNodes(newNodeList)
            } else {
                null
            }
        }
        return searchNodes(leafs)
    }

    override fun toString(): String {
        var currentNode: Tree? = this
        var result = ""
        while (currentNode != null) {
            result = "-> ${currentNode.obj}:${currentNode.value} $result"  // Updated to use obj
            currentNode = currentNode.superNode
        }
        return result.trimStart('-').trim() + "\n"
    }

    fun toString(context: Context): String {
        var currentNode: Tree? = this
        var result = ""
        while (currentNode != null) {
            result = "-> ${context.getString(currentNode.obj as Int)}:${currentNode.value} $result"  // Updated to use obj
            currentNode = currentNode.superNode
        }
        return result.trimStart('-').trim() + "\n"
    }
}
