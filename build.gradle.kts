plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "MyGrepConsoleExtension"
version = "1.0.0"

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2023.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("java","GrepConsole:12.19.211.6693.0"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("211.6693.3")
        untilBuild.set("")
        changeNotes.set(
            buildString {
                append("")
            }
        )
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }


    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildSearchableOptions {
        enabled = false
    }
}

repositories {
    mavenCentral()
}


dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}