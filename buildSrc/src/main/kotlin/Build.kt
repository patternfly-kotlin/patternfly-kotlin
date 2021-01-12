import org.gradle.api.artifacts.dsl.DependencyHandler

object Constants {
    const val group = "org.patternfly"
    const val name = "patternfly-fritz2"
    const val version = "0.2.0"
    const val description = "Kotlin implementation of PatternFly 4 based on fritz2"
    const val license = "Apache-2.0"
    const val githubRepo = "patternfly-kotlin/patternfly-fritz2"
}

object PluginVersions {
    const val bintray = "1.8.5"
    const val dokka = "1.4.20"
    const val js = "1.4.20"
    const val ktlint = "9.4.1"
    const val detekt = "1.15.0"
}

object Versions {
    const val fritz2 = "0.8"
    const val kotest = "4.3.2"
}

fun DependencyHandler.fritz2() {
    add("implementation", "dev.fritz2:core:${Versions.fritz2}")
}

fun DependencyHandler.kotest() {
    add("testImplementation", "io.kotest:kotest-framework-api:${Versions.kotest}")
    add("testImplementation", "io.kotest:kotest-assertions-core:${Versions.kotest}")
    add("testImplementation", "io.kotest:kotest-property:${Versions.kotest}")
    add("testImplementation", "io.kotest:kotest-framework-engine:${Versions.kotest}")
}
