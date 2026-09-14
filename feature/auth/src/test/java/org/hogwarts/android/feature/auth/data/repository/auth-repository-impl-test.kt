package org.hogwarts.android.feature.auth.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.security.TokenManager
import org.hogwarts.android.feature.auth.data.remote.AuthApi
import org.hogwarts.android.feature.auth.data.remote.dto.AuthResponseDto
import org.hogwarts.android.feature.auth.data.remote.dto.GoogleAuthRequestDto
import org.hogwarts.android.feature.auth.data.remote.dto.SocialAuthResponseDto
import org.hogwarts.android.feature.auth.domain.model.AuthError
import org.hogwarts.android.feature.auth.domain.model.AuthException
import org.hogwarts.android.feature.auth.domain.model.SchoolInfo
import org.hogwarts.android.feature.auth.domain.model.SocialLogin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class AuthRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true; isLenient = true }
    private lateinit var api: AuthApi
    private lateinit var tokens: TokenManager
    private lateinit var session: SessionManager
    private lateinit var repository: AuthRepositoryImpl

    /** The exact body prod answered for admin@balqalam.com on 2026-09-14. */
    private val loginBody = """
        {"access_token":"eyJ.a","refresh_token":"eyJ.r","expires_at":1789484272103,
         "user":{"id":"u1","email":"admin@balqalam.com","school_id":"s1","role":"ADMIN",
         "given_name":"ألباس","family_name":"دمبلدور","avatar_url":"https://cdn.databayt.org/avatars/admin.webp","grade":null}}
    """.trimIndent()

    @Before
    fun setup() {
        api = mockk()
        tokens = mockk(relaxed = true)
        session = mockk(relaxed = true)
        repository = AuthRepositoryImpl(api, tokens, session)
    }

    private fun <T> error(code: Int, body: String): Response<T> =
        Response.error(code, body.toResponseBody("application/json".toMediaType()))

    @Test
    fun `login decodes the prod shape and saves tokens`() = runTest {
        coEvery { api.login(any()) } returns Response.success(json.decodeFromString(AuthResponseDto.serializer(), loginBody))
        val result = repository.login(" admin@balqalam.com ", "1234")
        assertEquals(UserRole.ADMIN, result.role)
        assertEquals("s1", result.schoolId)
        verify { tokens.saveTokens("eyJ.a", "eyJ.r", 1789484272103) }
    }

    @Test
    fun `401 on login is invalid credentials`() = runTest {
        coEvery { api.login(any()) } returns error(401, """{"error":"Invalid email or password"}""")
        assertError(AuthError.InvalidCredentials) { repository.login("admin@balqalam.com", "x") }
    }

    @Test
    fun `401 for a password-less account is social only`() = runTest {
        coEvery { api.login(any()) } returns error(401, """{"error":"Please login with your OAuth provider (Google/Facebook)"}""")
        assertError(AuthError.SocialOnly) { repository.login("g@x.com", "x") }
    }

    @Test
    fun `403 is suspended`() = runTest {
        coEvery { api.login(any()) } returns error(403, """{"error":"Account is suspended"}""")
        assertError(AuthError.Suspended) { repository.login("a@b.co", "x") }
    }

    @Test
    fun `no connection is a network error`() = runTest {
        coEvery { api.login(any()) } throws IOException("offline")
        assertError(AuthError.Network) { repository.login("a@b.co", "x") }
    }

    @Test
    fun `an unknown role saves nothing`() = runTest {
        val body = json.decodeFromString(AuthResponseDto.serializer(), loginBody.replace("\"ADMIN\"", "\"WIZARD\""))
        coEvery { api.login(any()) } returns Response.success(body)
        assertError(AuthError.UnsupportedRole) { repository.login("a@b.co", "x") }
        verify(exactly = 0) { tokens.saveTokens(any(), any(), any()) }
    }

    @Test
    fun `google needs_school returns the schools and saves nothing`() = runTest {
        val body = json.decodeFromString(
            SocialAuthResponseDto.serializer(),
            """{"needs_school":true,"schools":[{"id":"s1","name":"الملك فهد","name_en":"King Fahd","logo_url":null,"domain":"kingfahd"}]}""",
        )
        coEvery { api.loginWithGoogle(GoogleAuthRequestDto("tok", null)) } returns Response.success(body)
        val outcome = repository.loginWithGoogle("tok", null)
        assertEquals(SocialLogin.NeedsSchool(listOf(SchoolInfo("s1", "الملك فهد", "King Fahd", null, "kingfahd"))), outcome)
        verify(exactly = 0) { tokens.saveTokens(any(), any(), any()) }
    }

    @Test
    fun `google with a school returns tokens`() = runTest {
        val body = json.decodeFromString(SocialAuthResponseDto.serializer(), loginBody)
        coEvery { api.loginWithGoogle(GoogleAuthRequestDto("tok", "s1")) } returns Response.success(body)
        val outcome = repository.loginWithGoogle("tok", "s1")
        assertTrue(outcome is SocialLogin.Authenticated)
    }

    @Test
    fun `google needs_school with no schools is an error`() = runTest {
        coEvery { api.loginWithGoogle(any()) } returns Response.success(SocialAuthResponseDto(needsSchool = true))
        assertError(AuthError.NoSchool) { repository.loginWithGoogle("tok", null) }
    }

    @Test
    fun `google request omits a null school_id`() {
        val encoded = json.encodeToString(GoogleAuthRequestDto.serializer(), GoogleAuthRequestDto("tok"))
        assertEquals("""{"id_token":"tok"}""", encoded)
        assertFalse("school_id" in encoded)
        assertTrue("school_id" in json.encodeToString(GoogleAuthRequestDto.serializer(), GoogleAuthRequestDto("tok", "s1")))
    }

    @Test
    fun `new-password code errors`() = runTest {
        coEvery { api.setNewPassword(any()) } returns error(401, """{"error":"Invalid or expired verification code"}""")
        assertError(AuthError.InvalidCode) { repository.setNewPassword("a@b.co", "000000", "secret1") }
        coEvery { api.setNewPassword(any()) } returns error(401, """{"error":"Verification code has expired. Please request a new one."}""")
        assertError(AuthError.CodeExpired) { repository.setNewPassword("a@b.co", "000000", "secret1") }
        coEvery { api.setNewPassword(any()) } returns error(429, """{"error":"Too many requests"}""")
        assertError(AuthError.TooManyRequests) { repository.setNewPassword("a@b.co", "000000", "secret1") }
    }

    @Test
    fun `reset lowercases the email`() = runTest {
        coEvery { api.requestPasswordReset(match { it.email == "parent@balqalam.com" }) } returns Response.success(Unit)
        repository.requestPasswordReset(" Parent@Balqalam.com ")
    }

    private suspend fun assertError(expected: AuthError, block: suspend () -> Unit) {
        try {
            block()
            fail("expected $expected")
        } catch (e: AuthException) {
            assertEquals(expected, e.error)
        }
    }
}
