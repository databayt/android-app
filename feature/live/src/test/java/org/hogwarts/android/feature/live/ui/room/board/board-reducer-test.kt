package org.hogwarts.android.feature.live.ui.room.board

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** hogwarts `class-channel.ts` `applyMessage` (whiteboard cases) and the draft handling of `use-class-channel.ts`. */
class BoardReducerTest {
    private val host = Sender("teacher", isHost = true)
    private val ali = Sender("ali", isHost = false)
    private val sara = Sender("sara", isHost = false)

    private fun stroke(id: String, by: String? = null, text: String? = null, tool: BoardTool? = BoardTool.Pen) =
        Stroke(id, listOf(Pt(0.1, 0.1), Pt(0.2, 0.2)), "#000000", 6.0, tool, text, by)

    private fun apply(s: BoardState, m: BoardMessage, from: Sender) = BoardReducer.apply(s, m, from)

    @Test
    fun `a host draws, a student needs the grant`() {
        var s = BoardState()
        s = apply(s, BoardMessage.StrokeMsg(stroke("a")), host)
        assertEquals(listOf("a"), s.strokes.map { it.id })
        // no grant: a student's stroke changes nothing
        assertSame(s, apply(s, BoardMessage.StrokeMsg(stroke("b")), ali))
        // granted to everyone
        s = apply(s, BoardMessage.Grant("*"), host)
        s = apply(s, BoardMessage.StrokeMsg(stroke("b")), ali)
        assertEquals(listOf("a", "b"), s.strokes.map { it.id })
        // granted to one: only that one
        s = apply(s, BoardMessage.Grant("sara"), host)
        assertSame(s, apply(s, BoardMessage.StrokeMsg(stroke("c")), ali))
        assertEquals(3, apply(s, BoardMessage.StrokeMsg(stroke("c")), sara).strokes.size)
        // taken back
        s = apply(s, BoardMessage.Grant(null), host)
        assertSame(s, apply(s, BoardMessage.StrokeMsg(stroke("d")), sara))
    }

    @Test
    fun `a student cannot grant, clear or show`() {
        val s = BoardState(show = false, strokes = listOf(stroke("a", "teacher")), grant = null)
        assertSame(s, apply(s, BoardMessage.Grant("*"), ali))
        assertSame(s, apply(s, BoardMessage.Clear, ali))
        assertSame(s, apply(s, BoardMessage.Show(true), ali))
        assertTrue(apply(s, BoardMessage.Show(true), host).show)
        assertEquals(emptyList<Stroke>(), apply(s, BoardMessage.Clear, host).strokes)
    }

    @Test
    fun `the author comes from the sender, never from the wire`() {
        var s = apply(BoardState(grant = "*"), BoardMessage.StrokeMsg(stroke("a", by = "teacher")), ali)
        assertEquals("ali", s.strokes.single().by) // a student cannot claim the teacher's work
        // the host replays a late joiner's board: the original author stays
        s = apply(BoardState(), BoardMessage.StrokeMsg(stroke("b", by = "ali")), host)
        assertEquals("ali", s.strokes.single().by)
        // a host's own stroke with no author is theirs
        s = apply(BoardState(), BoardMessage.StrokeMsg(stroke("c")), host)
        assertEquals("teacher", s.strokes.single().by)
    }

    @Test
    fun `a stroke id lands once`() {
        val s = apply(BoardState(), BoardMessage.StrokeMsg(stroke("a")), host)
        assertSame(s, apply(s, BoardMessage.StrokeMsg(stroke("a")), host))
    }

    @Test
    fun `text is cut to 200 characters`() {
        val s = apply(BoardState(), BoardMessage.StrokeMsg(stroke("t", text = "x".repeat(500), tool = BoardTool.Text)), host)
        assertEquals(200, s.strokes.single().text!!.length)
    }

    @Test
    fun `a student removes only their own, a host removes anything`() {
        val base = BoardState(
            grant = "*",
            strokes = listOf(stroke("mine", "ali"), stroke("sara1", "sara"), stroke("teacher1", "teacher")),
        )
        // ali tries to erase everyone's work: only his own goes
        val afterAli = apply(base, BoardMessage.Remove(listOf("mine", "sara1", "teacher1")), ali)
        assertEquals(listOf("sara1", "teacher1"), afterAli.strokes.map { it.id })
        // a foreign removal alone changes nothing at all
        assertSame(base, apply(base, BoardMessage.Remove(listOf("sara1")), ali))
        // the host erases anything
        assertEquals(emptyList<Stroke>(), apply(base, BoardMessage.Remove(listOf("mine", "sara1", "teacher1")), host).strokes)
        // without the grant a student removes nothing, even their own
        val ungranted = base.copy(grant = null)
        assertSame(ungranted, apply(ungranted, BoardMessage.Remove(listOf("mine")), ali))
    }

