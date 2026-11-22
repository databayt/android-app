package org.hogwarts.android.update

/** Sub-folder of `cacheDir` that holds downloaded APKs (and the FileProvider's only exposed path). */
const val UPDATES_DIR = "updates"

private val UNSAFE_NAME_CHARS = Regex("[^A-Za-z0-9._-]")
private val CONTENT_RANGE_START = Regex("""^\s*bytes\s+(\d+)\s*-""", RegexOption.IGNORE_CASE)

/**
 * `<versionName>-<latestCode>.apk`. One name per release, so a retry resumes the same file and an
 * older copy can never be mistaken for the new one. Anything that is not `[A-Za-z0-9._-]` becomes `_`
 * (no path separators can survive); a blank version name falls back to `update`.
 */
fun apkFileName(versionName: String?, latestCode: Int): String {
    val safe = versionName.orEmpty().trim()
        .replace(UNSAFE_NAME_CHARS, "_")
        .trim('.', '_', '-')
        .ifEmpty { "update" }
    return "$safe-$latestCode.apk"
}

/** The name a download is written under until it is complete and verified. */
fun partialName(finalName: String): String = "$finalName.part"

/**
 * Names in `updates/` to delete before a download: everything except the file about to be
 * written and its own partial (a download cut by a weak line resumes from that partial).
 */
fun staleUpdateFiles(existing: Collection<String>, keep: String): List<String> =
    existing.filter { it != keep && it != partialName(keep) }

/**
 * True when [actualHex] equals the manifest's [expected] digest, ignoring case and surrounding
 * whitespace. A manifest without a digest (null/blank) is accepted: the check fails open.
 */
fun sha256Matches(actualHex: String, expected: String?): Boolean {
    val want = expected?.trim().orEmpty()
    return want.isEmpty() || actualHex.trim().equals(want, ignoreCase = true)
}

/** What to do with the server's answer to a (possibly ranged) request. */
enum class ResumeAction {
    /** 206 from the offset we asked for: keep the partial file and append. */
    Append,

    /** 200: the server ignored (or we did not send) `Range` — start the file over. */
    Restart,

    /** 416 after asking from a non-zero offset: the partial file may already be the whole file. */
    CheckExisting,

    /** Anything else: not a usable body (HTTP error, or a 206 for the wrong offset). */
    Reject,
}

/**
 * [requestedFrom] is the offset sent in `Range: bytes=<n>-` (0 when no Range was sent);
 * [contentRange] the response's `Content-Range` header, if any.
 */
fun resumeAction(requestedFrom: Long, code: Int, contentRange: String?): ResumeAction = when {
    code == 200 -> ResumeAction.Restart
    code == 206 && requestedFrom > 0 -> {
        val start = contentRangeStart(contentRange)
        if (start == null || start == requestedFrom) ResumeAction.Append else ResumeAction.Reject
    }
    code == 416 && requestedFrom > 0 -> ResumeAction.CheckExisting
    else -> ResumeAction.Reject
}

/** First byte of `Content-Range: bytes 1000-1999/2000`, or null when absent/unparseable. */
fun contentRangeStart(header: String?): Long? =
    header?.let { CONTENT_RANGE_START.find(it)?.groupValues?.get(1)?.toLongOrNull() }

/** 0..100, or -1 when the total is unknown (draw an indeterminate bar). */
fun percentOf(done: Long, total: Long): Int =
    if (total <= 0L) -1 else ((done * 100L) / total).toInt().coerceIn(0, 100)
