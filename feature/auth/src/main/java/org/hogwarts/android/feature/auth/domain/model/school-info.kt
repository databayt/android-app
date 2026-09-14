package org.hogwarts.android.feature.auth.domain.model

/** A school a user can pick — hogwarts `api/mobile/lib/school-directory.ts`. */
data class SchoolInfo(
    val id: String,
    val name: String,
    val nameEn: String? = null,
    val logoUrl: String? = null,
    val domain: String? = null,
)
