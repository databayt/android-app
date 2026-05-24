package org.hogwarts.android.feature.profile.domain.model

data class SchoolInfo(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val domain: String? = null,
    val isCurrentSchool: Boolean = false
)
