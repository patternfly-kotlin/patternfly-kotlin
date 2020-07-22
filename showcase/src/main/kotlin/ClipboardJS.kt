import org.w3c.dom.Element

@JsNonModule
@JsModule("clipboard")
external class ClipboardJS(element: Element, options: Options = definedExternally) {

    fun on(type: String /* "success" | "error" */, handler: (e: Event) -> Unit): ClipboardJS /* this */
    fun destroy()

    interface Options {
        val action: ((elem: Element) -> String)?
            get() = definedExternally
        val target: ((elem: Element) -> Element)?
            get() = definedExternally
        val text: ((elem: Element) -> String)?
            get() = definedExternally
        var container: Element?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface Event {
        var action: String
        var text: String
        var trigger: Element
        fun clearSelection()
    }

    companion object {
        fun isSupported(): Boolean
    }
}
