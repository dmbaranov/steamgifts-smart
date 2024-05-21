plugins {
    application
    kotlin("jvm") version "1.9.23"
}

group = "org.steamgifts"
version = "1.0-SNAPSHOT"

application {
    mainClass = "org.steamgifts.MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("it.skrape:skrapeit:1.2.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}