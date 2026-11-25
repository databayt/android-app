package org.hogwarts.android.feature.admin.ui

import java.io.IOException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class SchoolAnalysisViewModelLogicTest {

    @Test fun `a 403 means admins only`() {
        val e = HttpException(Response.error<Any>(403, "{}".toResponseBody()))
        assertEquals(AnalysisError.Forbidden, classifyAnalysisError(e))
    }

    @Test fun `no connection is offline, anything else is a failure`() {
        assertEquals(AnalysisError.Offline, classifyAnalysisError(IOException("x")))
        assertEquals(AnalysisError.Failed, classifyAnalysisError(HttpException(Response.error<Any>(500, "{}".toResponseBody()))))
        assertEquals(AnalysisError.Failed, classifyAnalysisError(IllegalStateException()))
    }
}
