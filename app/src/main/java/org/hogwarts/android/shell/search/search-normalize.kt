package org.hogwarts.android.shell.search

import java.text.Normalizer
import java.util.Locale

/**
 * Normalize a string for case- and diacritic-insensitive matching — a literal
 * port of hogwarts `generic-command-menu/normalize.ts`, so a query typed here
 * picks the same items it picks on the web.
 *
 * In order:
 *  1. NFD, then strip combining marks — Latin diacritics (é→e) and Arabic
 *     harakat alike.
 *  2. Alef variants ا أ إ آ → ا, so أحمد finds احمد.
 *  3. Ya ى → ي, so على finds علي.
 *  4. Ta marbuta ة → ه, so فاطمة finds فاطمه.
 *  5. Drop tatweel ـ.
 *  6. Lowercase, collapse whitespace.
 */
fun normalizeForMatch(input: String): String {
    if (input.isEmpty()) return ""
    return Normalizer.normalize(input, Normalizer.Form.NFD)
        .replace(COMBINING, "")
        .replace(HARAKAT, "")
        .replace(ALEF, "ا")
        .replace("ى", "ي")
        .replace("ة", "ه")
        .replace("ـ", "")
        .lowercase(Locale.ROOT)
        .replace(WHITESPACE, " ")
        .trim()
}

private val COMBINING = Regex("[\\u0300-\\u036f]")
private val HARAKAT = Regex("[\\u064b-\\u0670\\u065f]")
private val ALEF = Regex("[إأآا]")
private val WHITESPACE = Regex("\\s+")
