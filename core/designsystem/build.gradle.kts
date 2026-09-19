plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.roborazzi)
}

/**
 * Thmanyah (خط ثمانية) is the Arabic face the web sets the dashboard in, and
 * its license permits embedding in an app but forbids redistribution — so the
 * TTFs are fetched per machine into `.thmanyah/` (git-ignored) by
 * `scripts/fetch-thmanyah.mjs` rather than committed to this public repo.
 *
 * This task stages them as a generated resource directory, where the five
 * `R.font.thmanyah_serif_text_*` ids always resolve. On a checkout that never
 * ran the fetch script — CI, a fresh clone — it stages Noto Sans Arabic under
 * the same names instead, so Arabic still renders in an open face and nothing
 * fails to compile. The rendered text is the only thing that differs.
 */
abstract class StageThmanyahTask : DefaultTask() {

    /** `.thmanyah/` — empty on any machine that has not run the fetch script. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val licensed: ConfigurableFileCollection

    /** `src/main/res/font/` — carries the open faces that stand in. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fallback: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun stage() {
        // Weight → the Noto face that stands in for it when Thmanyah is absent.
        val weights = mapOf(
            300 to "noto_sans_arabic_regular",
            400 to "noto_sans_arabic_regular",
            500 to "noto_sans_arabic_medium",
            700 to "noto_sans_arabic_bold",
            900 to "noto_sans_arabic_bold",
        )
        val fontDir = outputDir.get().dir("font").asFile
        fontDir.deleteRecursively()
        fontDir.mkdirs()
        weights.forEach { (weight, noto) ->
            val real = licensed.files.firstOrNull { it.name == "thmanyah-serif-text-$weight.ttf" }
            val source = real ?: fallback.files.first { it.name == "$noto.ttf" }
            source.copyTo(File(fontDir, "thmanyah_serif_text_$weight.ttf"), overwrite = true)
        }
    }
}

val stageThmanyah = tasks.register<StageThmanyahTask>("stageThmanyah") {
    licensed.from(layout.projectDirectory.dir(".thmanyah").asFileTree.matching { include("*.ttf") })
    fallback.from(layout.projectDirectory.dir("src/main/res/font").asFileTree.matching { include("noto_sans_arabic_*.ttf") })
}

androidComponents {
    onVariants { variant ->
        variant.sources.res?.addGeneratedSourceDirectory(stageThmanyah, StageThmanyahTask::outputDir)
    }
}

android {
    namespace = "org.hogwarts.android.core.designsystem"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    // Coil for image loading
    implementation(libs.coil.compose)

    // Navigation & Serialization (for atom studio nav graph)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
