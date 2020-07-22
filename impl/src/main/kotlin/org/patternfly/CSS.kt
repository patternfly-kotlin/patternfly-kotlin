package org.patternfly

fun String.component(vararg elements: String): String = combine("pf-c", this, elements)

fun String.layout(vararg elements: String): String = combine("pf-l", this, elements)

fun String.modifier(): String = "pf-m-$this"

fun String.util(): String = "pf-u-$this"

fun String.fas() = "fas fa-$this"

fun String.pfIcon() = "pf-icon pf-icon-$this"

private fun combine(prefix: String, main: String, elements: Array<out String>): String = buildString {
    append("$prefix-$main")
    if (elements.isNotEmpty()) elements.joinTo(this, "-", "__")
}

@Suppress("EnumEntryName")
enum class Modifier(val value: String) {

    _4xl("4xl".modifier()),
    _3xl("3xl".modifier()),
    _2xl("2xl".modifier()),
    ariaDisabled("aria-disabled".modifier()),
    block("block".modifier()),
    control("control".modifier()),
    current("current".modifier()),
    danger("danger".modifier()),
    disabled("disabled".modifier()),
    displayLg("display-lg".modifier()),
    end("end".modifier()),
    expanded("expanded".modifier()),
    expandable("expandable".modifier()),
    horizontal("horizontal".modifier()),
    info("info".modifier()),
    `inline`("inline".modifier()),
    lg("lg".modifier()),
    light("light".modifier()),
    link("link".modifier()),
    md("md".modifier()),
    plain("plain".modifier()),
    primary("primary".modifier()),
    read("read".modifier()),
    secondary("secondary".modifier()),
    selectable("selectable".modifier()),
    small("small".modifier()),
    start("start".modifier()),
    success("success".modifier()),
    tertiary("tertiary".modifier()),
    toast("toast".modifier()),
    unread("unread".modifier()),
    warning("warning".modifier()),
    xl("xl".modifier());

    override fun toString(): String = value
}
