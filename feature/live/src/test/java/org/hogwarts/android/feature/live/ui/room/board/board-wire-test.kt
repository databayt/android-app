package org.hogwarts.android.feature.live.ui.room.board

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The board's wire against bytes the WEB encoder produced (hogwarts `class-channel.ts`
 * `encodeMessage`, generated with `npx tsx` 2026-11-17): the phone decodes them and writes them
 * back out as the same JSON.
 */
class BoardWireTest {
    private val json = Json

    private val webPen =
        """{"t":"wb.stroke","stroke":{"id":"k1a2b3c4d5e6f7g8","tool":"pen","color":"#ef4444","width":6,"points":[[0.1234,0.5],[0.25,0.6],[1,0]]}}"""
    private val webHighlighterByHost =
        """{"t":"wb.stroke","stroke":{"id":"hl1","tool":"highlighter","color":"#eab308","width":12,"points":[[0.1,0.1],[0.9,0.1]],"by":"teacher-1"}}"""
    private val webArabicText =
        """{"t":"wb.stroke","stroke":{"id":"tx1","tool":"text","color":"#1f2937","width":12,"points":[[0.3,0.4]],"text":"مرحبا \"بكم\" ✓"}}"""
    private val webOldClient = """{"t":"wb.stroke","stroke":{"id":"old1","color":"#3b82f6","width":3,"points":[[0.5,0.5]]}}"""
    private val webDraft = """{"t":"wb.draft","draft":{"id":"r1","tool":"rect","color":"#22c55e","width":6,"points":[[0.2,0.2],[0.6,0.7]]}}"""
    private val webSync =
        """{"t":"sync.state","questions":[],"poll":null,"slides":null,"whiteboard":true,"strokeCount":3,"grant":"*","spotlight":null,"timer":null,"quiz":null}"""
    private val webSyncEmpty =
        """{"t":"sync.state","questions":[],"poll":null,"slides":null,"whiteboard":false,"strokeCount":0,"grant":null,"spotlight":null,"timer":null,"quiz":null}"""

