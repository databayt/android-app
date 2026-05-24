package org.hogwarts.android.feature.messaging.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactDto(
    val id: String,
    val firstName: String = "",
    val lastName: String = "",
    val displayName: String,
    val email: String? = null,
    val image: String? = null,
    val role: String,
    val category: String,
    val contextLabel: String? = null,
    val hasWhatsApp: Boolean = false,
)

@Serializable
data class ContactGroupDto(
    val category: String,
    val contacts: List<ContactDto>,
)

@Serializable
data class ContactGroupsResponse(
    val groups: List<ContactGroupDto>,
)
