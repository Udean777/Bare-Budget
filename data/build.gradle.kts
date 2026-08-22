plugins {
    id("com.android.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.ssajudn.barebudget.data"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    buildTypes {
        debug { buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\""); buildConfigField("String", "WEB_CLIENT_ID", "\"234922787074-fnm37va5028brlr45jmc9gvfp1ksgr17.apps.googleusercontent.com\"") }
        release { buildConfigField("String", "BASE_URL", "\"https://api.barebudget.app/\""); buildConfigField("String", "WEB_CLIENT_ID", "\"234922787074-fnm37va5028brlr45jmc9gvfp1ksgr17.apps.googleusercontent.com\"") }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    buildFeatures { buildConfig = true }
}

kotlin { jvmToolchain(21) }

dependencies {
    api(project(":domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.google.mlkit.text.recognition)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
