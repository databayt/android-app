package org.hogwarts.android.feature.live.ui.room.board

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas as GraphicsCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke as StrokeStyle
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/** The page's margin inside the box, in dp (the web's 8 CSS px). */
private const val PAGE_MARGIN_DP = 8f
private const val PAGE_RADIUS_DP = 12f
private const val ARROW_HEAD_MIN_DP = 10f
private const val HIGHLIGHTER_ALPHA = 0.35f

private val FALLBACK_INK = Color(0xFF1F2937)

/** `#rgb`, `#rrggbb` and `#aarrggbb` from the wire; anything else draws in the default ink. */
internal fun inkColor(hex: String): Color {
    val h = if (hex.length == 4 && hex[0] == '#') "#" + hex.drop(1).map { "$it$it" }.joinToString("") else hex
    return runCatching { Color(android.graphics.Color.parseColor(h)) }.getOrDefault(FALLBACK_INK)
}

/**
 * Paint one stroke into a scope whose origin is the page's top-left and whose unit is a pixel
 * (`w × h` is the page) — the web's `drawStroke`, tool for tool. The committed layer and the live
 * layer both call this, so a stroke looks the same while it forms and after it lands.
 */
internal fun DrawScope.drawInk(
    points: List<Pt>,
    colorHex: String,
    width: Double,
    tool: BoardTool,
    text: String?,
    w: Float,
    h: Float,
    arrowHeadMin: Float,
) {
    if (points.isEmpty()) return
    val ink = inkColor(colorHex)
    val lw = max(1f, (width * w / 1000).toFloat())
    val a = points.first()
    val b = points.last()
    val ax = (a.x * w).toFloat()
    val ay = (a.y * h).toFloat()
    val bx = (b.x * w).toFloat()
    val by = (b.y * h).toFloat()

    if (tool == BoardTool.Text) {
        if (!text.isNullOrEmpty()) drawBoardText(text, ink, textSize(width, w.toDouble()).toFloat(), ax, ay)
        return
    }

    val highlighter = tool == BoardTool.Highlighter
    val strokeWidth = if (highlighter) lw * 4 else lw
    val alpha = if (highlighter) HIGHLIGHTER_ALPHA else 1f
    val blend = if (highlighter) BlendMode.Multiply else BlendMode.SrcOver
    val style = StrokeStyle(
        width = strokeWidth,
        cap = if (highlighter) StrokeCap.Butt else StrokeCap.Round,
        join = StrokeJoin.Round,
    )

    when {
        tool == BoardTool.Line || tool == BoardTool.Arrow -> {
            val path = Path().apply {
                moveTo(ax, ay)
                lineTo(bx, by)
                if (tool == BoardTool.Arrow) {
                    val ang = atan2((by - ay).toDouble(), (bx - ax).toDouble())
                    val head = max(arrowHeadMin, lw * 4)
                    for (d in doubleArrayOf(-0.45, 0.45)) {
                        moveTo(bx, by)
                        lineTo(bx - head * cos(ang + d).toFloat(), by - head * sin(ang + d).toFloat())
                    }
                }
            }
            drawPath(path, ink, alpha, style, blendMode = blend)
        }
        tool == BoardTool.Rect -> drawRect(
            ink,
            topLeft = Offset(minOf(ax, bx), minOf(ay, by)),
            size = Size(Math.abs(bx - ax), Math.abs(by - ay)),
            alpha = alpha, style = style, blendMode = blend,
        )
        tool == BoardTool.Ellipse -> drawOval(
            ink,
            topLeft = Offset(minOf(ax, bx), minOf(ay, by)),
            size = Size(Math.abs(bx - ax), Math.abs(by - ay)),
            alpha = alpha, style = style, blendMode = blend,
        )
        // A tap is a dot.
        points.size == 1 -> drawCircle(ink, radius = strokeWidth / 2, center = Offset(ax, ay), alpha = alpha, style = Fill, blendMode = blend)
        else -> {
            // Smooth: a quadratic through each point, ending at the midpoints.
            val path = Path().apply {
                moveTo(ax, ay)
                for (i in 1 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    quadraticTo(
                        (p0.x * w).toFloat(), (p0.y * h).toFloat(),
                        (((p0.x + p1.x) / 2) * w).toFloat(), (((p0.y + p1.y) / 2) * h).toFloat(),
                    )
                }
                lineTo(bx, by)
            }
            drawPath(path, ink, alpha, style, blendMode = blend)
        }
    }
}

