package org.hogwarts.android.shell.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rules `normalize.ts` documents, checked with its own examples — the two
 * sides have to agree or a query finds different pages on the phone than it
 * does in the browser.
 */
class SearchNormalizeTest {

    @Test
    fun `alef variants collapse`() {
        assertEquals(normalizeForMatch("احمد"), normalizeForMatch("أحمد"))
        assertEquals(normalizeForMatch("احمد"), normalizeForMatch("إحمد"))
        assertEquals(normalizeForMatch("احمد"), normalizeForMatch("آحمد"))
    }

    @Test
    fun `ya and ta marbuta collapse`() {
        assertEquals(normalizeForMatch("علي"), normalizeForMatch("على"))
        assertEquals(normalizeForMatch("فاطمه"), normalizeForMatch("فاطمة"))
    }

    @Test
    fun `harakat and tatweel are dropped`() {
        assertEquals("محمد", normalizeForMatch("مُحَمَّد"))
        assertEquals("محمد", normalizeForMatch("محـمد"))
    }

    @Test
    fun `latin diacritics and case fold`() {
        assertEquals("eleve", normalizeForMatch("Élève"))
        assertEquals("a b", normalizeForMatch("  A   B  "))
    }

    @Test
    fun `arabic keywords find a page whose title is written otherwise`() {
        val items = listOf(
            SearchItem("nav-students", "الطلاب", "/students", SearchItemKind.Page, listOf("students", "طلاب", "تلاميذ")),
            SearchItem("nav-finance", "المالية", "/finance", SearchItemKind.Page, listOf("finance", "مالية")),
        )
        assertEquals(listOf("nav-students"), filterByQuery(items, "تلاميذ").map { it.id })
        // The synonym is written without the definite article; the query has it.
        assertTrue(filterByQuery(items, "طلاب").any { it.id == "nav-students" })
        assertTrue(filterByQuery(items, "STUDENTS").any { it.id == "nav-students" })
    }
}
