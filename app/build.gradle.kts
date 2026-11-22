import java.time.ZoneId
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// E01.S01: Materialise app/google-services.json before AGP's googleServices
// plugin needs it. Real Firebase config stays gitignored — `.template` ships a
// stub that lets `:app:processDebugGoogleServices` succeed on a fresh checkout
// (runtime Firebase calls fail gracefully via AppStartupInitializer.confirmFirebase).
//
// Override on a developer machine with:
//     ./gradlew assembleDebug -Pfirebase.config.path=/abs/path/to/google-services.json
// Or drop the real file at app/google-services.json (stays out of git).
run {
    val target = file("google-services.json")
    if (target.exists()) return@run

    val overridePath = providers.gradleProperty("firebase.config.path").orNull
    val template = file("google-services.json.template")
    val source = when {
        overridePath != null -> file(overridePath).also {
            require(it.exists()) {
                "firebase.config.path points to $overridePath but the file does not exist."
            }
        }
        template.exists() -> {
            logger.warn(
                "[E01.S01] Using google-services.json.template (stub Firebase config). " +
                    "FCM + Crashlytics will not deliver until a real config is provided. " +
                    "See README.md → Firebase setup."
            )
            template
        }
        else -> error(
            "No google-services.json available. Either drop one at app/google-services.json, " +
                "pass -Pfirebase.config.path=/abs/path, or restore app/google-services.json.template. " +
                "See README.md → Firebase setup."
        )
    }
    source.copyTo(target, overwrite = false)
}

// School brand: a school's own build (name, icon, colour, locked school, own update feed) comes
// from a directory OUTSIDE the repo, passed as `-Phogwarts.brand=/abs/path/to/brand` with
//   brand.properties  (school, package, label, label.en, color, updateUrl, apkUrl)
//   res/              (launcher + splash PNGs, colors.xml `balqalam_ground`, strings `app_name`)
// Without the property the `school` flavor is disabled and the build is the generic one.
val brandDir: java.io.File? = providers.gradleProperty("hogwarts.brand").orNull?.let { path ->
    val dir = file(path)
    require(dir.isDirectory && file("$path/brand.properties").isFile && file("$path/res").isDirectory) {
        "hogwarts.brand=$path must be a directory holding brand.properties and a res/ directory."
    }
    dir
}
val brandProps = Properties().apply {
    brandDir?.let { dir ->
        dir.resolve("brand.properties").reader(Charsets.UTF_8).use { load(it) }
        listOf("school", "package", "updateUrl", "apkUrl").forEach { key ->
            require(!getProperty(key).isNullOrBlank()) { "${dir.path}/brand.properties is missing `$key`." }
        }
        getProperty("timezone")?.trim()?.takeIf { it.isNotEmpty() }?.let { zone ->
            ZoneId.of(zone) // a typo fails the build, not the app
        }
    }
}
fun brandProp(key: String): String = brandProps.getProperty(key).orEmpty().trim()
fun quoted(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "org.hogwarts.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.hogwarts.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 13
        versionName = "1.3.3"

        // x86 is emulator-only; shipping arm only shrinks the APK a lot.
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Restrict shipped locales to those we actually translate.
        resourceConfigurations += listOf("en", "ar")

        // API and socket URLs live in core/network's BuildConfig (single source of truth).
    }

    flavorDimensions += "brand"
    productFlavors {
        // Generic Balqalam app: any school, signs in to whichever one the user names.
        create("balqalam") {
            dimension = "brand"
            isDefault = true
            buildConfigField("String", "BRAND_SCHOOL", quoted(""))
            buildConfigField("String", "BRAND_UPDATE_URL", quoted("https://cdn.databayt.org/balqalam/app/version.json"))
            buildConfigField("String", "BRAND_APK_URL", quoted("https://cdn.databayt.org/balqalam/app/balqalam.apk"))
            buildConfigField("String", "BRAND_TIMEZONE", quoted(""))
            buildConfigField("boolean", "BRAND_LOCKED", "false")
        }
        // One school's own build (only with -Phogwarts.brand=...): same signing key, its own identity.
        create("school") {
            dimension = "brand"
            brandDir?.let { applicationId = brandProp("package") }
            buildConfigField("String", "BRAND_SCHOOL", quoted(brandProp("school")))
            buildConfigField("String", "BRAND_UPDATE_URL", quoted(brandProp("updateUrl")))
            buildConfigField("String", "BRAND_APK_URL", quoted(brandProp("apkUrl")))
            buildConfigField("String", "BRAND_TIMEZONE", quoted(brandProp("timezone")))
            buildConfigField("boolean", "BRAND_LOCKED", "true")
        }
    }

    sourceSets {
        // The brand's res/ overrides main resources (icon, splash, colour, app_name).
        brandDir?.let { getByName("school").res.srcDir(it.resolve("res")) }
    }

    androidResources {
        // AGP synthesizes res/xml/_generated_res_locale_config.xml from the
        // values-* directories present at build time, eliminating manual drift.
        generateLocaleConfig = true
    }

    // Release signing: the keystore stays off the repo. Set
    // HOGWARTS_KEYSTORE (path) and HOGWARTS_KEYSTORE_PASSWORD (Keychain:
    // hogwarts-android-keystore-password); without them a release build is
    // left unsigned rather than signed with a throwaway key. Every update must
    // be signed with the SAME key — losing it means reinstalling everywhere.
    val keystorePath = providers.environmentVariable("HOGWARTS_KEYSTORE").orNull
    val keystorePassword = providers.environmentVariable("HOGWARTS_KEYSTORE_PASSWORD").orNull
    signingConfigs {
        if (keystorePath != null && keystorePassword != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = "balqalam"
                keyPassword = keystorePassword
            }
        }
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
            signingConfigs.findByName("release")?.let { signingConfig = it }
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
androidComponents {
    // No brand directory: a `school` build cannot exist, so assemble/test/lint all stay generic.
    beforeVariants(selector().all()) { variant ->
        if (brandDir == null && variant.productFlavors.any { it.second == "school" }) {
            variant.enable = false
        }
    }
}

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
    implementation(project(":feature:lumos"))
    implementation(project(":feature:live"))
    implementation(project(":feature:lessons"))
    implementation(project(":feature:admin"))
    implementation(project(":feature:idcard"))

    // New core modules
    implementation(project(":core:sync"))
    implementation(project(":core:push"))
    implementation(project(":core:connectivity"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)

    // Security (EncryptedSharedPreferences)
    implementation(libs.security.crypto)

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
    implementation(libs.androidx.core.splashscreen)

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
