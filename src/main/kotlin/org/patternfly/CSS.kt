package org.patternfly

/**
 * Creates a PatternFly component CSS class starting with `pf-c-`. Additional elements are appended to the component class using a modified version of [BEM](http://getbem.com/introduction/).
 *
 * @sample org.patternfly.CssSample.component
 */
public fun String.component(vararg elements: String): String = combine("pf-c", this, elements)

/**
 * Creates a PatternFly layout CSS class starting with `pf-l-`.
 */
public fun String.layout(vararg elements: String): String = combine("pf-l", this, elements)

/**
 * Creates a PatternFly modifier CSS class starting with `pf-m-`.
 */
public fun String.modifier(): String = "pf-m-$this"

/**
 * Creates a PatternFly utility CSS class starting with `pf-u-`.
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

/**
 * Combines the specified classes using " " as the separator.
 *
 * @sample org.patternfly.CssSample.classesDsl
 */
public fun classes(builderAction: ClassBuilder.() -> Unit): String? =
    ClassBuilder().apply { builderAction() }.build()

/**
 * Combines the specified classes using " " as the separator.
 *
 * @sample org.patternfly.CssSample.classesVararg
 */
public fun classes(vararg classes: String): String? = classes.joinToString(" ").ifEmpty { null }

internal fun classes(componentType: ComponentType, optionalClass: String? = null): String? =
    classes(componentType.baseClass, optionalClass)

internal fun classes(baseClass: String?, optionalClass: String? = null): String? = buildString {
    baseClass?.let { append(it).append(" ") }
    optionalClass?.let { append(it) }
}.trim().ifEmpty { null }

/**
 * DSL for combining CSS classes.
 *
 * @sample org.patternfly.CssSample.classesDsl
 */
public class ClassBuilder {

    private val builder = StringBuilder()

    /**
     * Adds the specified CSS class.
     */
    public operator fun String?.unaryPlus() {
        this?.let {
            builder.append(it).append(" ")
        }
    }

    /**
     * Adds the specified CSS class if the `condition == true`.
     *
     * @sample org.patternfly.CssSample.classesDsl
     */
    public infix fun String.`when`(condition: Boolean): String? = if (condition) this else null

    internal operator fun ComponentType.unaryPlus() {
        this.baseClass?.let {
            builder.append(it).append(" ")
        }
    }

    internal fun build(): String? = builder.toString().trim().ifEmpty { null }
}
