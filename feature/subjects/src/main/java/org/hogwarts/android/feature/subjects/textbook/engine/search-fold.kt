package org.hogwarts.android.feature.subjects.textbook.engine

import java.text.Normalizer

/**
 * Arabic-aware folding for book search — `normalizeForSearch` and
 * `normalizeWithMap` in the web's `parse.ts`: diacritics and tatweel go,
 * hamza seats fold to their letter, ة/ه and ى/ي meet, directional marks go,
 * Latin lowercases and whitespace collapses.
 */
object SearchFold {
    private val DIACRITICS = Regex("[ً-ٰٟۖ-ۭ]")
    private val ZERO_WIDTH = Regex("[​-‏‪-‮﻿]")
    private val SPACES = Regex("\\s+")

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFKC)
            .replace(DIACRITICS, "")
            .replace("ـ", "")
            .replace(Regex("[أإآٱ]"), "ا")
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace(ZERO_WIDTH, "")
            .lowercase()
            .replace(SPACES, " ")
            .trim()

    private val cache = HashMap<Char, String>()

    private fun fold(ch: Char): String = cache.getOrPut(ch) { normalize(ch.toString()) }

    /** The folded text plus, for each of its characters, the source index it came from. */
    fun withMap(text: String): Pair<String, IntArray> {
        val out = StringBuilder(text.length)
        val map = IntArray(text.length * 2 + 1)
        var n = 0
        var pendingSpace = false
        for (i in text.indices) {
            val ch = text[i]
            if (ch.isWhitespace()) {
                pendingSpace = out.isNotEmpty()
                continue
            }
            val folded = fold(ch)
            if (folded.isEmpty()) continue
            if (pendingSpace) {
                out.append(' ')
                map[n++] = i
                pendingSpace = false
            }
            for (c in folded) {
                out.append(c)
                if (n < map.size) map[n++] = i
            }
        }
        return out.toString() to map.copyOf(n)
    }
}
