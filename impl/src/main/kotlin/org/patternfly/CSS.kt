package org.patternfly

fun String.component(vararg elements: String): String = combine("pf-c", this, *elements)

fun String.layout(vararg elements: String): String = combine("pf-l", this, *elements)

fun String.modifier(): String = "pf-m-$this"

fun String.util(): String = "pf-u-$this"

fun String.fas() = "fas fa-$this"

fun String.pfIcon() = "pf-icon pf-icon-$this"

fun String.append(vararg classes: String) = buildString {
    append(this@append)
    if (classes.isNotEmpty()) classes.joinTo(this, " ", " ")
}

private fun combine(prefix: String, main: String, vararg elements: String): String = buildString {
    append("$prefix-$main")
    if (elements.isNotEmpty()) elements.joinTo(this, "-", "__")
}
