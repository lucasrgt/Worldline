import org.gradle.plugin.compatibility.compatibility

plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.1.1"
}

group = "io.github.lucasrgt.worldline"
version = "0.2.1"

layout.buildDirectory.set(rootProject.file("../../.worldline/gradle-plugin/build"))

repositories { mavenCentral() }

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
    withJavadocJar()
}

gradlePlugin {
    website.set("https://github.com/lucasrgt/Worldline")
    vcsUrl.set("https://github.com/lucasrgt/Worldline.git")
    plugins {
        create("worldlineTest") {
            id = "io.github.lucasrgt.worldline.test"
            implementationClass = "dev.worldline.gradle.WorldlinePlugin"
            displayName = "Worldline TestKit"
            description = "Deterministic Java 8 mod tests with serialized official Minecraft runtimes."
            tags.set(listOf("minecraft", "testing", "modding", "beta"))
            compatibility {
                features {
                    configurationCache = false
                }
            }
        }
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.12.2")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.compilerArgs.addAll(listOf("-Xlint:all,-options", "-Werror"))
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
