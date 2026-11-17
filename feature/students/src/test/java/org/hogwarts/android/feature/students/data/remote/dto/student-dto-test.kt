package org.hogwarts.android.feature.students.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pins the shape `GET /api/mobile/students` really sends (hogwarts route.ts). */
class StudentDtoTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Test
    fun `parses the server list with section as an object`() {
        val body = """
            {"data":[{"id":"s1","given_name":"أواب","family_name":"أحمد","gr_number":"G-1",
              "gender":"MALE","date_of_birth":"2012-03-04T00:00:00.000Z","status":"ACTIVE",
              "photo_url":null,"phone":"+249997021240",
              "section":{"id":"cmu8b9prc00ahsd86hi5b3g","name":"الثاني ثانوي - أ","grade":"الثاني ثانوي"}}],
             "total":1,"page":1,"per_page":20}
        """.trimIndent()
        val res = json.decodeFromString(StudentListResponse.serializer(), body)
        val s = res.data.single()
        assertEquals("أواب", s.firstName)
        assertEquals("أحمد", s.lastName)
        assertEquals("G-1", s.enrollmentNumber)
        assertEquals("الثاني ثانوي - أ", s.sectionName)
        assertEquals("الثاني ثانوي", s.gradeName)
    }

    @Test
    fun `still reads the older field names and a string section`() {
        val body = """{"data":[{"id":"s2","first_name":"A","last_name":"B","section":"أ","section_x":1}]}"""
        val s = json.decodeFromString(StudentListResponse.serializer(), body).data.single()
        assertEquals("A", s.firstName)
        assertEquals("أ", s.sectionName)
        assertNull(s.gradeName)
    }
}
