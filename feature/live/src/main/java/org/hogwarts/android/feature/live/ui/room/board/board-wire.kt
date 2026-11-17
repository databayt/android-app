package org.hogwarts.android.feature.live.ui.room.board

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import java.math.BigDecimal

/**
 * The whiteboard's wire, so a phone and a laptop draw on one board. Source of truth, hogwarts
 * `live/room/class-channel.ts`: JSON `{ "t": … }` on data topic `lc`, reliable, capped at
 * [MAX_MESSAGE_BYTES]. The encoder writes the same keys in the same order as `JSON.stringify`
 * does for the web's objects; the decoder is tolerant (unknown `t`, unknown keys and malformed
 * payloads are ignored, never thrown).
 */

/** Reliable data messages are capped around 15 KB; stay well under it (the web's `MAX_MESSAGE_BYTES`). */
const val MAX_MESSAGE_BYTES = 12_000

/** What made a stroke. Missing on strokes from older clients, which draw as [Pen]. */
enum class BoardTool(val wire: String) {
    Pen("pen"), Highlighter("highlighter"), Line("line"), Arrow("arrow"), Rect("rect"), Ellipse("ellipse"), Text("text");

    val isShape: Boolean get() = this == Line || this == Arrow || this == Rect || this == Ellipse

    companion object {
        fun fromWire(value: String?): BoardTool? = entries.firstOrNull { it.wire == value }
    }
}

/** A point on the board's fixed 16:9 page: x is 0..1 of the page width, y 0..1 of its height. */
data class Pt(val x: Double, val y: Double)

/** One committed stroke. [width] is thousandths of the page width; [by] is set by the reducer, never trusted. */
data class Stroke(
    val id: String,
    val points: List<Pt>,
    val color: String,
    val width: Double,
    val tool: BoardTool? = null,
    /** The text tool's words, anchored at the first point. */
    val text: String? = null,
    val by: String? = null,
) {
    val effectiveTool: BoardTool get() = tool ?: BoardTool.Pen
}

/**
 * A stroke still being drawn, streamed lossy. Pen and highlighter drafts carry only the points
 * since the last draft (the receiver appends); shape drafts carry both corners and replace.
 */
data class DraftStroke(
    val id: String,
    val points: List<Pt>,
    val color: String,
    val width: Double,
    val tool: BoardTool,
)

/** Who besides the hosts may draw: [EVERYONE], one identity, or null for nobody. */
const val GRANT_EVERYONE = "*"

sealed interface BoardMessage {
    data class StrokeMsg(val stroke: Stroke) : BoardMessage
    data class DraftMsg(val draft: DraftStroke) : BoardMessage
    data class Remove(val ids: List<String>) : BoardMessage
    data class Grant(val to: String?) : BoardMessage
    data object Clear : BoardMessage
    data class Show(val on: Boolean) : BoardMessage
    /** The host put slides up (or took them down): slides and the board are never up together. */
    data class SlidesShown(val active: Boolean) : BoardMessage
    data object SyncRequest : BoardMessage
    /** What a board needs of the web's `sync.state`; [strokeCount] is -1 when the sender left it out. */
    data class SyncState(val whiteboard: Boolean, val strokeCount: Int, val grant: String?) : BoardMessage
}

object BoardWire {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(msg: BoardMessage): ByteArray = encodeString(msg).toByteArray(Charsets.UTF_8)

    fun encodeString(msg: BoardMessage): String = when (msg) {
        is BoardMessage.StrokeMsg -> """{"t":"wb.stroke","stroke":${strokeJson(msg.stroke)}}"""
        is BoardMessage.DraftMsg -> """{"t":"wb.draft","draft":${draftJson(msg.draft)}}"""
        is BoardMessage.Remove -> """{"t":"wb.remove","ids":[${msg.ids.joinToString(",") { quote(it) }}]}"""
        is BoardMessage.Grant -> """{"t":"wb.grant","to":${msg.to?.let { quote(it) } ?: "null"}}"""
        BoardMessage.Clear -> """{"t":"wb.clear"}"""
        is BoardMessage.Show -> """{"t":"wb.show","on":${msg.on}}"""
        is BoardMessage.SlidesShown -> if (msg.active) """{"t":"slides","slides":{}}""" else """{"t":"slides","slides":null}"""
        BoardMessage.SyncRequest -> """{"t":"sync.request"}"""
        // The web's `snapshotOf`, with the parts a phone does not hold left empty.
        is BoardMessage.SyncState ->
            """{"t":"sync.state","questions":[],"poll":null,"slides":null,"whiteboard":${msg.whiteboard},""" +
                """"strokeCount":${msg.strokeCount},"grant":${msg.grant?.let { quote(it) } ?: "null"},""" +
                """"spotlight":null,"timer":null,"quiz":null}"""
    }

    /** A stroke as the web writes it: `id, tool, color, width, points, text, by`. */
    fun strokeJson(s: Stroke): String = buildString {
        append("""{"id":""").append(quote(s.id))
        s.tool?.let { append(""","tool":""").append(quote(it.wire)) }
        append(""","color":""").append(quote(s.color))
        append(""","width":""").append(num(s.width))
        append(""","points":""").append(pointsJson(s.points))
        s.text?.let { append(""","text":""").append(quote(it)) }
        s.by?.let { append(""","by":""").append(quote(it)) }
        append('}')
    }

