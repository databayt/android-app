package org.hogwarts.android.config

import org.hogwarts.android.core.network.BuildConfig as NetworkBuildConfig

object BuildConfigHelper {
    const val APPLICATION_ID = "org.hogwarts.android"
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1

    val isDebug: Boolean get() = org.hogwarts.android.BuildConfig.DEBUG

    // Reads through to core/network's BuildConfig.API_BASE_URL — the single
    // source of truth for the API host. See core/network/build.gradle.kts.
    val apiBaseUrl: String get() = NetworkBuildConfig.API_BASE_URL

    val environment: String get() = if (isDebug) "staging" else "production"
}
