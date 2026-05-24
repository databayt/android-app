package org.hogwarts.android.feature.students.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.students.data.repository.StudentsRepository
import org.hogwarts.android.feature.students.domain.model.Student
import javax.inject.Inject

class GetStudentsUseCase @Inject constructor(
    private val repository: StudentsRepository
) {
    operator fun invoke(
        classId: String? = null,
        status: String? = null,
        search: String? = null
    ): Flow<Resource<List<Student>>> =
        repository.getStudents(classId = classId, status = status, search = search)
}

class GetStudentDetailUseCase @Inject constructor(
    private val repository: StudentsRepository
) {
    operator fun invoke(studentId: String): Flow<Resource<Student?>> =
        repository.getStudent(studentId)
}
