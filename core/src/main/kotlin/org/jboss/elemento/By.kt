package org.jboss.elemento

import org.w3c.dom.Element

/**
 * Typesafe CSS selector API.
 *
 * Use the methods in this class to create arbitrary complex CSS selectors:
 *
 * ```
 * #main [data-list-item=foo] a[href^="http://"] > .fas.fa-check, .external[hidden]
 * ```
 *
 * ```
 * By.group(
 *         By.id("main")
 *                 .desc(By.data("listItem", "foo")
 *                         .desc(By.element("a").and(By.attribute("href", STARTS_WITH, "http://"))
 *                                 .child(By.classnames("fas", "fa-check")))),
 *         By.classname("external").and(By.attribute("hidden")));
 * ```
 */
interface By {
    val selector: String

    /**
     * Combines this selector with the given selector. Use this method to express selectors like `button.primary` or
     * `input[type=checkbox]`:
     *
     * ```
     * By.element("button").and(By.classname("primary"))
     * By.element("input").and(By.attribute("type", "checkbox"));
     * ```
     */
    fun and(selector: By): By = combinator(Combinator.AND, selector)

    /**
     * Combines this selector with the given selector using the `>` (child) combinator. Selects nodes that are
     * direct children of this element.
     */
    fun child(selector: By): By = combinator(Combinator.CHILD, selector)

    /**
     * Combines this selector with the given selector using the (space) combinator. Selects nodes that are descendants
     * of this element.
     */
    fun desc(selector: By): By = combinator(Combinator.DESCENDANT, selector)

    /**
     * Combines this selector with the given selector using the `~` (general sibling) combinator. This means that
     * `selector` follows this element (though not necessarily immediately), and both share the same parent.
     */
    fun sibling(selector: By): By = combinator(Combinator.SIBLING, selector)

    /**
     * Combines this selector with the given selector using the `+` (adjacent sibling combinator) combinator. This
     * means that `selector` directly follows this element, and both share the same parent.
     */
    fun adjacentSibling(selector: By): By = combinator(Combinator.ADJACENT_SIBLING, selector)

    private fun combinator(combinator: Combinator, selector: By): By {
        return ByCombination(this, combinator, selector)
    }

    // ------------------------------------------------------ factory methods

    companion object {
        /** Returns a selector as-is. */
        fun selector(selector: String): By = BySelector(selector)

        /** Selects an element based on the value of its id attribute. */
        fun id(id: String): By = ById(id)

        /** Selects elements that have the given element name. */
        fun element(element: String): By = ByElement(element)

        /** Selects elements that have the given element name. */
        fun element(element: Element): By = ByElement(element.tagName.toLowerCase())

        /** Selects elements that have all of the given class attributes. */
        fun classname(vararg classname: String): By = ByClassname(classname)

        /** Selects elements that have an attribute name of [name]. */
        fun attribute(name: String): By = ByAttribute(name, null, null)

        /**
         * Selects all elements that have an attribute name of [name] whose value applies to the given operator.
         *
         * You don't need to enclose the value in quotes. If necessary, quotes are added automatically
         * (see [https://mothereff.in/unquoted-attributes](https://mothereff.in/unquoted-attributes)).
         */
        fun attribute(name: String, value: String, operator: AttributeOperator = AttributeOperator.EQUALS): By =
            ByAttribute(name, operator, value)

        /**
         * Selects elements that have an attribute name of data-[name].
         *
         * If [name] contains "-" it is used as is, otherwise it is expected to be in camelCase and is converted to
         * kebab-case.
         */
        fun data(name: String): By = ByData(name, null, null)

        /**
         * Selects elements that have an attribute name of data-[name] whose value applies to the given operator.
         *
         * If [name] contains "-" it is used as is, otherwise it is expected to be in camelCase and is converted to
         * kebab-case.
         *
         * You don't need to enclose the value in quotes. If necessary, quotes are added automatically
         * (see [https://mothereff.in/unquoted-attributes](https://mothereff.in/unquoted-attributes)).
         */
        fun data(name: String, value: String, operator: AttributeOperator = AttributeOperator.EQUALS): By =
            ByData(name, operator, value)

        /** Groups the specified selectors using `,`. */
        fun group(selectors: Array<By>): By? = ByGroup(selectors)
    }
}

