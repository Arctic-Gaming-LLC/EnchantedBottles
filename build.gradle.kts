plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.4.1"
}

group = "dev.arctic"
version = "2.0.0"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("dev.arctic:IceStorm:1.0.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        val pluginVersion = version.toString()
        filesMatching("plugin.yml") {
            expand("version" to pluginVersion)
        }
    }

    shadowJar {
        archiveClassifier.set("shaded")
        // relocate("some.lib", "dev.arctic.enchantedbottles.lib.somelib")
    }

    jar {
        archiveClassifier.set("")
    }
}
