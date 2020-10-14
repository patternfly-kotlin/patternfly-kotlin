package org.patternfly

import dev.fritz2.binding.const
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Img
import org.w3c.dom.HTMLImageElement

// ------------------------------------------------------ dsl

public fun Header.pfBrandContainer(
    id: String? = null,
    baseClass: String? = null,
    content: BrandContainer.() -> Unit = {}
): BrandContainer =
    register(BrandContainer(id = id, baseClass = classes("page".component("header", "brand"), baseClass)), content)

public fun BrandContainer.pfBrandLink(
    homeLink: String,
    id: String? = null,
    baseClass: String? = null,
    content: A.() -> Unit = {}
): A = register(A(id = id, baseClass = classes("page".component("header", "brand", "link"), baseClass)).apply {
    href = const(homeLink)
}, content)

public fun HtmlElements.pfBrand(
    src: String,
    id: String? = null,
    baseClass: String? = null,
    content: Img.() -> Unit = {}
): Brand = register(Brand(src = src, id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

public class Brand internal constructor(src: String, id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLImageElement>, Img(id = id, baseClass = classes(ComponentType.Brand, baseClass)) {

    init {
        markAs(ComponentType.Brand)
        this.src = const(src)
    }
}

public class BrandContainer internal constructor(id: String?, baseClass: String?) : Div(id = id, baseClass = baseClass)
