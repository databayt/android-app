package org.hogwarts.android.feature.idcard.domain.usecase

import org.hogwarts.android.core.common.result.Result
import org.hogwarts.android.feature.idcard.data.repository.IdCardRepository
import org.hogwarts.android.feature.idcard.domain.model.IdCard
import javax.inject.Inject

class GetIdCardUseCase @Inject constructor(
    private val repository: IdCardRepository
) {
    suspend operator fun invoke(): Result<IdCard> {
        return try {
            Result.Success(repository.getIdCard())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
