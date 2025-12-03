import java.util.Locale

plugins {
    application
    alias(libs.plugins.kotlin)
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

