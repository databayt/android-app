package org.hogwarts.android.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches the brand's version.json with a plain client (no auth, no tenant headers, 10 s).
 * Throttled to once per [CHECK_INTERVAL_MS] per process; the first call after a cold start always runs.
 * Any failure returns null — an unreachable CDN must never block a teacher.
 */
@Singleton
class UpdateChecker @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    @Volatile private var lastCheckMs = 0L

    /** Returns the manifest, or null when throttled, offline or unparseable. */
    suspend fun check(url: String, nowMs: Long = System.currentTimeMillis()): UpdateManifest? {
        if (url.isBlank()) return null
        if (lastCheckMs != 0L && nowMs - lastCheckMs < CHECK_INTERVAL_MS) return null
        lastCheckMs = nowMs
        return withContext(Dispatchers.IO) {
            runCatching {
                client.newCall(Request.Builder().url(url).header("Cache-Control", "no-cache").build()).execute().use { r ->
                    if (!r.isSuccessful) null else r.body?.string()?.let(UpdateManifest::parse)
                }
            }.getOrNull()
        }
    }
}
