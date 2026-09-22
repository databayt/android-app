package org.hogwarts.android.feature.subjects.data.repository

import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.subjects.data.remote.SubjectsApi
import org.hogwarts.android.feature.subjects.domain.model.SubjectCatalog
import org.hogwarts.android.feature.subjects.domain.model.SubjectLevel
import org.hogwarts.android.feature.subjects.domain.model.SubjectDetail
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network-first implementation of [SubjectsRepository].
 *
 * All calls are scoped by schoolId via [TenantContext] for multi-tenant isolation,
 * and forward the active UI language so the backend's `getDisplayText` translates
 * catalog names into the user's locale (matches the web behavior).
 */
@Singleton
class SubjectsRepositoryImpl @Inject constructor(
    private val api: SubjectsApi,
    private val tenantContext: TenantContext,
) : SubjectsRepository {

    override suspend fun getSubjects(
        search: String?,
        department: String?,
    ): SubjectCatalog {
        tenantContext.requireSchoolId()
        val response = api.getSubjects(
            search = search,
            department = department,
            lang = currentLang(),
        )
        val body = response.body() ?: throw Exception("Failed to load subjects")
        return SubjectCatalog(
            subjects = body.data.map { it.toDomain() },
            // Parsed strictly: an unknown stage must not count as ELEMENTARY,
            // or it could turn the tabs on for a single-stage school.
            schoolLevels = body.schoolLevels.mapNotNull { value ->
                SubjectLevel.entries.find { it.name.equals(value, ignoreCase = true) }
            }.toSet(),
        )
    }

    override suspend fun getSubjectDetail(subjectId: String): SubjectDetail {
        tenantContext.requireSchoolId()
        val response = api.getSubjectDetail(
            subjectId = subjectId,
            lang = currentLang(),
        )
        return response.body()?.toDomain()
            ?: throw Exception("Failed to load subject detail")
    }

    /** ISO 639-1 language code ("ar"/"en") for the active app locale. */
    private fun currentLang(): String = Locale.getDefault().language
}
