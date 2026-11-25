package org.hogwarts.android.feature.admin.domain

import kotlinx.serialization.json.Json
import org.hogwarts.android.feature.admin.data.remote.dto.AnalysisResponseDto
import org.hogwarts.android.feature.admin.domain.model.ReadinessLevel
import org.hogwarts.android.feature.admin.domain.model.ReadinessReason
import org.hogwarts.android.feature.admin.domain.model.barFraction
import org.hogwarts.android.feature.admin.domain.model.percentText
import org.hogwarts.android.feature.admin.domain.model.ratioText
import org.hogwarts.android.feature.admin.domain.model.shortDate
import org.hogwarts.android.feature.admin.domain.model.slotIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalysisTest {

    @Test fun `percent and ratio text`() {
        assertEquals("80%", percentText(80.0))
        assertEquals("67%", percentText(66.7))
        assertEquals("0%", percentText(0.0))
        assertEquals("—", percentText(null))
        assertEquals("—", percentText(Double.NaN))
        assertEquals("12/40", ratioText(12, 40))
    }

    @Test fun `bar scaling is safe at max zero and clamps`() {
        assertEquals(0f, barFraction(5, 0), 0f)
        assertEquals(0f, barFraction(0, 10), 0f)
        assertEquals(0f, barFraction(-3, 10), 0f)
        assertEquals(0.5f, barFraction(5, 10), 0.0001f)
        assertEquals(1f, barFraction(20, 10), 0f)
    }

    @Test fun `rtl mirrors the canvas slots`() {
        assertEquals(0, slotIndex(0, 7, rtl = false))
        assertEquals(6, slotIndex(0, 7, rtl = true))
        assertEquals(0, slotIndex(6, 7, rtl = true))
    }

    @Test fun `dates are shortened`() {
        assertEquals("11-24", shortDate("2026-11-24"))
        assertEquals("x", shortDate("x"))
    }

    @Test fun `the server json parses, unknown reasons and keys are tolerated`() {
        val dto = Json { ignoreUnknownKeys = true }.decodeFromString<AnalysisResponseDto>(
            """{"attendanceRate14d":80,"readinessPct":50,
               "attendanceByDay":[{"date":"2026-11-24","joined":8,"expected":10}],
               "joinsByDay":[{"date":"2026-11-24","app":5,"androidWeb":2,"computer":1}],
               "needsAttention":[{"userId":"u","name":"B","role":"STUDENT","sectionName":"1A","level":"bad",
                  "reasons":["never_signed_in","something_new"]}],
               "counts":{"members":3,"onApp":2,"pushOn":1,"active7d":1},"future":1}"""
        )
        val a = dto.toDomain()
        assertEquals("80%", percentText(a.attendanceRate14d))
        assertEquals(50.0, a.readinessPct, 0.0)
        assertEquals(8, a.attendanceByDay.single().joined)
        assertEquals(8, a.joinsByDay.single().total)
        assertTrue(a.hasAttendanceData && a.hasJoinData)
        val p = a.needsAttention.single()
        assertEquals(ReadinessLevel.Bad, p.level)
        assertEquals(listOf(ReadinessReason.NeverSignedIn), p.reasons)
        assertEquals(2, a.counts.onApp)
    }

    @Test fun `a null rate and an empty report parse to empty charts`() {
        val a = Json { ignoreUnknownKeys = true }.decodeFromString<AnalysisResponseDto>(
            """{"attendanceRate14d":null,"readinessPct":0,"attendanceByDay":[],"joinsByDay":[],"needsAttention":[],
               "counts":{"members":0,"onApp":0,"pushOn":0,"active7d":0}}"""
        ).toDomain()
        assertNull(a.attendanceRate14d)
        assertFalse(a.hasAttendanceData)
        assertFalse(a.hasJoinData)
        assertEquals("—", percentText(a.attendanceRate14d))
    }
}
