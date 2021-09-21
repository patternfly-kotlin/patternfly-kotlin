package org.patternfly

import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.Scope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.By
import org.patternfly.dom.aria
import org.patternfly.dom.querySelector
import org.patternfly.dom.removeFromParent

// ------------------------------------------------------ dsl

/**
 * Creates the [Brand] component inside the [PageHeader] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun PageHeader.brand(
    id: String? = null,
    baseClass: String? = null,
    content: Brand.() -> Unit = {}
): Brand = register(Brand(this.page.sidebarStore, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * [PatternFly brand](https://www.patternfly.org/v4/components/page/design-guidelines) component.
 *
 * A brand is used to place a product logotype on a screen.
 */
public class Brand internal constructor(sidebarStore: SidebarStore, id: String?, baseClass: String?, job: Job) :
    Div(
        id = id,
        baseClass = classes("page".component("header", "brand"), baseClass),
        job = job,
        scope = Scope()
    ) {

    private var link: A

    init {
        div(baseClass = "page".component("header", "brand", "toggle")) {
            attr("hidden", sidebarStore.data.map { !it.visible })
            classMap(sidebarStore.data.map { mapOf("display-none".util() to !it.visible) })
            clickButton(plain) {
                aria["expanded"] = sidebarStore.data.map { it.expanded.toString() }
                icon("bars".fas())
            } handledBy sidebarStore.toggle
        }
        link = a(baseClass = "page".component("header", "brand", "link")) {}
    }

    /**
     * Sets the link to the homepage of the application.
     */
    public fun link(content: A.() -> Unit = {}) {
        with(link) {
            content(this)
        }
    }

    /**
     * Sets the image for the brand.
     */
    public fun img(content: Img.() -> Unit = {}) {
        with(link) {
            domNode.querySelector(By.classname("brand".component()))?.removeFromParent()
            img(baseClass = "brand".component()) {
                content(this)
            }
        }
    }
}
