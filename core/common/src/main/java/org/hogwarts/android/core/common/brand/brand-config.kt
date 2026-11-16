package org.hogwarts.android.core.common.brand

/**
 * Which build this is. The generic build has [locked] = false and behaves as one app for every
 * school; a school's own build is [locked] to one [school] (its subdomain) and never asks for it.
 * Built in the app module from its BuildConfig (per flavor) and provided through Hilt.
 */
data class BrandConfig(
    val school: String,
    val locked: Boolean,
    val updateUrl: String,
    val apkUrl: String,
)

/** The installed build's identity, sent on every API request. Values come from the app's BuildConfig. */
data class AppVersionInfo(
    val versionCode: Int,
    val applicationId: String,
)
