package org.hogwarts.android.feature.fees.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeeRecordDto(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_name") val studentName: String,
    val description: String,
    val amount: Double,
    @SerialName("paid_amount") val paidAmount: Double = 0.0,
    @SerialName("due_date") val dueDate: String,
    val status: String,
    val category: String? = null,
    val term: String? = null
)

@Serializable
data class FeeListResponse(
    val data: List<FeeRecordDto>
)

@Serializable
data class FeeSummaryDto(
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("paid_amount") val paidAmount: Double,
    @SerialName("pending_amount") val pendingAmount: Double,
    @SerialName("overdue_amount") val overdueAmount: Double
)
