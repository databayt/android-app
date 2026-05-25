plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "org.hogwarts.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.hogwarts.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Restrict shipped locales to those we actually translate.
        resourceConfigurations += listOf("en", "ar")

        // API base URL lives in core/network/BuildConfig.API_BASE_URL (single
        // source of truth). Socket URL stays here for now until E09 lifts it.
        buildConfigField("String", "SOCKET_URL", "\"https://ed.databayt.org\"")
    }

    androidResources {
        // AGP synthesizes res/xml/_generated_res_locale_config.xml from the
        // values-* directories present at build time, eliminating manual drift.
        generateLocaleConfig = true
    }

    buildTypes {
        debug {
            isDebuggable = true
            // Generate en-XA (text expansion) and ar-XB (RTL pseudo-locale)
            // resources for layout/translation QA. Switch via Developer Options
            // → Languages on a debug build.
            isPseudoLocalesEnabled = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            // API_BASE_URL is owned by core/network; override there if needed per buildType.
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // API_BASE_URL is owned by core/network; override there if needed per buildType.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        // Treat XML hardcoded text and missing translations as build failures.
        // (Compose `Text("…")` literals aren't covered by HardcodedText; rely
        // on code review and a future Compose lint pack for those.)
        error += setOf("HardcodedText", "MissingTranslation")
        warning += setOf("ImpliedQuantity", "MissingQuantity")
        checkDependencies = true
        abortOnError = true
    }
}

/**
 * Fails the build when an `<string name="…">` exists in `values/strings.xml`
 * but is missing from `values-ar/strings.xml` (or vice-versa). Catches drift
 * across the ~30 feature/core modules without relying on lint, which only
 * catches per-module gaps.
 */
tasks.register("checkStringParity") {
    description = "Verifies en/ar <string> name parity across all module strings.xml files."
    group = "verification"

    val rootDir = rootProject.projectDir
    val nameRegex = Regex("""<string\s+name="([^"]+)"""")

    doLast {
        fun parse(file: java.io.File): Set<String> =
            nameRegex.findAll(file.readText())
                .map { it.groupValues[1] }
                .toSet()

        // Group strings.xml files by module path so we compare the right pairs.
        fun moduleKey(file: java.io.File): String =
            file.relativeTo(rootDir).invariantSeparatorsPath
                .substringBefore("/src/")

        val en = mutableMapOf<String, MutableSet<String>>()
        val ar = mutableMapOf<String, MutableSet<String>>()

        rootDir.walkTopDown()
            .filter { it.isFile && it.invariantSeparatorsPath.contains("/src/main/res/") }
            .forEach { file ->
                val path = file.invariantSeparatorsPath
                if (path.contains("/build/")) return@forEach
                when {
                    path.endsWith("/values/strings.xml") ->
                        en.getOrPut(moduleKey(file)) { mutableSetOf() } += parse(file)
                    path.endsWith("/values-ar/strings.xml") ->
                        ar.getOrPut(moduleKey(file)) { mutableSetOf() } += parse(file)
                }
            }

        val problems = mutableListOf<String>()
        en.forEach { (module, keys) ->
            val missing = keys - (ar[module].orEmpty())
            if (missing.isNotEmpty()) {
                problems += "$module is missing AR translations for: ${missing.sorted()}"
            }
        }
        ar.forEach { (module, keys) ->
            val orphaned = keys - (en[module].orEmpty())
            if (orphaned.isNotEmpty()) {
                problems += "$module has AR-only keys not in EN: ${orphaned.sorted()}"
            }
        }
        if (problems.isNotEmpty()) {
            error("String parity check failed:\n" + problems.joinToString("\n"))
        }
    }
}

tasks.named("check") { dependsOn("checkStringParity") }

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:security"))

    // Feature modules
    implementation(project(":feature:auth"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:students"))
    implementation(project(":feature:attendance"))
    implementation(project(":feature:grades"))
    implementation(project(":feature:fees"))
    implementation(project(":feature:timetable"))
    implementation(project(":feature:messaging"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:exams"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:notifications"))
    implementation(project(":feature:guardian"))
    implementation(project(":feature:teacher"))
    implementation(project(":feature:announcements"))
    implementation(project(":feature:events"))
    implementation(project(":feature:admission"))
    implementation(project(":feature:library"))
    implementation(project(":feature:subjects"))
    implementation(project(":feature:report-cards"))
    implementation(project(":feature:stream"))
    implementation(project(":feature:lessons"))
    implementation(project(":feature:admin"))
    implementation(project(":feature:idcard"))
    implementation(project(":feature:quizgame"))

    // New core modules
    implementation(project(":core:sync"))
    implementation(project(":core:push"))
    implementation(project(":core:connectivity"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.compose)

    // Security (EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Image loading (for Coil ImageLoaderFactory with SVG support)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.foundation)

    // Room
    implementation(libs.bundles.room)

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Network (Retrofit)
    implementation(libs.bundles.network)

    // WorkManager
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Logging
    implementation(libs.timber)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
