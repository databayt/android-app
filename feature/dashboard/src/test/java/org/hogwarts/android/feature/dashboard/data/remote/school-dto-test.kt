package org.hogwarts.android.feature.dashboard.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SchoolDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `the school decodes with a timezone`() {
        val school = json.decodeFromString<SchoolDto>("""{"id":"s1","timezone":"Africa/Khartoum"}""")
        assertEquals("Africa/Khartoum", school.timezone)
    }

    @Test
    fun `the school decodes without a timezone from an older server`() {
        val school = json.decodeFromString<SchoolDto>("""{"id":"s1","name":"x"}""")
        assertNull(school.timezone)
    }
}