private val boardTypeface: Typeface by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) Typeface.create(Typeface.DEFAULT, 600, false) else Typeface.DEFAULT_BOLD
}

/** The text tool: semi-bold system sans, top-anchored at the point, right-anchored for Arabic. */
private fun DrawScope.drawBoardText(text: String, ink: Color, size: Float, x: Float, y: Float) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink.toArgb()
        textSize = size
        typeface = boardTypeface
        textAlign = if (isArabic(text)) Paint.Align.RIGHT else Paint.Align.LEFT
    }
    // `textBaseline = "top"`: the baseline sits one ascent below the anchor.
    drawContext.canvas.nativeCanvas.drawText(text, x, y - paint.ascent(), paint)
}

private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255 + 0.5f).toInt(), (red * 255 + 0.5f).toInt(), (green * 255 + 0.5f).toInt(), (blue * 255 + 0.5f).toInt(),
)

/**
 * The committed strokes, painted into one bitmap and kept: a new stroke is drawn onto it alone,
 * so 300 strokes cost one `drawImage` a frame. A removal or a resize repaints from scratch.
 */
internal class StrokeLayer {
    private var bitmap: ImageBitmap? = null
    private var canvas: GraphicsCanvas? = null
    private var drawn: List<Stroke> = emptyList()
    private var w = 0f
    private var h = 0f

    fun update(strokes: List<Stroke>, w: Float, h: Float, density: Float): ImageBitmap {
        val current = bitmap
        val prefix = this.w == w && this.h == h &&
            strokes.size >= drawn.size && drawn.indices.all { strokes[it] === drawn[it] }
        val target: ImageBitmap
        val from: Int
        if (prefix && current != null) {
            target = current
            from = drawn.size
        } else {
            target = ImageBitmap(ceil(w).toInt().coerceAtLeast(1), ceil(h).toInt().coerceAtLeast(1))
            canvas = GraphicsCanvas(target)
            from = 0
        }
        val gc = canvas
        if (gc != null && from < strokes.size) {
            CanvasDrawScope().draw(Density(density), LayoutDirection.Ltr, gc, Size(target.width.toFloat(), target.height.toFloat())) {
                for (i in from until strokes.size) {
                    val s = strokes[i]
                    drawInk(s.points, s.color, s.width, s.effectiveTool, s.text, w, h, ARROW_HEAD_MIN_DP * density)
                }
            }
        }
        bitmap = target
        drawn = strokes
        this.w = w
        this.h = h
        return target
    }
}

/** The stroke being drawn right now: points are appended in place, [tick] says it changed. */
private class Mine(val id: String, val tool: BoardTool, val color: String, val width: Double, val points: MutableList<Pt>)

/**
 * The page: white, 16:9, letterboxed in whatever box it is given, with the committed layer cached
 * and everyone's live strokes drawn on top. With [canDraw] the finger draws (or erases) in page
 * coordinates; [onDraft] is called at most every 50 ms with what to stream, [onStroke] once on lift.
 */
