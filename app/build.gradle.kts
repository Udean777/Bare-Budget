import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Release signing: credentials live in gitignored keystore.properties (local)
// with KEYSTORE_PASSWORD / KEY_PASSWORD env fallbacks (CI). Missing config
// degrades gracefully to an unsigned APK instead of failing the build.
val keystoreProps = Properties()
rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use {
    keystoreProps.load(it)
}

android {
    namespace = "com.ssajudn.bareuang"
    compileSdk {
        version = release(36)
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProps["storeFile"]?.toString()?.let { path ->
                // Tolerate a leading '/' meant as "relative to project root".
                file(path).takeIf { it.exists() } ?: rootProject.file(path.removePrefix("/"))
            } ?: System.getenv("KEYSTORE_FILE")?.let { file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: keystoreProps["storePassword"] as String?
            keyAlias = System.getenv("KEY_ALIAS") ?: keystoreProps["keyAlias"] as String?
            keyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProps["keyPassword"] as String?
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    defaultConfig {
        applicationId = "com.ssajudn.bareuang"
        minSdk = 26
        targetSdk = 36
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "BASE_URL", "\"https://api.bareuang.app/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.all { it.jvmArgs("-Dnet.bytebuddy.experimental=true") }
        // android.util.Log (used by data-layer mappers) returns defaults in JVM tests.
        unitTests.isReturnDefaultValues = true
    }
}

// Module-wide opt-in so no file needs @OptIn for Material 3 APIs.
// MaterialExpressiveTheme, ShortNavigationBar and WideNavigationRail are all
// still annotated as experimental in material3 1.4.0.
kotlin {
    compilerOptions {
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
        )
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":presentation"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation & ViewModel
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    implementation(libs.androidx.work.runtime.ktx)

    // Home screen widget (Glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    testImplementation(libs.junit)
    // Required only by repository integration fakes in app unit tests; production
    // Room implementation remains encapsulated by :data.
    testImplementation(libs.androidx.room.runtime)
    testImplementation(libs.androidx.room.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    // The BOM must be applied to the androidTest configuration too, otherwise the
    // versionless Compose test artifacts below cannot resolve — which failed the
    // build for `lint` and any instrumented test run.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    implementation(libs.androidx.palette.ktx)
}
