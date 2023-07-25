import io.ktor.plugin.features.DockerImageRegistry
import io.ktor.plugin.features.DockerPortMapping
import io.ktor.plugin.features.DockerPortMappingProtocol
import io.ktor.plugin.features.JreVersion

plugins {
    kotlin("jvm") version "1.9.0"
    id("io.ktor.plugin") version "2.3.2"
}

group = "com.arnyminerz.escalaralcoiaicomtat.backend"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val exposedVersion: String by project

dependencies {
    // JSON support
    implementation("org.json:json:20230618")

    // Ktor dependencies
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-locations")

    // Exposed dependencies
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // Database engines
    implementation("mysql:mysql-connector-java:8.0.33")


    testImplementation(kotlin("test"))

    // For testing, use SQLite as the database engine
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")

    // Add Ktor's testing dependencies
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)

    sourceSets {
        all {
            languageSettings.enableLanguageFeature("ContextReceivers")
        }
    }
}

ktor {
    docker {
        jreVersion.set(JreVersion.JRE_17)
        localImageName.set("escalaralcoiaicomtat")
        imageTag.set(version.toString())
        portMappings.set(
            listOf(
                DockerPortMapping(80, 8080, DockerPortMappingProtocol.TCP)
            )
        )

        externalRegistry.set(
            DockerImageRegistry.dockerHub(
                appName = provider { "escalaralcoiaicomtat" },
                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
            )
        )
    }
}

application {
    mainClass.set("MainKt")
}
