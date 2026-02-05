plugins {
    `java-library`
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
    compileOnlyApi("org.jetbrains:annotations:24.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

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
    compileJmhJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
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
        minimumToolchain(17)
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
