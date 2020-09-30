package org.patternfly

import dev.fritz2.binding.const
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Img
import org.w3c.dom.HTMLImageElement

// ------------------------------------------------------ dsl

fun Header.pfBrandContainer(
    id: String? = null,
    classes: String? = null,
    content: BrandContainer.() -> Unit = {}
): BrandContainer = register(BrandContainer(id = id, classes = classes("page".component("header", "brand"), classes)), content)

fun BrandContainer.pfBrandLink(
    homeLink: String,
    id: String? = null,
    classes: String? = null,
    content: A.() -> Unit = {}
): A = register(A(id = id, baseClass = classes("page".component("header", "brand", "link"), classes)).apply {
        href = const(homeLink)
    }, content)

fun HtmlElements.pfBrand(
    src: String,
    id: String? = null,
    classes: String? = null,
    content: Img.() -> Unit = {}
): Brand = register(Brand(src = src, id = id, classes = classes), content)

// ------------------------------------------------------ tag

class Brand internal constructor(src: String, id: String?, classes: String?) :
    PatternFlyComponent<HTMLImageElement>, Img(id = id, baseClass = classes(ComponentType.Brand, classes)) {

    init {
        markAs(ComponentType.Brand)
        this.src = const(src)
    }
}

class BrandContainer(id: String?, classes: String?) : Div(id = id, baseClass = classes)
