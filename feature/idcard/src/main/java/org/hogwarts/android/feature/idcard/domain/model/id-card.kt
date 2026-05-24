package org.hogwarts.android.feature.idcard.domain.model

/**
 * Domain model for a digital ID card.
 */
data class IdCard(
    val id: String,
    val userId: String,
    val name: String,
    val role: String,
    val photoUrl: String? = null,
    val schoolName: String,
    val schoolLogo: String? = null,
    val idNumber: String,
    val barcode: String,
    val qrContent: String,
    val validUntil: String
)
