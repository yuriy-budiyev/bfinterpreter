plugins {
    kotlin("jvm") version "2.3.10"
}

group = "com.github.yuriybudiyev.brainfuck"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
