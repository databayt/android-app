package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.BookEntity
import org.hogwarts.android.core.database.entity.BorrowingEntity

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE schoolId = :schoolId ORDER BY title ASC")
    fun getBooks(schoolId: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE schoolId = :schoolId AND category = :category ORDER BY title ASC")
    fun getBooksByCategory(schoolId: String, category: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id AND schoolId = :schoolId")
    suspend fun getBookById(id: String, schoolId: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooks(books: List<BookEntity>)

    @Query("SELECT * FROM borrowings WHERE userId = :userId AND schoolId = :schoolId ORDER BY borrowedDate DESC")
    fun getBorrowings(userId: String, schoolId: String): Flow<List<BorrowingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBorrowings(borrowings: List<BorrowingEntity>)
}
