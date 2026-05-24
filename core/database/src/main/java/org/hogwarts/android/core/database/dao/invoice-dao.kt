package org.hogwarts.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.database.entity.InvoiceEntity
import org.hogwarts.android.core.database.entity.InvoiceLineItemEntity
import org.hogwarts.android.core.database.entity.PaymentTransactionEntity

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE schoolId = :schoolId ORDER BY dueDate DESC")
    fun getInvoices(schoolId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE studentId = :studentId AND schoolId = :schoolId ORDER BY dueDate DESC")
    fun getInvoicesByStudent(studentId: String, schoolId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE status = :status AND schoolId = :schoolId ORDER BY dueDate DESC")
    fun getInvoicesByStatus(status: String, schoolId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id AND schoolId = :schoolId")
    suspend fun getInvoiceById(id: String, schoolId: String): InvoiceEntity?

    @Query("SELECT * FROM invoice_line_items WHERE invoiceId = :invoiceId")
    suspend fun getLineItems(invoiceId: String): List<InvoiceLineItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoices(invoices: List<InvoiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLineItems(items: List<InvoiceLineItemEntity>)

    @Query("DELETE FROM invoice_line_items WHERE invoiceId = :invoiceId")
    suspend fun deleteLineItemsByInvoice(invoiceId: String)

    @Query("SELECT * FROM payment_transactions WHERE schoolId = :schoolId ORDER BY processedAt DESC")
    fun getTransactions(schoolId: String): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE invoiceId = :invoiceId AND schoolId = :schoolId")
    fun getTransactionsByInvoice(invoiceId: String, schoolId: String): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE id = :id AND schoolId = :schoolId")
    suspend fun getTransactionById(id: String, schoolId: String): PaymentTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransactions(transactions: List<PaymentTransactionEntity>)
}
