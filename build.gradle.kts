// Root build.gradle.kts for Hogwarts Android App
// Following BMAD methodology and Hogwarts patterns

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
}

// Clean task
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
