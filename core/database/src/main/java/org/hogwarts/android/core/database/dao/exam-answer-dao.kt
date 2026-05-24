package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.ExamAnswerEntity
import org.hogwarts.android.core.database.entity.ExamViolationEntity
import org.hogwarts.android.core.database.entity.QuestionBankEntity

@Dao
interface ExamAnswerDao {
    @Query("SELECT * FROM exam_answers WHERE examId = :examId")
    suspend fun getAnswers(examId: String): List<ExamAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnswers(answers: List<ExamAnswerEntity>)

    @Query("SELECT * FROM exam_violations WHERE examId = :examId ORDER BY timestamp DESC")
    suspend fun getViolations(examId: String): List<ExamViolationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViolation(violation: ExamViolationEntity)

    @Query("SELECT * FROM question_bank WHERE schoolId = :schoolId ORDER BY subject ASC, topic ASC")
    fun getQuestionBank(schoolId: String): Flow<List<QuestionBankEntity>>

    @Query("SELECT * FROM question_bank WHERE subject = :subject AND schoolId = :schoolId")
    fun getQuestionsBySubject(subject: String, schoolId: String): Flow<List<QuestionBankEntity>>

    @Query("UPDATE question_bank SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmarked(id: String, bookmarked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestions(questions: List<QuestionBankEntity>)
}
