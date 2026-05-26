package org.hogwarts.android.core.network.translation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire models for `POST /api/mobile/translate`
 * (hogwarts PR #346, issue #276 — P0 store gate for LOC-010).
 *
 * Mirrors the iOS client (ios-app PR #36). Same whitelist on both
 * platforms: v1 supports `announcement` + `assignment`. `message` is
 * deferred until the web Message model gains a `lang` column.
 */

@Serializable
enum class TranslatableEntity {
    @SerialName("announcement") ANNOUNCEMENT,
    @SerialName("assignment") ASSIGNMENT,
}

@Serializable
enum class SupportedLanguage {
    @SerialName("ar") AR,
    @SerialName("en") EN,
}

/**
 * Request body for `POST /api/mobile/translate`.
 *
 * `schoolId` is NOT in the request — the server scopes by JWT, so the
 * client cannot reach across tenants by tampering with `entityId`.
 */
@Serializable
data class TranslateRequest(
    @SerialName("entity_type") val entityType: TranslatableEntity,
    @SerialName("entity_id") val entityId: String,
    @SerialName("target_lang") val targetLang: SupportedLanguage,
)

/**
 * Response from `POST /api/mobile/translate`.
 *
 * `cached: true` means the server returned a value from `TranslationCache`
 * rather than calling Google Translate. Useful for analytics: a sustained
 * drop in hit-rate signals cache thrashing — e.g., announcement bodies
 * churning faster than they're re-rendered.
 */
@Serializable
data class TranslateResponse(
    @SerialName("translated_text") val translatedText: String,
    val cached: Boolean,
    @SerialName("source_lang") val sourceLang: SupportedLanguage,
)
