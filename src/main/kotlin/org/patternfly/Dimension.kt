package org.patternfly

/**
 * Size modifier used in various components.
 */
public enum class Size(public val modifier: String) {
    XL_4("4xl".modifier()),
    XL_3("3xl".modifier()),
    XL_2("2xl".modifier()),
    XL("xl".modifier()),
    LG("lg".modifier()),
    MD("md".modifier()),
    SM("sm".modifier()),
    XS("xs".modifier())
}

/**
 * FontSize modifier e.g. used in [Skeleton] component.
 */
public enum class TextSize(public val modifier: String) {
    XL_4("text-4xl".modifier()),
    XL_3("text-3xl".modifier()),
    XL_2("text-2xl".modifier()),
    XL("text-xl".modifier()),
    LG("text-lg".modifier()),
    MD("text-md".modifier()),
    SM("text-sm".modifier()),
    XS("text-xs".modifier())
}
