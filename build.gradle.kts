plugins {
    application
    kotlin("jvm") version "2.0.0"
}

group = "org.steamgifts"
version = "1.0-SNAPSHOT"

application {
    mainClass = "org.steamgifts.MainKt"
    applicationName = "steamgifts"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

distributions {
    main {
        distributionBaseName.set("steamgifts")
    }
}