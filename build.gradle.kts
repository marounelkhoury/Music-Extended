// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.kotlin.android) apply false
//    alias(libs.plugins.kotlin.compose) apply false
//}

// Top-level build file for Music Extended (Kotlin DSL)
plugins {
    // Apply the Android Application plugin and Kotlin Android plugin
    id("com.android.application") version "8.3.2" apply false // Use your AGP version from Project Structure
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false // Set Kotlin version to 1.9.20
    id("org.jetbrains.kotlin.kapt") version "1.9.20" apply false // Kapt needs to match Kotlin version
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}


// All projects configuration (Kotlin DSL equivalent)
// This is managed by dependencyResolutionManagement in settings.gradle.kts now.
// The 'allprojects' block is less common in modern Kotlin DSL setups.

// Clean task (Kotlin DSL)
tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}