    private fun decode(s: String) = BoardWire.decode(s.toByteArray(Charsets.UTF_8))
    private fun same(expected: String, actual: String) = assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(actual))

    @Test
    fun `web strokes decode and re-encode to the same JSON`() {
        for (web in listOf(webPen, webHighlighterByHost, webArabicText, webOldClient)) {
            val msg = decode(web)
            assertNotNull(web, msg)
            same(web, BoardWire.encodeString(msg!!))
        }
        // byte for byte too: same keys, same order, numbers the way JS prints them
        assertEquals(webPen, BoardWire.encodeString(decode(webPen)!!))
        assertEquals(webHighlighterByHost, BoardWire.encodeString(decode(webHighlighterByHost)!!))
        assertEquals(webArabicText, BoardWire.encodeString(decode(webArabicText)!!))
        assertEquals(webOldClient, BoardWire.encodeString(decode(webOldClient)!!))
    }

    @Test
    fun `a stroke decodes into the model`() {
        val s = (decode(webPen) as BoardMessage.StrokeMsg).stroke
        assertEquals("k1a2b3c4d5e6f7g8", s.id)
        assertEquals(BoardTool.Pen, s.tool)
        assertEquals(6.0, s.width, 0.0)
        assertEquals(listOf(Pt(0.1234, 0.5), Pt(0.25, 0.6), Pt(1.0, 0.0)), s.points)
        assertNull(s.by)
        assertEquals("teacher-1", (decode(webHighlighterByHost) as BoardMessage.StrokeMsg).stroke.by)
        assertEquals("مرحبا \"بكم\" ✓", (decode(webArabicText) as BoardMessage.StrokeMsg).stroke.text)
        // a stroke from an older client has no tool: it draws as a pen
        val old = (decode(webOldClient) as BoardMessage.StrokeMsg).stroke
        assertNull(old.tool)
        assertEquals(BoardTool.Pen, old.effectiveTool)
    }

    @Test
    fun `drafts, removals, grants, show and clear round-trip`() {
        assertEquals(webDraft, BoardWire.encodeString(decode(webDraft)!!))
        val draft = (decode(webDraft) as BoardMessage.DraftMsg).draft
        assertEquals(BoardTool.Rect, draft.tool)

        assertEquals("""{"t":"wb.remove","ids":["a","b"]}""", BoardWire.encodeString(decode("""{"t":"wb.remove","ids":["a","b"]}""")!!))
        for (to in listOf("\"*\"", "\"student-9\"", "null")) {
            val web = """{"t":"wb.grant","to":$to}"""
            assertEquals(web, BoardWire.encodeString(decode(web)!!))
        }
        assertEquals(BoardMessage.Grant("*"), decode("""{"t":"wb.grant","to":"*"}"""))
        assertEquals(BoardMessage.Grant(null), decode("""{"t":"wb.grant","to":null}"""))
        assertEquals("""{"t":"wb.clear"}""", BoardWire.encodeString(BoardMessage.Clear))
        assertEquals(BoardMessage.Show(true), decode("""{"t":"wb.show","on":true}"""))
        assertEquals("""{"t":"wb.show","on":false}""", BoardWire.encodeString(BoardMessage.Show(false)))
        assertEquals(BoardMessage.SyncRequest, decode("""{"t":"sync.request"}"""))
        assertEquals("""{"t":"sync.request"}""", BoardWire.encodeString(BoardMessage.SyncRequest))
    }

    @Test
    fun `sync state reads the web snapshot and writes one the web reads`() {
        assertEquals(BoardMessage.SyncState(true, 3, "*"), decode(webSync))
        assertEquals(BoardMessage.SyncState(false, 0, null), decode(webSyncEmpty))
        // what a phone host answers a late joiner with is the web's snapshot, the parts it lacks left empty
        assertEquals(webSync, BoardWire.encodeString(BoardMessage.SyncState(true, 3, "*")))
        assertEquals(webSyncEmpty, BoardWire.encodeString(BoardMessage.SyncState(false, 0, null)))
    }

    @Test
    fun `a slides message is seen so the board can step aside`() {
        assertEquals(BoardMessage.SlidesShown(true), decode("""{"t":"slides","slides":{"url":"u","title":"t","page":1}}"""))
        assertEquals(BoardMessage.SlidesShown(false), decode("""{"t":"slides","slides":null}"""))
    }

    @Test
    fun `other messages, unknown types and garbage are ignored, never thrown`() {
        assertNull(decode("""{"t":"hand.clear"}"""))
        assertNull(decode("""{"t":"muted"}"""))
        assertNull(decode("""{"t":"wb.future","x":1}"""))
        assertNull(decode("not json"))
        assertNull(decode("[1,2]"))
        assertNull(decode("""{"t":3}"""))
        assertNull(decode("""{"t":"wb.stroke"}"""))
        assertNull(decode("""{"t":"wb.stroke","stroke":{"id":"x","color":"#000","width":3,"points":[[0.1]]}}"""))
        assertNull(decode("""{"t":"wb.stroke","stroke":{"id":"x","color":"#000","width":3,"points":[["a","b"]]}}"""))
        assertNull(decode("""{"t":"wb.stroke","stroke":{"color":"#000","width":3,"points":[[0,0]]}}"""))
        assertNull(decode("""{"t":"wb.show","on":"yes"}"""))
        assertTrue(BoardWire.decode(ByteArray(0)) == null)
        // unknown keys on a known message are fine
        assertNotNull(decode("""{"t":"wb.clear","extra":true}"""))
        assertNotNull(decode("""{"t":"wb.stroke","stroke":{"id":"x","points":[[0,0]],"future":1},"future":2}"""))
    }

    @Test
    fun `numbers print the way JavaScript prints them`() {
        assertEquals("1", BoardWire.num(1.0))
        assertEquals("0", BoardWire.num(0.0))
        assertEquals("0.5", BoardWire.num(0.5))
        assertEquals("0.0001", BoardWire.num(1.0E-4))
        assertEquals("0.9999", BoardWire.num(0.9999))
        assertEquals("12", BoardWire.num(12.0))
    }

    @Test
    fun `fitStroke keeps a 5000-point stroke under the cap and keeps its end`() {
        // 5000 points, four decimals each, like a finger on a long afternoon.
        val points = (0 until 5000).map { i -> Pt(((i * 37) % 9000 + 1000) / 10_000.0, ((i * 53) % 8000 + 1000) / 10_000.0) }
        val big = Stroke("big", points, "#000000", 6.0)
        assertTrue(BoardWire.encode(BoardMessage.StrokeMsg(big)).size > MAX_MESSAGE_BYTES)
        val fit = fitStroke(big)
        assertTrue(BoardWire.encode(BoardMessage.StrokeMsg(fit)).size <= MAX_MESSAGE_BYTES)
        assertEquals(points.last(), fit.points.last())
        assertEquals(points.first(), fit.points.first())
        // the web's algorithm on 5000 points: 5000 → 2500 → 1250 → 626 (every other point plus the last)
        assertEquals(626, fit.points.size)
        // a short stroke is untouched
        val small = Stroke("s", listOf(Pt(0.1, 0.1), Pt(0.2, 0.2)), "#000000", 6.0)
        assertEquals(small, fitStroke(small))
    }
}