@Composable
internal fun BoardCanvas(
    strokes: List<Stroke>,
    drafts: List<DraftStroke>,
    canDraw: Boolean,
    eraser: Boolean,
    tool: BoardTool,
    color: String,
    width: Double,
    isHost: Boolean,
    localIdentity: String,
    description: String,
    onStroke: (Stroke) -> Unit,
    onDraft: (DraftStroke) -> Unit,
    onErase: (List<Stroke>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val layer = remember { StrokeLayer() }
    val streamer = remember { DraftStreamer() }
    var tick by remember { mutableIntStateOf(0) }
    val mine = remember { arrayOfNulls<Mine>(1) }

    val strokesNow by rememberUpdatedState(strokes)
    val eraserNow by rememberUpdatedState(eraser)
    val toolNow by rememberUpdatedState(tool)
    val colorNow by rememberUpdatedState(color)
    val widthNow by rememberUpdatedState(width)
    val isHostNow by rememberUpdatedState(isHost)
    val identityNow by rememberUpdatedState(localIdentity)
    val onStrokeNow by rememberUpdatedState(onStroke)
    val onDraftNow by rememberUpdatedState(onDraft)
    val onEraseNow by rememberUpdatedState(onErase)

    Canvas(
        modifier
            .semantics { contentDescription = description }
            .pointerInput(canDraw) {
                if (!canDraw) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val page = pageRect(size.width.toDouble(), size.height.toDouble(), (PAGE_MARGIN_DP * density).toDouble())
                    fun at(o: Offset) = toPage(o.x.toDouble(), o.y.toDouble(), page)
                    down.consume()

                    val erasing = eraserNow
                    val erased = HashSet<String>()
                    fun erase(p: Pt) {
                        val hits = strokesNow.filter {
                            it.id !in erased && (isHostNow || it.by == identityNow) && hitStroke(it, p, ERASER_RADIUS)
                        }
                        if (hits.isEmpty()) return
                        hits.forEach { erased += it.id }
                        onEraseNow(hits)
                    }

                    val first = at(down.position)
                    val m: Mine? = if (erasing) {
                        erase(first)
                        null
                    } else {
                        streamer.reset()
                        Mine(
                            newStrokeId(), toolNow, colorNow, widthNow,
                            (if (toolNow.isShape) mutableListOf(first, first) else mutableListOf(first)),
                        ).also { mine[0] = it; tick++ }
                    }

                    fun stream(force: Boolean) {
                        val cur = m ?: return
                        val packet = streamer.next(
                            DraftStroke(cur.id, cur.points, cur.color, cur.width, cur.tool), System.currentTimeMillis(), force,
                        )
                        if (packet != null) onDraftNow(packet)
                    }

                    fun move(o: Offset) {
                        val p = at(o)
                        if (erasing) {
                            erase(p)
                            return
                        }
                        val cur = m ?: return
                        if (cur.tool.isShape) {
                            cur.points[1] = p
                        } else {
                            addPoint(cur.points, p)
                        }
                    }

                    var lifted = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            lifted = true
                            change.consume()
                            break
                        }
                        for (h in change.historical) move(h.position)
                        move(change.position)
                        change.consume()
                        if (m != null) {
                            tick++
                            stream(force = false)
                        }
                    }

                    mine[0] = null
                    tick++
                    val cur = m ?: return@awaitEachGesture
                    if (!lifted) return@awaitEachGesture
                    val a = cur.points.first()
                    val b = cur.points.last()
                    // A shape with no size is a stray tap.
                    if (cur.tool.isShape && Math.hypot(b.x - a.x, b.y - a.y) < 0.004) return@awaitEachGesture
                    stream(force = true)
                    onStrokeNow(Stroke(cur.id, cur.points.toList(), cur.color, cur.width, cur.tool))
                }
            },
    ) {
        tick // the stroke in progress changed
        val margin = PAGE_MARGIN_DP * density
        val page = pageRect(size.width.toDouble(), size.height.toDouble(), margin.toDouble())
        val pw = page.w.toFloat()
        val ph = page.h.toFloat()
        if (pw < 1f || ph < 1f) return@Canvas
        val left = page.x.toFloat()
        val top = page.y.toFloat()
        val radius = PAGE_RADIUS_DP * density
        val corners = Path().apply { addRoundRect(RoundRect(Rect(left, top, left + pw, top + ph), CornerRadius(radius))) }
        clipPath(corners) {
            drawRect(Color.White, Offset(left, top), Size(pw, ph))
            drawImage(layer.update(strokes, pw, ph, density), Offset(left, top))
            translate(left, top) {
                for (d in drafts) drawInk(d.points, d.color, d.width, d.tool, null, pw, ph, ARROW_HEAD_MIN_DP * density)
                mine[0]?.let { drawInk(it.points, it.color, it.width, it.tool, null, pw, ph, ARROW_HEAD_MIN_DP * density) }
            }
        }
    }
}

/** The eraser's reach, in page-width units (the web's `hitStroke(s, p, 0.012)`). */
private const val ERASER_RADIUS = 0.012

/** The web's `newId`: base-36 time plus random, cut to 16 characters. */
internal fun newStrokeId(): String =
    (java.lang.Long.toString(System.currentTimeMillis(), 36) +
        java.lang.Long.toString(Math.abs(kotlin.random.Random.nextLong()), 36).take(6)).take(16)
