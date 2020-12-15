import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.publish.maven.MavenPom

object Constants {
    const val group = "org.patternfly"
    const val name = "patternfly-fritz2"
    const val version = "0.1.2"
    const val description = "Kotlin implementation of PatternFly 4 based on fritz2"
    const val license = "Apache-2.0"
    const val githubRepo = "patternfly-kotlin/patternfly-fritz2"
}

object PluginVersions {
    const val bintray = "1.8.5"
    const val dokka = "1.4.20"
    const val js = "1.4.20"
    const val ktlint = "9.4.1"
}

object Versions {
    const val fritz2 = "0.8"
    const val kotest = "4.3.1"
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
    description.set(Constants.description)
    url.set("https://github.com/${Constants.githubRepo}")
    licenses {
        license {
            name.set(Constants.license)
            url.set("https://opensource.org/licenses/Apache-2.0")
        }
    }
    developers {
        developer {
            id.set("hpehl")
            name.set("Harald Pehl")
            organization.set("Red Hat")
            organizationUrl.set("https://developers.redhat.com/")
        }
    }
    scm {
        url.set("https://github.com/${Constants.githubRepo}.git")
        connection.set("scm:git:git://github.com/${Constants.githubRepo}.git")
        developerConnection.set("scm:git:git://github.com/#${Constants.githubRepo}.git")
    }
    issueManagement {
        url.set("https://github.com/${Constants.githubRepo}/issues")
    }
}
