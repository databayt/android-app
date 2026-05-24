package org.hogwarts.android.feature.students.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.students.domain.model.Student

/**
 * Repository interface for student data operations.
 */
interface StudentsRepository {

    fun getStudents(
        classId: String? = null,
        status: String? = null,
        search: String? = null
    ): Flow<Resource<List<Student>>>

    fun getStudent(studentId: String): Flow<Resource<Student?>>

    suspend fun createStudent(student: Student): Student

    suspend fun updateStudent(student: Student): Student
}
