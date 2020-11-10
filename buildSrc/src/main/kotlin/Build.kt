import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.publish.maven.MavenPom

object Constants {
    const val group = "org.patternfly"
    const val version = "0.0.1"
}

object PluginVersions {
    const val js = "1.4.10"
    const val dokka = "1.4.10"
}

object Versions {
    const val fritz2 = "0.8-SNAPSHOT"
    const val elemento = "0.0.2"
    const val kotest = "4.3.0"
}

object Dependencies {
    const val elemento = "dev.fritz2:elemento:${Versions.elemento}"
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

fun MavenPom.defaultPom() {
    name.set("patternfly-fritz2")
    description.set("Kotlin implementation of PatternFly 4 based on fritz2")
    url.set("https://github.com/patternfly-kotlin/patternfly-fritz2")
    licenses {
        license {
            name.set("Apache-2.0")
            url.set("https://opensource.org/licenses/Apache-2.0")
        }
    }
    developers {
        developer {
            id.set("hpehl")
            name.set("Harald Pehl")
            organization.set("Red Hat")
            organizationUrl.set("http://www.redhat.com")
        }
    }
    scm {
        url.set("https://github.com/patternfly-kotlin/patternfly-fritz2.git")
        connection.set("scm:git:git://github.com/patternfly-kotlin/patternfly-fritz2.git")
        developerConnection.set("scm:git:git://github.com/patternfly-kotlin/patternfly-fritz2.git")
    }
}
