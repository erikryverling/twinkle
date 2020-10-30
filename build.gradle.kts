import org.gradle.kotlin.dsl.*

plugins {
    application
    kotlin("jvm") version "1.4.10"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("se.yverling.twinkle.TwinkleKt")
}

val retrofitVersion by extra("2.8.1")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("io.reactivex.rxjava2:rxkotlin:2.2.0")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.jakewharton.retrofit:retrofit2-rxjava2-adapter:1.0.0")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "se.yverling.twinkle.TwinkleKt"
    }

    // To add all of the dependencies otherwise a "NoClassDefFoundError" error
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}