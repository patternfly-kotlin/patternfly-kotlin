package org.patternfly

import kotlinx.browser.document

fun initDocument() {
    document.clear()
    document.write("""<body>Loading...</body>""")
}
