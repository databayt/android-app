package org.hogwarts.android.feature.live.ui.room

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * The web room's wire, so a phone in the app and a laptop on the web sit in
 * the same class. Source of truth, hogwarts `live/room/`:
 *
 * - `class-channel.ts` — the class channel: JSON `{ "t": … }` on data topic `lc`.
 * - `use-class-channel.ts` — a raised hand is the participant attributes
 *   `hand` ("1" up, "" down) and `handAt` (epoch ms); the host lowers it with
 *   `hand.clear` (addressed) or `hand.clearAll`; `muted` follows a mute-all.
 * - LiveKit components' `useChat` — chat is a text stream on topic `lk.chat`.
 *
 * Host-only messages are trusted only from the class's host or a ROOM
 * metadata host (`{ "hosts": [...] }`, written server-side) — never from a
 * participant's own attributes, which any student can rewrite.
 */
object ClassWire {
    const val CLASS_TOPIC = "lc"
    const val CHAT_TOPIC = "lk.chat"
    const val HAND = "hand"
    const val HAND_AT = "handAt"

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(type: String): ByteArray = """{"t":"$type"}""".toByteArray(Charsets.UTF_8)

    /** The message's `t`, or null for anything that is not a class message. */
    fun typeOf(payload: ByteArray): String? = runCatching {
        val obj = json.parseToJsonElement(payload.toString(Charsets.UTF_8)) as? JsonObject
        (obj?.get("t") as? JsonPrimitive)?.takeIf { it.isString }?.content
    }.getOrNull()

    /** `{ "hosts": [...] }` — the room's server-written host list. */
    fun roomHosts(metadata: String?): Set<String> = runCatching {
        val obj = json.parseToJsonElement(metadata ?: return emptySet()) as? JsonObject
        (obj?.get("hosts") as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }?.toSet()
    }.getOrNull().orEmpty()

    fun handAttributes(up: Boolean, nowMs: Long): Map<String, String> =
        mapOf(HAND to if (up) "1" else "", HAND_AT to if (up) nowMs.toString() else "")

    data class HandInput(val identity: String, val name: String, val hand: String?, val handAt: String?, val firstSeenMs: Long)

    /** Raised hands in the order they went up: `handAt` first, then when this phone first saw them. */
    fun handQueue(people: List<HandInput>): List<HandInput> =
        people.filter { it.hand == "1" }
            .sortedWith(compareBy<HandInput> { it.handAt?.toLongOrNull() ?: it.firstSeenMs }.thenBy { it.identity })
}

/** The school's room switches, from the join ticket's `roomConfig`. */
data class RoomTools(
    val chat: Boolean = true,
    val hands: Boolean = true,
    val studentShare: Boolean = false,
    val joinMuted: Boolean = false,
) {
    companion object {
        fun from(config: JsonObject?): RoomTools {
            val tools = config?.get("tools") as? JsonObject
            fun flag(o: JsonObject?, key: String, default: Boolean) =
                (o?.get(key) as? JsonPrimitive)?.booleanOrNull ?: default
            return RoomTools(
                chat = flag(tools, "chat", true),
                hands = flag(tools, "hands", true),
                studentShare = flag(tools, "studentShare", false),
                joinMuted = flag(config, "joinMuted", false),
            )
        }
    }
}

/** Why a screen share did not start — the web's `shareErrorMessage`. A closed system dialog says nothing. */
enum class ShareFailure { Failed }

@Suppress("unused")
private fun JsonObject.str(key: String): String? = (get(key) as? JsonPrimitive)?.jsonPrimitive?.content
