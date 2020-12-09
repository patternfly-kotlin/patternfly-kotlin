package org.patternfly.dom

/**
 * Typesafe CSS selector API.
 *
 * Use the methods in this class to create arbitrary complex CSS selectors:
 *
 * ```
 * #main [data-list-item=foo] a[href^="http://"] > .fas.fa-check, .external[hidden]
 * ```
 *
 * @sample org.patternfly.dom.BySample.complex
 */
public interface By {

    public val selector: String

    /**
     * Combines this selector with the given selector. Use this method to express selectors like `button.primary` or
     * `input[type=checkbox]`
     *
     * @sample org.patternfly.dom.BySample.and
     */
    public fun and(selector: By): By = combinator(Combinator.AND, selector)

    /**
     * Combines this selector with the given selector using the `>` (child) combinator. Selects nodes that are
     * direct children of this element.
     */
    public fun child(selector: By): By = combinator(Combinator.CHILD, selector)

    /**
     * Combines this selector with the given selector using the (space) combinator. Selects nodes that are descendants
     * of this element.
     */
    public fun desc(selector: By): By = combinator(Combinator.DESCENDANT, selector)

    /**
     * Combines this selector with the given selector using the `~` (general sibling) combinator. This means that
     * `selector` follows this element (though not necessarily immediately), and both share the same parent.
     */
    public fun sibling(selector: By): By = combinator(Combinator.SIBLING, selector)

    /**
     * Combines this selector with the given selector using the `+` (adjacent sibling combinator) combinator. This
     * means that `selector` directly follows this element, and both share the same parent.
     */
    public fun adjacentSibling(selector: By): By = combinator(Combinator.ADJACENT_SIBLING, selector)

    private fun combinator(combinator: Combinator, selector: By): By {
        return ByCombination(this, combinator, selector)
    }

    // ------------------------------------------------------ factory methods

    public companion object {
        /** Returns a selector as-is. */
        public fun selector(selector: String): By = BySelector(selector)

        /** Selects an element based on the value of its id attribute. */
        public fun id(id: String): By = ById(id)

        /** Selects elements that have the given element name. */
        public fun element(element: String): By = ByElement(element)

        /** Selects elements that have all of the given class attributes. */
        public fun classname(vararg classname: String): By = ByClassname(classname)

        /** Selects elements that have an attribute name of [name]. */
        public fun attribute(name: String): By = ByAttribute(name, null, null)

        /**
         * Selects all elements that have an attribute name of [name] whose value applies to the given operator.
         *
         * You don't need to enclose the value in quotes. If necessary, quotes are added automatically
         * (see [https://mothereff.in/unquoted-attributes](https://mothereff.in/unquoted-attributes)).
         */
        public fun attribute(name: String, value: String, operator: AttributeOperator = AttributeOperator.EQUALS): By =
            ByAttribute(name, operator, value)

        /**
         * Selects elements that have an attribute name of data-[name].
         *
         * If [name] contains "-" it is used as is, otherwise it is expected to be in camelCase and is converted to
         * kebab-case.
         */
        public fun data(name: String): By = ByData(name, null, null)

        /**
         * Selects elements that have an attribute name of data-[name] whose value applies to the given operator.
         *
         * If [name] contains "-" it is used as is, otherwise it is expected to be in camelCase and is converted to
         * kebab-case.
         *
         * You don't need to enclose the value in quotes. If necessary, quotes are added automatically
         * (see [https://mothereff.in/unquoted-attributes](https://mothereff.in/unquoted-attributes)).
         */
        public fun data(name: String, value: String, operator: AttributeOperator = AttributeOperator.EQUALS): By =
            ByData(name, operator, value)

        /** Groups the specified selectors using `,`. */
        public fun group(vararg selectors: By): By = ByGroup(selectors)
    }
}

// ------------------------------------------------------ by implementations (a-z)

private abstract class ByBase : By {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as By
        if (selector != other.selector) return false
        return true
    }

    override fun hashCode(): Int {
        return selector.hashCode()
    }
}

private open class ByAttribute(
    private val name: String,
    private val operator: AttributeOperator?,
    private val value: String?
) : ByBase(), By {

    override val selector: String
        get(): String = buildString {
            append("[").append(name)
            if (value != null && value.isNotEmpty() && operator != null) {
                append(operator.operator)
                val unquotable = isUnquotableCSS(value)
                if (!unquotable) {
                    append("\"")
                }
                append(value)
                if (!unquotable) {
                    append("\"")
                }
            }
            append("]")
        }

    // Taken from https://mothereff.in/unquoted-attributes
    private fun isUnquotableCSS(value: String): Boolean {
        if (value == "" || value == "-") {
            return false
        }
        val r0 = """\\([0-9A-Fa-f]{1,6})[ \t\n\f\r]?""".toRegex()
        val r1 = """\\.""".toRegex()
        val replaced = value
            .replace(r0, "a")
            .replace(r1, "a")

        val m0 = """[\u0000-\u002c\u002e\u002f\u003A-\u0040\u005B-\u005E\u0060\u007B-\u009f]""".toRegex()
        val m1 = """^(?:-?\d|--)""".toRegex()
        return !(m0.containsMatchIn(replaced) || m1.containsMatchIn(replaced))
    }
}

private class ByClassname(private val classnames: Array<out String>) : ByBase(), By {
    override val selector: String
        get() = classnames.joinToString(".", ".")
}

private class ByCombination(private val left: By, private val combinator: Combinator, private val right: By) :
    ByBase(), By {
    override val selector: String
        get() = left.selector + combinator.operator + right.selector
}

private class ByData(name: String, operator: AttributeOperator?, value: String?) :
    ByAttribute("data-" + if (name.contains("-")) name else camelToKebabCase(name), operator, value) {

    companion object {
        private fun camelToKebabCase(str: String): String {
            // from https://codepen.io/wpatter6/pen/wvweWZa
            return str.replace("([a-z0-9]|(?=[A-Z]))([A-Z])".toRegex(), "$1-$2").toLowerCase()
        }
    }
}

private class ByElement(element: String) : ByBase(), By {
    override val selector: String = element
}

private class ByGroup(private val selectors: Array<out By>) : ByBase(), By {
    override val selector: String
        get() = selectors.joinToString(", ") { it.selector }
}

private class ById(private val id: String) : ByBase(), By {
    override val selector: String
        get() = "#$id"
}

private class BySelector(override val selector: String) : ByBase(), By

// ------------------------------------------------------ enums

/** Operator used for attribute selectors.  */
public enum class AttributeOperator(internal val operator: String) {
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