    @Test
    fun `sync state from the host sets the board, from a student nothing`() {
        val strokes = listOf(stroke("a", "teacher"))
        val s = BoardState(strokes = strokes)
        assertSame(s, apply(s, BoardMessage.SyncState(true, 1, "*"), ali))
        val synced = apply(s, BoardMessage.SyncState(true, 1, "*"), host)
        assertTrue(synced.show)
        assertEquals("*", synced.grant)
        assertEquals(strokes, synced.strokes) // strokes follow, one message each
        // an empty board clears the phone's strokes; an absent grant means no grant
        val cleared = apply(synced, BoardMessage.SyncState(false, 0, null), host)
        assertEquals(emptyList<Stroke>(), cleared.strokes)
        assertFalse(cleared.show)
        assertNull(cleared.grant)
        // a count that was left out leaves the strokes alone
        assertEquals(strokes, apply(s, BoardMessage.SyncState(true, -1, null), host).strokes)
    }

    @Test
    fun `slides from the host fold the board away`() {
        val s = BoardState(show = true)
        assertTrue(apply(s, BoardMessage.SlidesShown(true), ali).show)
        assertFalse(apply(s, BoardMessage.SlidesShown(true), host).show)
        assertTrue(apply(s, BoardMessage.SlidesShown(false), host).show)
    }

    private fun draft(id: String, tool: BoardTool, vararg pts: Pt) = DraftStroke(id, pts.toList(), "#000000", 6.0, tool)

    @Test
    fun `drafts need the board up and a hand that may draw`() {
        val up = BoardState(show = true)
        val d = draft("d1", BoardTool.Pen, Pt(0.1, 0.1))
        assertTrue(BoardReducer.applyDraft(up, d, host, 0).drafts.containsKey("d1"))
        assertTrue(BoardReducer.applyDraft(up, d, ali, 0).drafts.isEmpty()) // not granted
        assertTrue(BoardReducer.applyDraft(up.copy(grant = "*"), d, ali, 0).drafts.containsKey("d1"))
        assertTrue(BoardReducer.applyDraft(BoardState(show = false), d, host, 0).drafts.isEmpty()) // board is down
    }

    @Test
    fun `pen drafts append, shape drafts replace`() {
        var s = BoardState(show = true)
        s = BoardReducer.applyDraft(s, draft("p", BoardTool.Pen, Pt(0.1, 0.1), Pt(0.2, 0.2)), host, 0)
        s = BoardReducer.applyDraft(s, draft("p", BoardTool.Pen, Pt(0.2, 0.2), Pt(0.3, 0.3)), host, 10)
        assertEquals(4, s.drafts.getValue("p").draft.points.size)
        s = BoardReducer.applyDraft(s, draft("r", BoardTool.Rect, Pt(0.1, 0.1), Pt(0.4, 0.4)), host, 20)
        s = BoardReducer.applyDraft(s, draft("r", BoardTool.Rect, Pt(0.1, 0.1), Pt(0.6, 0.6)), host, 30)
        assertEquals(listOf(Pt(0.1, 0.1), Pt(0.6, 0.6)), s.drafts.getValue("r").draft.points)
    }

    @Test
    fun `a landed stroke replaces its draft and old drafts expire`() {
        var s = BoardState(show = true)
        s = BoardReducer.applyDraft(s, draft("p", BoardTool.Pen, Pt(0.1, 0.1)), host, 0)
        val landed = apply(s, BoardMessage.StrokeMsg(stroke("p")), host)
        assertTrue(landed.drafts.isEmpty())
        // a late draft for a stroke that already landed is dropped
        assertSame(landed, BoardReducer.applyDraft(landed, draft("p", BoardTool.Pen, Pt(0.2, 0.2)), host, 5))
        assertEquals(1, BoardReducer.pruneDrafts(s, BoardReducer.DRAFT_TTL_MS).drafts.size)
        assertTrue(BoardReducer.pruneDrafts(s, BoardReducer.DRAFT_TTL_MS + 1).drafts.isEmpty())
    }

    @Test
    fun `the draft streamer sends at most every 50 ms and only the new points`() {
        val streamer = DraftStreamer()
        val pts = (0 until 200).map { Pt(it / 200.0, 0.5) }
        fun d(n: Int) = DraftStroke("x", pts.take(n), "#000000", 6.0, BoardTool.Pen)
        val first = streamer.next(d(10), 1_000)!!
        assertEquals(10, first.points.size)
        assertNull(streamer.next(d(20), 1_020)) // too soon
        val second = streamer.next(d(20), 1_060)!!
        assertEquals(pts[9], second.points.first()) // one point of overlap, no gap
        assertEquals(11, second.points.size)
        // a long run goes in 60-point packets
        assertEquals(60, streamer.next(d(200), 1_200)!!.points.size)
        // shapes go whole
        val shape = DraftStroke("r", listOf(Pt(0.1, 0.1), Pt(0.5, 0.5)), "#000000", 6.0, BoardTool.Rect)
        assertEquals(shape, DraftStreamer().next(shape, 5_000))
        // force ignores the clock
        assertEquals(11, DraftStreamer().apply { next(d(10), 0, force = true) }.next(d(20), 1, force = true)!!.points.size)
    }
}
