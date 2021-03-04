package org.patternfly

/**
 * Creates a CSS class for screen-reader
 * The content is invisible and only available to a screen reader, use inspector to investigate
 *
 * @see <a href="https://www.patternfly.org/v4/utilities/accessibility/#screen-reader-only">
 *     https://www.patternfly.org/v4/utilities/accessibility/#screen-reader-only</a>
 */
public fun screenReader(): String = "screen-reader".util()
