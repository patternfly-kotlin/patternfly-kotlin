import java.net.URL

plugins {
    kotlin("js") version PluginVersions.js
    id("org.jetbrains.dokka") version PluginVersions.dokka
    `maven-publish`
}

group = Constants.group
version = Constants.version

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    jcenter()
}

dependencies {
    fritz2()
    implementation(Dependencies.elemento)
    kotest()
}

kotlin {
    js {
        explicitApi()
        compilations.named("main") {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
            }
        }
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("main") {
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
            platform.set(org.jetbrains.dokka.Platform.js)
            samples.from("src/main/resources/")
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/patternfly-kotlin/patternfly-fritz2/blob/master/" +
                        "src/main/kotlin/"
                ))
                remoteLineSuffix.set("#L")
            }
            externalDocumentationLink {
                this.url.set(URL("https://api.fritz2.dev/core/core/"))
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            pom {
                defaultPom()
            }
        }
    }
}
