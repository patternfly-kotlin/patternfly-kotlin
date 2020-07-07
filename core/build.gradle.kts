ktlint {
    disabledRules.set(setOf("import-ordering"))
}

detekt {
    toolVersion = "1.10.0"
}

kotlin {
    js {
        browser { }
        binaries.executable()
    }
}
