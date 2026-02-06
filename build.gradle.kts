plugins {
    id("net.kyori.indra") version "4.0.0"
    id("net.kyori.indra.publishing") version "4.0.0"
    id("net.neoforged.licenser") version "0.7.5"
    id("me.champeau.jmh") version "0.7.2"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/bedrockk/MoLang")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
    maven("https://maven.blamejared.com/") // moonflower's molang-compiler
}

dependencies {
    api("org.javassist:javassist:3.30.2-GA")
    api("com.google.guava:guava:31.1-jre")
    compileOnlyApi("org.jetbrains:annotations:24.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // performance comparison with other libraries
    jmhImplementation("com.bedrockk:molang:1.0-SNAPSHOT")
    jmhImplementation("gg.moonflower:molang-compiler:3.1.1.19")
}

tasks {
    register<Exec>("generateExpectations") {
        commandLine = listOf("node", "scripts/generate_expectations.js")
    }
    test {
        dependsOn("generateExpectations")
    }
    javadoc {
        isFailOnError = false
    }
    withType<Sign>().configureEach {
        enabled = false
    }
}

indra {
    javaVersions {
        target(17)
    }

    publishReleasesTo("roxymc", "https://repo.roxymc.net/releases")
    publishSnapshotsTo("roxymc", "https://repo.roxymc.net/snapshots")

    mitLicense()

    configurePublications {
        pom {
            name = "Mocha"
            description = project.description

            developers {
                developer {
                    id.set("yusshu")
                    name.set("Andre Roldan")
                    email.set("andre@unnamed.team")
                }
            }
        }
    }
}

license {
    header.set(rootProject.resources.text.fromFile("header.txt"))
    include("**/*.java")
    newLine = false
}
