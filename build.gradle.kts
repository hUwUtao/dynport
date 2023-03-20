import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.apache.tools.ant.filters.*

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("kapt") version "1.8.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"

}

group = "me.hUwUtao"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
//    implementation("net.kyori:adventure-api:4.13.0")
    // https://mvnrepository.com/artifact/com.github.luben/zstd-jni
    implementation("com.github.luben:zstd-jni:1.5.4-2")
    compileOnly("net.kyori:adventure-text-minimessage:4.13.0")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
//    testImplementation(kotlin("test"))
}

//tasks.test {
//    useJUnitPlatform()
//}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks {
    processResources {
        filter(FixCrLfFilter::class)
        filter(ReplaceTokens::class, "tokens" to mapOf("version" to project.version))
        filteringCharset = "UTF-8"
    }
    jar {
        // Disabled, because we use the shadowJar task for building our jar
        enabled = false
    }
    build {
        dependsOn(shadowJar)
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}