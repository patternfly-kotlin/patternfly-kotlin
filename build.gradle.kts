import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.js") version "1.4-M3" apply false
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.10.0" apply false
}

val fritz2Version = "0.7-SNAPSHOT"

allprojects {
    group = "org.patternfly"
    version = "0.0.1"

    repositories {
        jcenter()
        mavenLocal()
        maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.js")
    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-js")
        "implementation"("dev.fritz2:core:$fritz2Version") {
            isChanging = true
        }
    }
}
