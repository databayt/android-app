package org.hogwarts.android.feature.idcard.data.remote.dto

import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.idcard.domain.model.IdCard

@Serializable
data class IdCardDto(
    val id: String,
    val userId: String,
    val name: String = "",
    val role: String = "",
    val photoUrl: String? = null,
    val schoolName: String = "",
    val schoolLogo: String? = null,
    val idNumber: String = "",
    val barcode: String = "",
    val qrContent: String = "",
    val validUntil: String = ""
) {
    fun toDomain(): IdCard = IdCard(
        id = id,
        userId = userId,
        name = name,
        role = role,
        photoUrl = photoUrl,
        schoolName = schoolName,
        schoolLogo = schoolLogo,
        idNumber = idNumber,
        barcode = barcode,
        qrContent = qrContent,
        validUntil = validUntil
    )
}
