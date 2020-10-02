package org.patternfly

fun String.component(vararg elements: String): String = combine("pf-c", this, elements)

fun String.layout(vararg elements: String): String = combine("pf-l", this, elements)

fun String.modifier(): String = "pf-m-$this"

fun String.util(): String = "pf-u-$this"

fun String.fas() = "fas fa-$this"

fun String.pfIcon() = "pficon pf-$this"

private fun combine(prefix: String, main: String, elements: Array<out String>): String = buildString {
    append("$prefix-$main")
    if (elements.isNotEmpty()) elements.joinTo(this, "-", "__")
}

inline fun classes(builderAction: ClassBuilder.() -> Unit): String? = ClassBuilder().apply { builderAction() }.build()

fun classes(vararg classes: String): String? = classes.joinToString(" ").ifEmpty { null }

internal fun classes(componentType: ComponentType, optionalClass: String? = null): String? =
    classes(componentType.baseClass, optionalClass)

internal fun classes(baseClass: String?, optionalClass: String? = null): String? = buildString {
    baseClass?.let { append(it).append(" ") }
    optionalClass?.let { append(it) }
}.trim().ifEmpty { null }

class ClassBuilder {

    private val builder = StringBuilder()

    operator fun String?.unaryPlus() {
        this?.let {
            builder.append(it).append(" ")
        }
    }

    infix fun String.`when`(condition: Boolean): String? = if (condition) this else null

    internal operator fun ComponentType.unaryPlus() {
        this.baseClass?.let {
            builder.append(it).append(" ")
        }
    }

    fun build(): String? = builder.toString().trim().ifEmpty { null }
}