    private fun draftJson(d: DraftStroke): String =
        """{"id":${quote(d.id)},"tool":${quote(d.tool.wire)},"color":${quote(d.color)},"width":${num(d.width)},"points":${pointsJson(d.points)}}"""

    private fun pointsJson(points: List<Pt>): String =
        points.joinToString(",", "[", "]") { "[${num(it.x)},${num(it.y)}]" }

    /** A stroke as a JSON tree, for the saved board's body. */
    fun strokeElement(s: Stroke): JsonElement = json.parseToJsonElement(strokeJson(s))

    private fun quote(s: String): String = JsonPrimitive(s).toString()

    /** JS prints 1 as `1` and 0.5 as `0.5`: no `.0`, no exponent. */
    internal fun num(d: Double): String = when {
        d.isNaN() || d.isInfinite() -> "0"
        d == Math.rint(d) && Math.abs(d) < 1e15 -> d.toLong().toString()
        else -> BigDecimal(d.toString()).stripTrailingZeros().toPlainString()
    }

    /**
     * The class message in [payload] if it is one the board cares about, else null: another
     * message type, an unknown one, or a payload that is not valid for its type. Never throws.
     */
    fun decode(payload: ByteArray): BoardMessage? = runCatching {
        val obj = json.parseToJsonElement(payload.toString(Charsets.UTF_8)) as? JsonObject ?: return@runCatching null
        decodeObject(obj)
    }.getOrNull()

    private fun decodeObject(o: JsonObject): BoardMessage? = when (o.string("t")) {
        "wb.stroke" -> (o["stroke"] as? JsonObject)?.let(::decodeStroke)?.let { BoardMessage.StrokeMsg(it) }
        "wb.draft" -> (o["draft"] as? JsonObject)?.let(::decodeDraft)?.let { BoardMessage.DraftMsg(it) }
        "wb.remove" -> (o["ids"] as? JsonArray)?.let { ids ->
            BoardMessage.Remove(ids.mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content })
        }
        "wb.grant" -> BoardMessage.Grant(o.stringOrNull("to"))
        "wb.clear" -> BoardMessage.Clear
        "wb.show" -> (o["on"] as? JsonPrimitive)?.booleanOrNull?.let { BoardMessage.Show(it) }
        "slides" -> BoardMessage.SlidesShown(o["slides"] is JsonObject)
        "sync.request" -> BoardMessage.SyncRequest
        "sync.state" -> BoardMessage.SyncState(
            whiteboard = (o["whiteboard"] as? JsonPrimitive)?.booleanOrNull ?: false,
            strokeCount = ((o["strokeCount"] as? JsonPrimitive)?.doubleOrNull)?.toInt() ?: -1,
            grant = o.stringOrNull("grant"),
        )
        else -> null
    }

    /** A saved stroke (the board memory's JSON), or null when it is malformed. */
    fun strokeFrom(el: JsonElement): Stroke? = (el as? JsonObject)?.let(::decodeStroke)

    private fun decodeStroke(o: JsonObject): Stroke? {
        val id = o.string("id") ?: return null
        val points = decodePoints(o["points"]) ?: return null
        return Stroke(
            id = id,
            points = points,
            color = o.string("color") ?: DEFAULT_COLOR,
            width = (o["width"] as? JsonPrimitive)?.doubleOrNull ?: DEFAULT_WIDTH,
            tool = o.string("tool")?.let { BoardTool.fromWire(it) ?: BoardTool.Pen },
            text = o.string("text"),
            by = o.string("by"),
        )
    }

    private fun decodeDraft(o: JsonObject): DraftStroke? {
        val id = o.string("id") ?: return null
        val points = decodePoints(o["points"]) ?: return null
        return DraftStroke(
            id = id,
            points = points,
            color = o.string("color") ?: DEFAULT_COLOR,
            width = (o["width"] as? JsonPrimitive)?.doubleOrNull ?: DEFAULT_WIDTH,
            tool = BoardTool.fromWire(o.string("tool")) ?: BoardTool.Pen,
        )
    }

    /** Every point must be a pair of numbers; one that is not makes the whole stroke malformed. */
    private fun decodePoints(el: JsonElement?): List<Pt>? {
        val arr = el as? JsonArray ?: return null
        return arr.map { p ->
            val pair = p as? JsonArray ?: return null
            if (pair.size < 2) return null
            val x = (pair[0] as? JsonPrimitive)?.takeIf { !it.isString }?.doubleOrNull ?: return null
            val y = (pair[1] as? JsonPrimitive)?.takeIf { !it.isString }?.doubleOrNull ?: return null
            Pt(x, y)
        }
    }

    private fun JsonObject.string(key: String): String? =
        (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content

    private fun JsonObject.stringOrNull(key: String): String? =
        (get(key) as? JsonPrimitive)?.takeIf { it !is JsonNull && it.isString }?.content

    const val DEFAULT_COLOR = "#1f2937"
    const val DEFAULT_WIDTH = 6.0
}

/**
 * Keep a stroke under the message cap by dropping every other point (the last one always stays)
 * until it fits — the web's `fitStroke`.
 */
fun fitStroke(stroke: Stroke): Stroke {
    var s = stroke
    while (BoardWire.encode(BoardMessage.StrokeMsg(s)).size > MAX_MESSAGE_BYTES) {
        if (s.points.size <= 2) break
        val last = s.points.size - 1
        s = s.copy(points = s.points.filterIndexed { i, _ -> i % 2 == 0 || i == last })
    }
    return s
}
