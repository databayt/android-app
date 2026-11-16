package org.hogwarts.android.update

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** `version.json` on the CDN. Only the two codes are required. */
@Serializable
data class UpdateManifest(
    val latestCode: Int = 0,
    val minCode: Int = 0,
    val apkUrl: String? = null,
    val notesAr: String? = null,
    val notesEn: String? = null,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Null on any parse problem — the check fails open. */
        fun parse(body: String): UpdateManifest? = runCatching { json.decodeFromString<UpdateManifest>(body) }.getOrNull()
    }
}
