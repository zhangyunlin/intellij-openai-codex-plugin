plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.3"
}

repositories {
    mavenCentral()
}

intellij {
    // Target IntelliJ IDEA Community 2024.1
    type.set("IC")
    version.set("2024.1")
    plugins.set(listOf("java"))
}

// Set Java toolchain to match current IntelliJ baseline
kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set(null as String?)
    }

    runIde {
        // leave defaults; launches sandbox IDE with the plugin
    }
}

