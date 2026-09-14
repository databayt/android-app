plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "org.hogwarts.android.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Single source of truth for the API host. Retrofit endpoints carry the
        // `api/...` path prefix themselves, so this value intentionally has no
        // `/api/` suffix. App-level BuildConfigHelper reads through to here.
        // Tenant comes from the JWT, so every school shares the apex host.
        // Override locally with -Phogwarts.apiBaseUrl=http://10.0.2.2:3000/
        val apiBaseUrl = providers.gradleProperty("hogwarts.apiBaseUrl")
            .getOrElse("https://balqalam.com/")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        // Production has no Socket.IO server yet; blank disables realtime and
        // messaging stays on REST. Override with -Phogwarts.socketUrl=...
        val socketUrl = providers.gradleProperty("hogwarts.socketUrl").getOrElse("")
        buildConfigField("String", "SOCKET_URL", "\"$socketUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Project modules
    implementation(project(":core:common"))

    // AppCompat (AppCompatDelegate.getApplicationLocales for Accept-Language)
    implementation(libs.androidx.appcompat)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Network
    implementation(libs.bundles.network)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Socket.IO
    implementation(libs.socketio.client)

    // Logging
    implementation(libs.timber)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
