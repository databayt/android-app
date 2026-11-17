package org.hogwarts.android.feature.live.ui.room.board

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** hogwarts `board/board-canvas.ts`: the page, the point mapping, the thinning and the eraser. */
class BoardGeometryTest {
    private val eps = 1e-9

    @Test
    fun `the page is the largest 16 by 9 that fits, centred, with a margin`() {
        // a wide box: the height decides
        val wide = pageRect(1000.0, 400.0)
        assertEquals(384.0, wide.h, eps)
        assertEquals(384.0 * 16 / 9, wide.w, eps)
        assertEquals((1000 - wide.w) / 2, wide.x, eps)
        assertEquals(8.0, wide.y, eps)
        // a tall box (a phone upright): the width decides
        val tall = pageRect(360.0, 800.0)
        assertEquals(344.0, tall.w, eps)
        assertEquals(344.0 * 9 / 16, tall.h, eps)
        assertEquals(8.0, tall.x, eps)
        assertEquals((800 - tall.h) / 2, tall.y, eps)
        // nothing to draw in: an empty page, not a negative one
        val none = pageRect(10.0, 10.0)
        assertEquals(0.0, none.w, eps)
        assertEquals(0.0, none.h, eps)
    }

    @Test
    fun `a touch maps onto the page, clamped, four decimals`() {
        val page = PageRect(10.0, 20.0, 160.0, 90.0)
        assertEquals(Pt(0.0, 0.0), toPage(10.0, 20.0, page))
        assertEquals(Pt(1.0, 1.0), toPage(170.0, 110.0, page))
        assertEquals(Pt(0.5, 0.5), toPage(90.0, 65.0, page))
        assertEquals(Pt(0.0, 1.0), toPage(-50.0, 500.0, page)) // off the page lands on its edge
        assertEquals(Pt(0.3333, 0.1111), toPage(10 + 160 / 3.0, 30.0, page))
        assertEquals(Pt(0.0, 0.0), toPage(5.0, 5.0, PageRect(0.0, 0.0, 0.0, 0.0)))
    }

    @Test
    fun `points closer than the minimum are dropped`() {
        val pts = mutableListOf<Pt>()
        assertTrue(addPoint(pts, Pt(0.5, 0.5)))
        assertFalse(addPoint(pts, Pt(0.5005, 0.5)))
        assertTrue(addPoint(pts, Pt(0.502, 0.5)))
        // y counts in page-width units (a 16:9 page is shorter than it is wide)
        assertFalse(addPoint(pts, Pt(0.502, 0.5 + 0.0015 * 9 / 16 / 2)))
        assertEquals(2, pts.size)
    }

    @Test
    fun `the text size follows the width setting and the page`() {
        assertEquals(18.0, textSize(3.0, 1000.0), eps)
        assertEquals(28.0, textSize(6.0, 1000.0), eps)
        assertEquals(44.0, textSize(12.0, 1000.0), eps)
        assertEquals(22.0, textSize(6.0, 785.7142857142857), 0.001)
    }

    private fun s(tool: BoardTool?, vararg pts: Pt, width: Double = 6.0, text: String? = null) =
        Stroke("x", pts.toList(), "#000000", width, tool, text)

    @Test
    fun `the eraser hits a pen line near it and misses far away`() {
        val line = s(BoardTool.Pen, Pt(0.1, 0.5), Pt(0.5, 0.5), Pt(0.9, 0.5))
        assertTrue(hitStroke(line, Pt(0.3, 0.5), 0.012))
        assertTrue(hitStroke(line, Pt(0.3, 0.505), 0.012))
        assertFalse(hitStroke(line, Pt(0.3, 0.6), 0.012))
        assertFalse(hitStroke(line, Pt(0.95, 0.5), 0.012))
        // a tap is a dot
        val dot = s(null, Pt(0.4, 0.4))
        assertTrue(hitStroke(dot, Pt(0.405, 0.4), 0.012))
        assertFalse(hitStroke(dot, Pt(0.6, 0.4), 0.012))
    }

    @Test
    fun `a highlighter is hit from further away than a pen`() {
        val pen = s(BoardTool.Pen, Pt(0.1, 0.5), Pt(0.9, 0.5), width = 12.0)
        val hl = s(BoardTool.Highlighter, Pt(0.1, 0.5), Pt(0.9, 0.5), width = 12.0)
        val p = Pt(0.5, 0.5 + 0.03 * 16 / 9)
        assertFalse(hitStroke(pen, p, 0.012))
        assertTrue(hitStroke(hl, p, 0.012))
    }

    @Test
    fun `a rectangle is hit on its edge, not inside`() {
        val r = s(BoardTool.Rect, Pt(0.2, 0.2), Pt(0.8, 0.8))
        assertTrue(hitStroke(r, Pt(0.5, 0.2), 0.012))
        assertTrue(hitStroke(r, Pt(0.8, 0.5), 0.012))
        assertFalse(hitStroke(r, Pt(0.5, 0.5), 0.012))
    }

    @Test
    fun `an ellipse is hit on its ring`() {
        val e = s(BoardTool.Ellipse, Pt(0.2, 0.2), Pt(0.8, 0.8))
        assertTrue(hitStroke(e, Pt(0.8, 0.5), 0.012))
        assertTrue(hitStroke(e, Pt(0.5, 0.2), 0.012))
        assertFalse(hitStroke(e, Pt(0.5, 0.5), 0.012))
        assertFalse(hitStroke(e, Pt(0.2, 0.2), 0.012)) // a corner of the box is off the ring
    }

    @Test
    fun `text is hit anywhere inside its box, Arabic from the right`() {
        val latin = s(BoardTool.Text, Pt(0.3, 0.4), width = 12.0, text = "hello")
        assertTrue(hitStroke(latin, Pt(0.32, 0.41), 0.012))
        assertFalse(hitStroke(latin, Pt(0.1, 0.41), 0.012))
        val arabic = s(BoardTool.Text, Pt(0.7, 0.4), width = 12.0, text = "مرحبا")
        assertTrue(hitStroke(arabic, Pt(0.68, 0.41), 0.012)) // the box runs to the left of the anchor
        assertFalse(hitStroke(arabic, Pt(0.75, 0.41), 0.012))
    }

    @Test
    fun `a stroke with no points is hit by nothing`() {
        assertFalse(hitStroke(s(BoardTool.Pen), Pt(0.5, 0.5), 0.012))
    }

    @Test
    fun `the palette is the web's`() {
        assertEquals(8, BOARD_COLORS.size)
        assertEquals("#1f2937", BOARD_COLORS[0])
        assertEquals(listOf(3.0, 6.0, 12.0), BOARD_WIDTHS)
    }
}
