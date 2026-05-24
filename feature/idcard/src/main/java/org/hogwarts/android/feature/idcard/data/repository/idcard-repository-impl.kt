package org.hogwarts.android.feature.idcard.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.idcard.data.remote.IdCardApi
import org.hogwarts.android.feature.idcard.domain.model.IdCard
import javax.inject.Inject

class IdCardRepositoryImpl @Inject constructor(
    private val api: IdCardApi,
    private val tenantContext: TenantContext
) : IdCardRepository {

    override suspend fun getIdCard(): IdCard {
        val schoolId = tenantContext.requireSchoolId()
        val userId = tenantContext.requireUserId()
        return api.getIdCard(schoolId, userId).toDomain()
    }
}
