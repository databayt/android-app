package org.hogwarts.android.feature.attendance.domain.model

data class AttendanceAnalytics(
    val totalDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int,
    val excusedDays: Int,
    val attendancePercentage: Float,
    val monthlyBreakdown: List<MonthlyAttendance> = emptyList()
) {
    companion object {
        fun fromRecords(records: List<AttendanceRecord>): AttendanceAnalytics {
            val total = records.size
            val present = records.count { it.status == AttendanceStatus.PRESENT }
            val absent = records.count { it.status == AttendanceStatus.ABSENT }
            val late = records.count { it.status == AttendanceStatus.LATE }
            val excused = records.count { it.status == AttendanceStatus.EXCUSED }
            val percentage = if (total > 0) (present + late).toFloat() / total * 100 else 0f

            val monthly = records
                .groupBy { "${it.date.year}-${it.date.monthValue}" }
                .map { (key, recs) ->
                    val parts = key.split("-")
                    MonthlyAttendance(
                        year = parts[0].toInt(),
                        month = parts[1].toInt(),
                        totalDays = recs.size,
                        presentDays = recs.count { it.status == AttendanceStatus.PRESENT },
                        absentDays = recs.count { it.status == AttendanceStatus.ABSENT }
                    )
                }
                .sortedBy { it.year * 100 + it.month }

            return AttendanceAnalytics(
                totalDays = total,
                presentDays = present,
                absentDays = absent,
                lateDays = late,
                excusedDays = excused,
                attendancePercentage = percentage,
                monthlyBreakdown = monthly
            )
        }
    }
}

data class MonthlyAttendance(
    val year: Int,
    val month: Int,
    val totalDays: Int,
    val presentDays: Int,
    val absentDays: Int
) {
    val attendanceRate: Float
        get() = if (totalDays > 0) presentDays.toFloat() / totalDays * 100 else 0f
}
