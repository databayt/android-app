package org.hogwarts.android.feature.live.ui.room.board

/** A draft on its way across the room: [atMs] is when it last grew, on this phone's clock. */
data class DraftEntry(val draft: DraftStroke, val atMs: Long)

/** The board as this phone holds it. The host's copy is authoritative; everyone else mirrors it. */
data class BoardState(
    /** The web's `whiteboard`: the host has the board up on the stage. */
    val show: Boolean = false,
    val strokes: List<Stroke> = emptyList(),
    /** Who besides the hosts may draw: [GRANT_EVERYONE], one identity, or null. */
    val grant: String? = null,
    /** Other people's strokes still forming; the canvas draws them above the committed layer. */
    val drafts: Map<String, DraftEntry> = emptyMap(),
)

/** The sender of a message: a participant identity, and whether the room's host set contains it. */
data class Sender(val identity: String, val isHost: Boolean)

/**
 * The reducer — the whiteboard cases of the web's `applyMessage` (`class-channel.ts`) and the
 * draft handling of `use-class-channel.ts` / `board.tsx`. Pure, so the rules are testable.
 */
object BoardReducer {
    const val DRAFT_TTL_MS = 2_000L
    private const val MAX_DRAFTS = 32
    private const val MAX_DRAFT_POINTS = 5_000
    private const val MAX_TEXT = 200

    /** May [identity] draw on the board? Hosts always; others by grant. */
    fun canDrawOn(grant: String?, identity: String, isHost: Boolean): Boolean =
        isHost || grant == GRANT_EVERYONE || grant == identity

    fun apply(state: BoardState, msg: BoardMessage, from: Sender): BoardState = when (msg) {
        is BoardMessage.StrokeMsg -> {
            val incoming = msg.stroke
            when {
                !canDrawOn(state.grant, from.identity, from.isHost) -> state
                state.strokes.any { it.id == incoming.id } -> state
                else -> {
                    // The host replays every stroke to a late joiner, so a host's copy keeps the
                    // original author; anyone else is the author of their own.
                    val by = if (from.isHost) incoming.by ?: from.identity else from.identity
                    val stroke = incoming.copy(by = by, text = incoming.text?.take(MAX_TEXT))
                    state.copy(strokes = state.strokes + stroke, drafts = state.drafts - incoming.id)
                }
            }
        }
        is BoardMessage.Remove -> {
            // The eraser and undo. A host removes anything; a granted student only what they drew.
            if (!canDrawOn(state.grant, from.identity, from.isHost)) {
                state
            } else {
                val ids = msg.ids.toSet()
                val strokes = state.strokes.filter { it.id !in ids || (!from.isHost && it.by != from.identity) }
                if (strokes.size == state.strokes.size) state else state.copy(strokes = strokes)
            }
        }
        is BoardMessage.Grant -> if (from.isHost) state.copy(grant = msg.to) else state
        BoardMessage.Clear -> if (from.isHost) state.copy(strokes = emptyList()) else state
        is BoardMessage.Show -> if (from.isHost) state.copy(show = msg.on) else state
        is BoardMessage.SlidesShown -> if (from.isHost && msg.active) state.copy(show = false) else state
        is BoardMessage.SyncState ->
            if (!from.isHost) {
                state
            } else {
                state.copy(
                    show = msg.whiteboard,
                    // strokes arrive right after, one per message, and dedupe by id
                    strokes = if (msg.strokeCount == 0) emptyList() else state.strokes,
                    grant = msg.grant,
                )
            }
        // Ephemeral or addressed: handled by the room, never stored here.
        is BoardMessage.DraftMsg, BoardMessage.SyncRequest -> state
    }

    /**
     * A lossy draft from [from]: shown only while the board is up and the sender may draw. Pen
     * and highlighter drafts append their points to the last one (a dropped packet is a straight
     * hop until the real stroke lands); shape drafts replace.
     */
    fun applyDraft(state: BoardState, draft: DraftStroke, from: Sender, nowMs: Long): BoardState {
        if (!state.show || !canDrawOn(state.grant, from.identity, from.isHost)) return state
        if (state.strokes.any { it.id == draft.id }) return state
        val fresh = state.drafts.filterValues { nowMs - it.atMs <= DRAFT_TTL_MS }
        val current = fresh[draft.id]
        val merged = if (!draft.tool.isShape && current != null) {
            current.draft.copy(points = (current.draft.points + draft.points).takeLast(MAX_DRAFT_POINTS))
        } else {
            draft
        }
        if (current == null && fresh.size >= MAX_DRAFTS) return state.copy(drafts = fresh)
        return state.copy(drafts = fresh + (draft.id to DraftEntry(merged, nowMs)))
    }

    /** Drop drafts that stopped growing; the same state when nothing expired. */
    fun pruneDrafts(state: BoardState, nowMs: Long): BoardState {
        if (state.drafts.isEmpty()) return state
        val fresh = state.drafts.filterValues { nowMs - it.atMs <= DRAFT_TTL_MS }
        return if (fresh.size == state.drafts.size) state else state.copy(drafts = fresh)
    }
}

/**
 * What to send of the stroke being drawn, and when — the web's `streamDraft`: at most every
 * [EVERY_MS], shapes whole, pen and highlighter only the points since the last packet (one
 * point of overlap so the receiver's line has no gap), at most [MAX_POINTS] per packet.
 */
class DraftStreamer {
    private var lastSentMs = 0L
    private var sentUpTo = 0

    fun reset() {
        lastSentMs = 0L
        sentUpTo = 0
    }

    fun next(draft: DraftStroke, nowMs: Long, force: Boolean = false): DraftStroke? {
        if (!force && nowMs - lastSentMs < EVERY_MS) return null
        lastSentMs = nowMs
        if (draft.tool.isShape) return draft
        val from = maxOf(0, sentUpTo - 1)
        val pts = draft.points.drop(from).take(MAX_POINTS)
        if (pts.isEmpty()) return null
        sentUpTo = from + pts.size
        return draft.copy(points = pts)
    }

    companion object {
        const val EVERY_MS = 50L
        const val MAX_POINTS = 60
    }
}
