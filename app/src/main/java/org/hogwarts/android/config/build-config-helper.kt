package org.hogwarts.android.config

object BuildConfigHelper {
    const val APPLICATION_ID = "org.hogwarts.android"
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1

    val isDebug: Boolean get() = org.hogwarts.android.BuildConfig.DEBUG

    val apiBaseUrl: String get() = if (isDebug) {
        "https://staging-api.databayt.org"
    } else {
        "https://api.databayt.org"
    }

    val environment: String get() = if (isDebug) "staging" else "production"
}
