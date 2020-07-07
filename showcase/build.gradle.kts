dependencies {
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.109-kotlin-1.3.72")
    implementation(project(":core"))
    implementation(npm("@patternfly/patternfly", "4.10.31"))
    // dev dependencies
    implementation(npm("file-loader", "6.0.0"))
    implementation(npm("style-loader", "1.2.1"))
}

kotlin {
    js {
        browser { }
        binaries.executable()
    }
}
