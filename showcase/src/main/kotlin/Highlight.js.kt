@file:JsModule("highlight.js")
@file:JsNonModule

package hljs

import org.w3c.dom.Node

external fun highlightBlock(block: Node)
