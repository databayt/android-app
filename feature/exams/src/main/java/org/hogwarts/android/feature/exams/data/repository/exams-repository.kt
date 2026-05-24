package org.hogwarts.android.feature.exams.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.exams.domain.model.Exam

interface ExamsRepository {
    fun getExams(status: String? = null): Flow<Resource<List<Exam>>>
    fun getExam(examId: String): Flow<Resource<Exam?>>
}
