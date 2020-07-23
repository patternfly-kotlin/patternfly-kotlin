dependencies {
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.109-kotlin-1.4-M3")
    implementation(project(":patternfly-fritz2"))
    implementation(npm("@patternfly/patternfly", "4.23.3"))
    implementation(npm("clipboard", "2.0.6"))
    implementation(npm("highlight.js", "10.1.1"))
    // dev dependencies
    implementation(npm("css-loader", "3.6.0"))
    implementation(npm("file-loader", "6.0.0"))
    implementation(npm("sass", "1.26.10"))
    implementation(npm("sass-loader", "9.0.2"))
    implementation(npm("style-loader", "1.2.1"))
    implementation(kotlin("stdlib-js"))
}

kotlin {
    js {
        browser { }
        binaries.executable()
    }
}

repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    mavenCentral()
}
