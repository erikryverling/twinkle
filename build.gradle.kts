import java.util.Locale

plugins {
    application
    alias(libs.plugins.kotlin)
    alias(libs.plugins.versions)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("se.yverling.twinkle.TwinkleKt")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.retrofit)
    implementation(libs.kotlinx.serialization)
}

ktlint {
    ignoreFailures = true
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "se.yverling.twinkle.TwinkleKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // To add all of the dependencies otherwise a "NoClassDefFoundError" error
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(Locale.getDefault()).contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
