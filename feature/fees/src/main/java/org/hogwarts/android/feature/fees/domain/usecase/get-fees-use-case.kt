package org.hogwarts.android.feature.fees.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.fees.data.repository.FeesRepository
import org.hogwarts.android.feature.fees.domain.model.FeeRecord
import javax.inject.Inject

class GetFeesUseCase @Inject constructor(
    private val repository: FeesRepository
) {
    operator fun invoke(studentId: String? = null, status: String? = null): Flow<Resource<List<FeeRecord>>> =
        repository.getFees(studentId, status)
}
