package org.hogwarts.android.feature.admin.ui

import java.io.IOException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ClassReadinessViewModelLogicTest {

    @Test fun `a 403 means not a class supervisor`() {
        val e = HttpException(Response.error<Any>(403, "{}".toResponseBody()))
        assertEquals(ReadinessError.NotSupervisor, classifyReadinessError(e))
    }

    @Test fun `no connection is offline, anything else is a failure`() {
        assertEquals(ReadinessError.Offline, classifyReadinessError(IOException("x")))
        assertEquals(ReadinessError.Failed, classifyReadinessError(HttpException(Response.error<Any>(500, "{}".toResponseBody()))))
        assertEquals(ReadinessError.Failed, classifyReadinessError(IllegalStateException()))
    }

    @Test fun `the date arrows step one day, across month ends`() {
        assertEquals("2026-11-23", shiftDate("2026-11-24", -1))
        assertEquals("2026-12-01", shiftDate("2026-11-30", 1))
        assertNull(shiftDate(null, 1))
        assertNull(shiftDate("not-a-date", 1))
    }
}
