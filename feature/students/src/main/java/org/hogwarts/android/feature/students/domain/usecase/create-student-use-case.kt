package org.hogwarts.android.feature.students.domain.usecase

import org.hogwarts.android.feature.students.data.repository.StudentsRepository
import org.hogwarts.android.feature.students.domain.model.Student
import javax.inject.Inject

class CreateStudentUseCase @Inject constructor(
    private val repository: StudentsRepository
) {
    suspend operator fun invoke(student: Student): Student =
        repository.createStudent(student)
}
