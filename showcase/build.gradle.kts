dependencies {
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.109-kotlin-1.4-M3")
    implementation(project(":patternfly-fritz2"))
    implementation(npm("@patternfly/patternfly", "4.16.7"))
    // dev dependencies
    implementation(npm("css-loader", "3.6.0"))
    implementation(npm("file-loader", "6.0.0"))
    implementation(npm("style-loader", "1.2.1"))
}

kotlin {
    js {
        browser { }
        binaries.executable()
    }
}
