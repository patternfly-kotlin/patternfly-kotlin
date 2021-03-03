@file:Suppress("TooManyFunctions")

package org.patternfly

/**
 * Creates a PatternFly component CSS class starting with `pf-c-`. Additional elements are appended to the component class using a modified version of [BEM](http://getbem.com/introduction/).
 *
 * @receiver the component name
 *
 * @sample org.patternfly.sample.CssSample.component
 */
public fun String.component(vararg elements: String): String = combine("pf-c", this, elements)

/**
 * Creates a PatternFly layout CSS class starting with `pf-l-`. Additional elements are appended to the component class using a modified version of [BEM](http://getbem.com/introduction/).
 *
 * @receiver the layout name
 *
 * @see [component]
 * @see <a href="https://www.patternfly.org/v4/layouts/bullseye/">https://www.patternfly.org/v4/layouts/bullseye/</a>
 */
public fun String.layout(vararg elements: String): String = combine("pf-l", this, elements)

/**
 * Creates a PatternFly modifier CSS class starting with `pf-m-`.
 *
 * @receiver the modifier name
 */
public fun String.modifier(): String = "pf-m-$this"

/**
 * Creates a PatternFly utility CSS class starting with `pf-u-`.
 *
 * @receiver the utility name
 *
 * @see <a href="https://www.patternfly.org/v4/utilities/accessibility">https://www.patternfly.org/v4/utilities/accessibility</a>
 */
public fun String.util(): String = "pf-u-$this"

/**
 * Creates a Font Awesome regular CSS class starting with `far fa-`.
 */
public fun String.far(): String = "far fa-$this"

/**
 * Creates a Font Awesome solid CSS class starting with `fas fa-`.
 */
public fun String.fas(): String = "fas fa-$this"

/**
 * Creates a PatternFly icon CSS class starting with `pf-icon-`.
 */
public fun String.pfIcon(): String = "pf-icon-$this"

private fun combine(prefix: String, main: String, elements: Array<out String>): String = buildString {
    append("$prefix-$main")
    if (elements.isNotEmpty()) elements.joinTo(this, "-", "__")
}
