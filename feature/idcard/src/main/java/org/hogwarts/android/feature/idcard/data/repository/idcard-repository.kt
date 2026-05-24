package org.hogwarts.android.feature.idcard.data.repository

import org.hogwarts.android.feature.idcard.domain.model.IdCard

/**
 * Repository interface for ID card data.
 */
interface IdCardRepository {
    suspend fun getIdCard(): IdCard
}
