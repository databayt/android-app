package org.hogwarts.android.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/** How a download ended. */
enum class DownloadOutcome {
    /** The file is complete and its SHA-256 matches (or the manifest had none). */
    Verified,

    /** Two complete downloads in a row failed the SHA-256 check (or the server sent a bad range). */
    Corrupt,

    /** The line dropped or the server refused. A partial file is kept so the next try resumes. */
    Network,

    /** The device ran out of storage. */
    NoSpace,
}

/**
 * Streams the release APK from the CDN into `cacheDir/updates/`. Uses a plain client of its own
 * (like [UpdateChecker]): no auth interceptor, no tenant headers — the school token must never
 * reach the CDN. Redirects are followed; the read timeout is generous for weak lines.
 */
@Singleton
class ApkDownloader @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * Deletes older files in [target]'s folder, then downloads (resuming a partial [target]) and
     * verifies it. A SHA-256 mismatch deletes the file and retries the whole download once.
     * [onProgress] gets 0..100 (or -1 when the size is unknown); [onVerifying] fires before hashing.
     */
    suspend fun fetch(
        url: String,
        target: File,
        sha256: String?,
        onProgress: (Int) -> Unit,
        onVerifying: () -> Unit,
    ): DownloadOutcome = withContext(Dispatchers.IO) {
        target.parentFile?.let { dir ->
            dir.mkdirs()
            staleUpdateFiles(dir.list().orEmpty().toList(), target.name).forEach { File(dir, it).delete() }
        }
        var outcome = DownloadOutcome.Corrupt
        // Corrupt => the file is gone; one more clean pass, then give up.
        repeat(2) {
            if (outcome == DownloadOutcome.Corrupt) outcome = downloadOnce(url, target, sha256, onProgress, onVerifying)
        }
        outcome
    }

    private suspend fun downloadOnce(
        url: String,
        target: File,
        sha256: String?,
        onProgress: (Int) -> Unit,
        onVerifying: () -> Unit,
    ): DownloadOutcome {
        // Bytes land in `<name>.part` and only a complete, verified file takes the final name. So the
        // final name always means "ready to install", and a partial is never mistaken for a bad file:
        // hashing a half-finished download and deleting it on the mismatch would throw away exactly
        // the megabytes a weak line had managed to bring down.
        val part = File(target.parentFile, partialName(target.name))
        try {
            // Already downloaded and intact (e.g. the app was restarted after the installer was backed out).
            if (target.exists() && target.length() > 0) {
                onVerifying()
                if (sha256Matches(sha256Hex(target), sha256)) return DownloadOutcome.Verified
                target.delete()
            }
            val from = if (part.exists()) part.length() else 0L

            val request = Request.Builder()
                .url(url)
                // Identity: the CDN must not gzip an APK, or Content-Length (our progress total) is lost.
                .header("Accept-Encoding", "identity")
                .apply { if (from > 0) header("Range", "bytes=$from-") }
                .build()
            val call = client.newCall(request)
            val cancelOnStop = coroutineContext.job.invokeOnCompletion { call.cancel() }
            try {
                call.execute().use { response ->
                    when (resumeAction(from, response.code, response.header("Content-Range"))) {
                        ResumeAction.CheckExisting -> {
                            // The server says there is nothing after our last byte: the partial is whole.
                            onVerifying()
                            return finish(part, target, sha256)
                        }
                        ResumeAction.Reject -> {
                            if (response.code == 206) {
                                part.delete()
                                return DownloadOutcome.Corrupt
                            }
                            throw IOException("HTTP ${response.code}")
                        }
                        ResumeAction.Append, ResumeAction.Restart -> {
                            val append = from > 0 && response.code == 206
                            val length = response.body.contentLength()
                            val total = if (length > 0) length + (if (append) from else 0L) else -1L
                            var done = if (append) from else 0L
                            var lastPercent = Int.MIN_VALUE
                            FileOutputStream(part, append).use { out ->
                                response.body.byteStream().use { input ->
                                    val buffer = ByteArray(BUFFER_BYTES)
                                    while (true) {
                                        coroutineContext.job.ensureActive()
                                        val n = input.read(buffer)
                                        if (n < 0) break
                                        out.write(buffer, 0, n)
                                        done += n
                                        val percent = percentOf(done, total)
                                        if (percent != lastPercent) {
                                            lastPercent = percent
                                            onProgress(percent)
                                        }
                                    }
                                }
                            }
                            // A short body keeps the partial file: the next try resumes from here.
                            if (total > 0 && done != total) return DownloadOutcome.Network
                        }
                    }
                    Unit
                }
            } finally {
                cancelOnStop.dispose()
            }

            onVerifying()
            return finish(part, target, sha256)
        } catch (e: IOException) {
            return if (isNoSpace(e)) DownloadOutcome.NoSpace else DownloadOutcome.Network
        }
    }

    /** A complete partial: verify it, then give it the final name. A bad one is deleted. */
    private fun finish(part: File, target: File, sha256: String?): DownloadOutcome {
        if (!part.exists() || !sha256Matches(sha256Hex(part), sha256)) {
            part.delete()
            return DownloadOutcome.Corrupt
        }
        target.delete()
        if (!part.renameTo(target)) {
            // Same folder, so a rename should never fail; copy as a last resort.
            part.copyTo(target, overwrite = true)
            part.delete()
        }
        return DownloadOutcome.Verified
    }

    private fun isNoSpace(e: IOException): Boolean {
        val m = e.message.orEmpty()
        return m.contains("ENOSPC") || m.contains("No space left", ignoreCase = true)
    }

    private fun sha256Hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_BYTES)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val BUFFER_BYTES = 64 * 1024
    }
}