// ------------------------------------------------------ by implementations (a-z)

private open class ByAttribute(private val name: String, private val operator: AttributeOperator?, private val value: String?) :
    By {

    override val selector: String
        get(): String = buildString {
            append("[").append(name)
            if (value != null && value.isNotEmpty() && operator != null) {
                append(operator.operator)
                val needsQuotes: Boolean = needsQuotes(value)
                if (needsQuotes) {
                    append("\"")
                }
                append(value)
                if (needsQuotes) {
                    append("\"")
                }
            }
            append("]")
        }

    // Taken from https://mothereff.in/unquoted-attributes
    private fun needsQuotes(value: String): Boolean {
        if (value == "" || value == "-") return false

        val r0 = """\\([0-9A-Fa-f]{1,6})[ \t\n\f\r]?""".toRegex()
        val r1 = """\\.""".toRegex()
        val replaced = value
            .replace(r0, "a")
            .replace(r1, "a")

        val m0 = """[\00-\x2C\x2E\x2F\x3A-\x40\x5B-\x5E\x60\x7B-\x9F]""".toRegex()
        val m1 = """^-?\d""".toRegex()
        return !(m0.matches(replaced) || m1.matches(replaced))
    }
}

private class ByClassname(private val classnames: Array<out String>) : By {
    override val selector: String
        get() = classnames.joinToString(".", ".")
}

private class ByCombination(private val left: By, private val combinator: Combinator, private val right: By) : By {
    override val selector: String
        get() = left.selector + combinator.operator + right.selector
}

private class ByData(private val name: String, private val operator: AttributeOperator?, private val value: String?) :
    ByAttribute("data-" + if (name.contains("-")) name else camelToKebabCase(name), operator, value) {

    companion object {
        private fun camelToKebabCase(str: String): String {
            // from https://codepen.io/wpatter6/pen/wvweWZa
            return str.replace("([a-z0-9]|(?=[A-Z]))([A-Z])".toRegex(), "$1-$2").toLowerCase()
        }
    }
}

private class ByElement(private val element: String) : By {
    override val selector: String = element
}

private class ByGroup(private val selectors: Array<By>) : By {
    override val selector: String
        get() = selectors.joinToString(", ") { it.selector }
}

private class ById(private val id: String) : By {
    override val selector: String
        get() = "#$id"
}

private class BySelector(override val selector: String) : By

// ------------------------------------------------------ enums

/** Operator used for attribute selectors.  */
enum class AttributeOperator(internal val operator: String) {
    /**
     * `[attr=value]`
     * Represents elements with an attribute name of attr whose value is exactly value.
     */
    EQUALS("="),

    /**
     * `[attr^=value]`: Represents elements with an attribute name of attr whose value is prefixed (preceded)
     * by value.
     */
    STARTS_WITH("^="),

    /**
     * `[attr$=value]`: Represents elements with an attribute name of attr whose value is suffixed (followed)
     * by value.
     */
    ENDS_WITH("$="),

    /**
     * `[attr*=value]`: Represents elements with an attribute name of attr whose value contains at least one
     * occurrence of value within the string.
     */
    CONTAINS("*="),

    /**
     * `[attr~=value]`: Represents elements with an attribute name of attr whose value is a
     * whitespace-separated list of words, one of which is exactly value.
     */
    CONTAINS_WORD("~="),

    /**
     * `[attr|=value]`: Represents elements with an attribute name of attr whose value can be exactly value or
     * can begin with value immediately followed by a hyphen, - (U+002D). It is often used for language subcode
     * matches.
     */
    CONTAINS_TOKEN("|=");
}

private enum class Combinator(val operator: String) {
    AND(""),
    DESCENDANT(" "),
    CHILD(" > "),
    ADJACENT_SIBLING(" + "),
    SIBLING(" ~ ");
}
