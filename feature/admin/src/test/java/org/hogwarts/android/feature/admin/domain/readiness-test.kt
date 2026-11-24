package org.hogwarts.android.feature.admin.domain

import kotlinx.serialization.json.Json
import org.hogwarts.android.feature.admin.R
import org.hogwarts.android.feature.admin.data.remote.dto.ReadinessResponseDto
import org.hogwarts.android.feature.admin.domain.model.ReadinessCounts
import org.hogwarts.android.feature.admin.domain.model.ReadinessLevel
import org.hogwarts.android.feature.admin.domain.model.ReadinessPerson
import org.hogwarts.android.feature.admin.domain.model.ReadinessReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReadinessTest {

    private fun person(vararg r: ReadinessReason) = ReadinessPerson("p", ReadinessLevel.Ok, r.toList())

    @Test fun `every server reason code maps to its own string`() {
        val expected = mapOf(
            "on_app" to R.string.readiness_on_app,
            "allowed_web" to R.string.readiness_allowed_web,
            "android_web" to R.string.readiness_android_web,
            "old_app" to R.string.readiness_old_app,
            "password_trouble" to R.string.readiness_password_trouble,
            "never_signed_in" to R.string.readiness_never_signed_in,
            "no_class_recent" to R.string.readiness_no_class_recent,
            "push_off" to R.string.readiness_push_off,
        )
        assertEquals(expected.size, ReadinessReason.entries.size)
        expected.forEach { (code, res) -> assertEquals(code, res, ReadinessReason.fromCode(code)?.labelRes) }
    }

    @Test fun `an unknown code is dropped, not crashed on`() {
        assertNull(ReadinessReason.fromCode("something_new"))
        val dto = Json { ignoreUnknownKeys = true }.decodeFromString<ReadinessResponseDto>(
            """{"date":"2026-11-24","classes":[{"sectionId":"a","name":"1A","supervisor":null,
               "sessions":[{"start":"08:00","subject":null,"teacher":null}],
               "students":[{"name":"S","username":"s1","level":"warn","reasons":["on_app","something_new"],"extra":1}]}]}"""
        )
        val c = dto.toDomain().classes.single()
        assertNull(c.supervisor)
        assertEquals(listOf(ReadinessReason.OnApp), c.students.single().reasons)
        assertEquals(ReadinessLevel.Warn, c.students.single().level)
    }

    @Test fun `severity follows the server rules`() {
        assertEquals(ReadinessLevel.Bad, ReadinessReason.NeverSignedIn.level)
        assertEquals(ReadinessLevel.Warn, ReadinessReason.AndroidWeb.level)
        assertEquals(ReadinessLevel.Ok, ReadinessReason.OnApp.level)
    }

    @Test fun `card counts tally the students by reason`() {
        val students = listOf(
            person(ReadinessReason.OnApp),
            person(ReadinessReason.OnApp, ReadinessReason.OldApp),
            person(ReadinessReason.AndroidWeb),
            person(ReadinessReason.NeverSignedIn),
            person(ReadinessReason.NoClassRecent, ReadinessReason.AllowedWeb),
            person(ReadinessReason.NeverSignedIn, ReadinessReason.NoClassRecent), // counted once
            person(ReadinessReason.PasswordTrouble),
        )
        assertEquals(ReadinessCounts(onApp = 2, androidWeb = 1, notAttending = 3, signInTrouble = 1), ReadinessCounts.of(students))
    }

    @Test fun `an empty class counts zero`() {
        assertEquals(ReadinessCounts(0, 0, 0, 0), ReadinessCounts.of(emptyList()))
    }
}
