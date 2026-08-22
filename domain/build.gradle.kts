plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("javax.inject:javax.inject:1")
}

kotlin {
    jvmToolchain(21)
}
