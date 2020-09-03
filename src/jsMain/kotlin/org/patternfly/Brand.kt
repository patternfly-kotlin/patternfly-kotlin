package org.patternfly

import dev.fritz2.binding.const
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Img
import org.w3c.dom.HTMLImageElement

// ------------------------------------------------------ dsl

fun Header.pfBrandContainer(classes: String? = null, content: BrandContainer.() -> Unit = {}): BrandContainer =
    register(BrandContainer(classes("page".component("header", "brand"), classes)), content)

fun Header.pfBrandContainer(modifier: Modifier, content: BrandContainer.() -> Unit = {}): BrandContainer =
    register(BrandContainer(classes("page".component("header", "brand"), modifier.value)), content)

fun BrandContainer.pfBrandLink(homeLink: String, classes: String? = null, content: A.() -> Unit = {}): A =
    register(A(baseClass = classes("page".component("header", "brand", "link"), classes)).apply {
        href = const(homeLink)
    }, content)

fun BrandContainer.pfBrandLink(homeLink: String, modifier: Modifier, content: A.() -> Unit = {}): A =
    register(A(baseClass = classes("page".component("header", "brand", "link"), modifier.value)).apply {
        href = const(homeLink)
    }, content)

fun HtmlElements.pfBrand(src: String, classes: String? = null, content: Img.() -> Unit = {}): Brand =
    register(Brand(src, classes), content)

fun HtmlElements.pfBrand(src: String, modifier: Modifier, content: Img.() -> Unit = {}): Brand =
    register(Brand(src, modifier.value), content)

// ------------------------------------------------------ tag

class Brand internal constructor(src: String, classes: String?) :
    PatternFlyComponent<HTMLImageElement>, Img(baseClass = classes(ComponentType.Brand, classes)) {

    init {
        markAs(ComponentType.Brand)
        this.src = const(src)
    }
}

class BrandContainer(classes: String?) : Div(baseClass = classes)
