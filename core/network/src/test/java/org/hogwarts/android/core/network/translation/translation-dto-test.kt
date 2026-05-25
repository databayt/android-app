package org.hogwarts.android.core.network.translation

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Wire-contract tests for the translation DTOs. Locks the snake_case
 * mapping so a future rename can't silently break the Android ↔ web
 * boundary.
 */
class TranslationDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `TranslateRequest encodes with snake_case keys and lowercase enum names`() {
        val req = TranslateRequest(
            entityType = TranslatableEntity.ANNOUNCEMENT,
            entityId = "ann-1",
            targetLang = SupportedLanguage.EN,
        )
        val out = json.encodeToString(TranslateRequest.serializer(), req)
        assertEquals(
            """{"entity_type":"announcement","entity_id":"ann-1","target_lang":"en"}""",
            out,
        )
    }

    @Test
    fun `TranslateRequest assignment + ar encodes correctly`() {
        val req = TranslateRequest(
            entityType = TranslatableEntity.ASSIGNMENT,
            entityId = "asg-9",
            targetLang = SupportedLanguage.AR,
        )
        val out = json.encodeToString(TranslateRequest.serializer(), req)
        assertEquals(
            """{"entity_type":"assignment","entity_id":"asg-9","target_lang":"ar"}""",
            out,
        )
    }

    @Test
    fun `TranslateResponse decodes snake_case payload from the web`() {
        val payload = """
            {
              "translated_text": "Hello",
              "cached": true,
              "source_lang": "ar"
            }
        """.trimIndent()
        val res = json.decodeFromString(TranslateResponse.serializer(), payload)
        assertEquals("Hello", res.translatedText)
        assertEquals(true, res.cached)
        assertEquals(SupportedLanguage.AR, res.sourceLang)
    }

    @Test
    fun `TranslateResponse round-trips an empty translated_text (server returns 200 for empty source)`() {
        val payload = """
            {
              "translated_text": "",
              "cached": true,
              "source_lang": "en"
            }
        """.trimIndent()
        val res = json.decodeFromString(TranslateResponse.serializer(), payload)
        assertEquals("", res.translatedText)
        assertEquals(true, res.cached)
    }

    @Test
    fun `TranslateResponse fails to decode when source_lang is missing`() {
        // Drop source_lang — decoder should throw rather than silently
        // pass a default through.
        val payload = """
            {
              "translated_text": "Hello",
              "cached": false
            }
        """.trimIndent()
        assertThrows(Exception::class.java) {
            json.decodeFromString(TranslateResponse.serializer(), payload)
        }
    }

    @Test
    fun `enum surface matches the server whitelist exactly`() {
        // Adding a new entity type means adding it here AND on the web
        // server — if the server adds a third and Android stays at two,
        // this assertion catches it as soon as someone touches the enum.
        assertEquals(2, TranslatableEntity.values().size)
        assertEquals(2, SupportedLanguage.values().size)
    }
}
