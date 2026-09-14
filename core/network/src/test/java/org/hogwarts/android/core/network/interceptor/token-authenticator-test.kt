package org.hogwarts.android.core.network.interceptor

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hogwarts.android.core.common.api.TokenProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var tokens: FakeTokenProvider
    private lateinit var client: OkHttpClient
    private val refreshCalls = AtomicInteger(0)

    @Volatile private var refreshStatus = 200

    @Before
    fun setUp() {
        server = MockWebServer()
        tokens = FakeTokenProvider(access = "old-access", refresh = "refresh-1")
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                if (path == TokenAuthenticator.AUTH_PATH && request.method == "PUT") {
                    refreshCalls.incrementAndGet()
                    Thread.sleep(50)
                    if (refreshStatus != 200) return MockResponse().setResponseCode(refreshStatus)
                    assertEquals("refresh-1", request.getHeader(TokenAuthenticator.HEADER_REFRESH_TOKEN))
                    return MockResponse().setBody(
                        """{"access_token":"new-access","refresh_token":"refresh-2","expires_at":123,"user":{}}"""
                    )
                }
                return if (request.getHeader("Authorization") == "Bearer new-access") {
                    MockResponse().setBody("""{"ok":true}""")
                } else {
                    MockResponse().setResponseCode(401)
                }
            }
        }
        server.start()
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokens))
            .authenticator(TokenAuthenticator(tokens, Json { ignoreUnknownKeys = true }))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun get(path: String = "/api/mobile/dashboard") =
        client.newCall(Request.Builder().url(server.url(path)).build()).execute()

    @Test
    fun `401 refreshes once and retries with the new token`() {
        get().use { assertEquals(200, it.code) }
        assertEquals(1, refreshCalls.get())
        assertEquals("new-access", tokens.accessToken)
        assertEquals("refresh-2", tokens.refreshToken)
        assertEquals(123L, tokens.expiresAt)
    }

    @Test
    fun `parallel 401s share a single refresh`() {
        val pool = Executors.newFixedThreadPool(5)
        val done = CountDownLatch(5)
        val codes = java.util.Collections.synchronizedList(mutableListOf<Int>())
        repeat(5) {
            pool.execute {
                get().use { codes += it.code }
                done.countDown()
            }
        }
        assertTrue(done.await(10, TimeUnit.SECONDS))
        pool.shutdown()
        assertEquals(List(5) { 200 }, codes)
        assertEquals(1, refreshCalls.get())
    }

    @Test
    fun `rejected refresh ends the session`() {
        refreshStatus = 401
        get().use { assertEquals(401, it.code) }
        assertTrue(tokens.expired)
    }

    @Test
    fun `server error on refresh keeps the session`() {
        refreshStatus = 503
        get().use { assertEquals(401, it.code) }
        assertFalse(tokens.expired)
        assertEquals("old-access", tokens.accessToken)
    }

    @Test
    fun `auth endpoints never trigger a refresh`() {
        get(TokenAuthenticator.AUTH_PATH + "/google").use { assertEquals(401, it.code) }
        assertEquals(0, refreshCalls.get())
    }

    private class FakeTokenProvider(access: String?, refresh: String?) : TokenProvider {
        @Volatile override var accessToken: String? = access
        @Volatile override var refreshToken: String? = refresh
        @Volatile var expiresAt: Long = 0
        @Volatile var expired = false

        override fun saveRefreshedTokens(accessToken: String, refreshToken: String?, expiresAtMillis: Long) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken ?: this.refreshToken
            this.expiresAt = expiresAtMillis
        }

        override fun onSessionExpired() {
            expired = true
            accessToken = null
            refreshToken = null
        }
    }
}
