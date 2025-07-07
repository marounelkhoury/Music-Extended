import java.util.Properties

// Load local.properties for sensitive keys
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { input ->
        localProperties.load(input)
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // KSP for annotation processing
}

android {
    namespace = "com.musicextended"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.musicextended"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["appAuthRedirectScheme"] = "com.musicextended"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Inject keys with proper quoting
            buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${localProperties["SPOTIFY_CLIENT_ID"] ?: ""}\"")
            buildConfigField("String", "CLIENT_SECRET", "\"${localProperties["CLIENT_SECRET"] ?: ""}\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties["GEMINI_API_KEY"] ?: ""}\"")
        }

        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true

            // Inject keys with proper quoting
            buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${localProperties["SPOTIFY_CLIENT_ID"] ?: ""}\"")
            buildConfigField("String", "CLIENT_SECRET", "\"${localProperties["CLIENT_SECRET"] ?: ""}\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties["GEMINI_API_KEY"] ?: ""}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true  // Make sure BuildConfig generation is enabled!
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    // AndroidX Core & UI
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")

    // Lifecycle & Activity Compose
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    // If you intend to use Paging 3 with Room
    // implementation("androidx.room:room-paging:$room_version")

    // Networking - Retrofit, Gson, OkHttp Logging
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // OAuth2 - AppAuth
    implementation("net.openid:appauth:0.11.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Image Loading - Coil (Consolidated to one entry)
    implementation("io.coil-kt:coil-compose:2.7.0") // Using 2.7.0 for Coil

    // Security Crypto
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // Testing (default template)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}