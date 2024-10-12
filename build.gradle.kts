plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
    kotlin("jvm") version "1.6.21"
    id("org.sonarqube") version "3.3"
    idea
}

repositories {
    mavenCentral() // This allows Gradle to look for dependencies in Maven Central
}

description = "Robocode - Build the best - destroy the rest!"

val ossrhUsername: String by project
val ossrhPassword: String by project

dependencies {
    implementation(kotlin("stdlib-jdk8")) // Use Kotlin standard library
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Xlint:unchecked")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.io=ALL-UNNAMED",
            "--add-opens", "java.base/java.net=ALL-UNNAMED",
            "--add-opens", "java.base/java.util=ALL-UNNAMED"
    )
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))
            stagingProfileId.set("c7f511545ccf8")
            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}

val initializeSonatypeStagingRepository by tasks.existing
subprojects {
    initializeSonatypeStagingRepository {
        shouldRunAfter(tasks.withType<Sign>())
    }
}