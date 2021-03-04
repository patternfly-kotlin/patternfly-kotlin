package org.patternfly

import kotlinx.browser.document

/**
 * Create an empty DIV Element in the document root (body).
 * The created element can then be used for component mounting.
 *
 * @param id of the created element
 */
fun createRootElement(id: String = "target") {
    val div = document.createElement("div")
    div.id = id
    document.body!!.appendChild(div)
}

/**
 * Remove an element from the dom by id
 * It ignores unknown id's
 *
 * @param id of the element
 */
fun removeElementById(id: String = "target") {
    document.getElementById(id)?.remove()
}
