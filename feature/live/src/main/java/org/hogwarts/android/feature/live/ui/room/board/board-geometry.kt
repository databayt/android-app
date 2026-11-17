package org.hogwarts.android.feature.live.ui.room.board

/**
 * The board's maths with no Compose in it — hogwarts `live/room/board/board-canvas.ts`, ported
 * line for line: page geometry, point thinning and what the eraser hits.
 */

/** The page is always 16:9 — the shape of a projector and of a laptop. */
const val PAGE_RATIO = 16.0 / 9.0

/** Eight inks that read on white. The first is the default. */
val BOARD_COLORS = listOf("#1f2937", "#ef4444", "#f97316", "#eab308", "#22c55e", "#3b82f6", "#a855f7", "#ec4899")

/** Stroke widths in thousandths of the page width: S · M · L. */
val BOARD_WIDTHS = listOf(3.0, 6.0, 12.0)

/** Strokes kept when the board is saved — past this the oldest go. */
const val MAX_STROKES = 400

data class PageRect(val x: Double, val y: Double, val w: Double, val h: Double)

/** The largest 16:9 page that fits the box, centred, with a margin. */
fun pageRect(boxW: Double, boxH: Double, margin: Double = 8.0): PageRect {
    val availW = maxOf(0.0, boxW - margin * 2)
    val availH = maxOf(0.0, boxH - margin * 2)
    var w = availW
    var h = w / PAGE_RATIO
    if (h > availH) {
        h = availH
        w = h * PAGE_RATIO
    }
    return PageRect((boxW - w) / 2, (boxH - h) / 2, w, h)
}

/** A point in the box → page coordinates, clamped onto the page, four decimals. */
fun toPage(px: Double, py: Double, page: PageRect): Pt {
    val x = if (page.w != 0.0) (px - page.x) / page.w else 0.0
    val y = if (page.h != 0.0) (py - page.y) / page.h else 0.0
    return Pt(round4(clamp01(x)), round4(clamp01(y)))
}

private fun clamp01(v: Double) = if (v < 0) 0.0 else if (v > 1) 1.0 else v

/** Four decimals: a 10,000-step page, far finer than any screen (`Math.round` rounds half up, as JS does). */
private fun round4(v: Double): Double = Math.round(v * 10_000) / 10_000.0

/** Drop points closer than [min] (page units) to the last kept one. True when [p] was kept. */
fun addPoint(points: MutableList<Pt>, p: Pt, min: Double = 0.0015): Boolean {
    val last = points.lastOrNull()
    if (last != null && Math.hypot(p.x - last.x, (p.y - last.y) / PAGE_RATIO) < min) return false
    points.add(p)
    return true
}

/** The `text` tool's font size in px for a page [w] wide. S/M/L → 18/28/44px on a 1000px page. */
fun textSize(width: Double, w: Double): Double {
    val pt = if (width <= 3) 18.0 else if (width <= 6) 28.0 else 44.0
    return pt * w / 1000
}

private val ARABIC = Regex("[\\u0600-\\u06FF]")

internal fun isArabic(text: String): Boolean = ARABIC.containsMatchIn(text)

private typealias Seg = Pair<Pt, Pt>

/** Distance from p to segment ab, in page-width units (y scaled to match). */
private fun segDist(p: Pt, a: Pt, b: Pt): Double {
    val k = 1 / PAGE_RATIO
    val px = p.x
    val py = p.y * k
    val ax = a.x
    val ay = a.y * k
    val dx = b.x - ax
    val dy = b.y * k - ay
    val len = dx * dx + dy * dy
    val t = if (len != 0.0) maxOf(0.0, minOf(1.0, ((px - ax) * dx + (py - ay) * dy) / len)) else 0.0
    return Math.hypot(px - (ax + t * dx), py - (ay + t * dy))
}

/** The stroke's outline as segments — what the eraser tests against. */
private fun outline(s: Stroke): List<Seg> {
    val pts = s.points
    if (pts.isEmpty()) return emptyList()
    val a = pts.first()
    val b = pts.last()
    return when (s.effectiveTool) {
        BoardTool.Line, BoardTool.Arrow -> listOf(a to b)
        BoardTool.Rect -> rectOutline(a, b)
        BoardTool.Ellipse -> {
            val cx = (a.x + b.x) / 2
            val cy = (a.y + b.y) / 2
            val rx = Math.abs(b.x - a.x) / 2
            val ry = Math.abs(b.y - a.y) / 2
            val ring = (0..32).map { i ->
                val t = i.toDouble() / 32 * Math.PI * 2
                Pt(cx + rx * Math.cos(t), cy + ry * Math.sin(t))
            }
            ring.drop(1).mapIndexed { i, p -> ring[i] to p }
        }
        BoardTool.Text -> {
            // A box the text roughly fills: 0.55em per character, one line tall.
            val em = textSize(s.width, 1000.0) / 1000
            val wPage = (s.text?.length ?: 1) * em * 0.55
            val hPage = em * PAGE_RATIO * 1.2
            val x0 = if (isArabic(s.text.orEmpty())) a.x - wPage else a.x
            rectOutline(Pt(x0, a.y), Pt(x0 + wPage, a.y + hPage))
        }
        else ->
            if (pts.size == 1) listOf(a to a) else pts.drop(1).mapIndexed { i, p -> pts[i] to p }
    }
}

private fun rectOutline(a: Pt, b: Pt): List<Seg> {
    val c = Pt(b.x, a.y)
    val d = Pt(a.x, b.y)
    return listOf(a to c, c to b, b to d, d to a)
}

/**
 * Does the eraser at [p] (radius [r], page-width units) touch the stroke? Text also counts when
 * the point is inside its box.
 */
fun hitStroke(s: Stroke, p: Pt, r: Double): Boolean {
    val reach = r + (s.width / 1000) * (if (s.tool == BoardTool.Highlighter) 2.0 else 0.5)
    val segs = outline(s)
    if (s.tool == BoardTool.Text && segs.isNotEmpty()) {
        val xs = segs.map { it.first.x }
        val ys = segs.map { it.first.y }
        if (p.x >= xs.min() && p.x <= xs.max() && p.y >= ys.min() && p.y <= ys.max()) return true
    }
    return segs.any { (a, b) -> segDist(p, a, b) <= reach }
}
