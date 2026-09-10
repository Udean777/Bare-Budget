import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("javax.inject:javax.inject:1")
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
    jvmToolchain(21)
}
