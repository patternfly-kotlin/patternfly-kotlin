package org.patternfly

/**
 * Combines the specified classes using " " as the separator.
 *
 * @sample org.patternfly.sample.BemSample.classesDsl
 */
public fun classes(builderAction: ClassBuilder.() -> Unit): String? =
    ClassBuilder().apply { builderAction() }.build()

/**
 * Combines the specified classes using " " as the separator.
 *
 * @sample org.patternfly.sample.BemSample.classesVararg
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
 * @sample org.patternfly.sample.BemSample.classesDsl
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
     * @sample org.patternfly.sample.BemSample.classesDsl
     */
    public infix fun String.`when`(condition: Boolean): String? = if (condition) this else null

    internal operator fun ComponentType.unaryPlus() {
        this.baseClass?.let {
            builder.append(it).append(" ")
        }
    }

    internal fun build(): String? = builder.toString().trim().ifEmpty { null }
}
