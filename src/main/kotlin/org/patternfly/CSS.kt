package org.patternfly

public fun String.component(vararg elements: String): String = combine("pf-c", this, elements)

public fun String.layout(vararg elements: String): String = combine("pf-l", this, elements)

public fun String.modifier(): String = "pf-m-$this"

public fun String.util(): String = "pf-u-$this"

public fun String.fas(): String = "fas fa-$this"

public fun String.pfIcon(): String = "pficon pf-$this"

private fun combine(prefix: String, main: String, elements: Array<out String>): String = buildString {
    append("$prefix-$main")
    if (elements.isNotEmpty()) elements.joinTo(this, "-", "__")
}

public inline fun classes(builderAction: ClassBuilder.() -> Unit): String? = ClassBuilder().apply { builderAction() }.build()

public fun classes(vararg classes: String): String? = classes.joinToString(" ").ifEmpty { null }

internal fun classes(componentType: ComponentType, optionalClass: String? = null): String? =
    classes(componentType.baseClass, optionalClass)

internal fun classes(baseClass: String?, optionalClass: String? = null): String? = buildString {
    baseClass?.let { append(it).append(" ") }
    optionalClass?.let { append(it) }
}.trim().ifEmpty { null }

public class ClassBuilder {

    private val builder = StringBuilder()

    public operator fun String?.unaryPlus() {
        this?.let {
            builder.append(it).append(" ")
        }
    }

    public infix fun String.`when`(condition: Boolean): String? = if (condition) this else null

    internal operator fun ComponentType.unaryPlus() {
        this.baseClass?.let {
            builder.append(it).append(" ")
        }
    }

    public fun build(): String? = builder.toString().trim().ifEmpty { null }
}
