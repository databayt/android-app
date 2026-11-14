package org.hogwarts.android.feature.live.ui.room

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The web room's wire, byte for byte — hogwarts `live/room/class-channel.ts` and `use-class-channel.ts`. */
class ClassWireTest {

    @Test
    fun `class messages are the web's JSON`() {
        assertEquals("""{"t":"hand.clear"}""", ClassWire.encode("hand.clear").toString(Charsets.UTF_8))
        assertEquals("muted", ClassWire.typeOf("""{"t":"muted"}""".toByteArray()))
        assertEquals("poll.tally", ClassWire.typeOf("""{"t":"poll.tally","id":"p1","counts":[1,2],"total":3}""".toByteArray()))
    }

    @Test
    fun `anything else is not a class message`() {
        assertNull(ClassWire.typeOf("not json".toByteArray()))
        assertNull(ClassWire.typeOf("""{"t":3}""".toByteArray()))
        assertNull(ClassWire.typeOf("""["t"]""".toByteArray()))
    }

    @Test
    fun `room hosts come from the server-written metadata`() {
        assertEquals(setOf("u1", "u2"), ClassWire.roomHosts("""{"hosts":["u1","u2"]}"""))
        assertTrue(ClassWire.roomHosts(null).isEmpty())
        assertTrue(ClassWire.roomHosts("garbage").isEmpty())
    }

    @Test
    fun `a hand is the attributes the web reads`() {
        assertEquals(mapOf("hand" to "1", "handAt" to "1700000000000"), ClassWire.handAttributes(true, 1_700_000_000_000))
        assertEquals(mapOf("hand" to "", "handAt" to ""), ClassWire.handAttributes(false, 5))
    }

    @Test
    fun `hands queue in the order they went up`() {
        val queue = ClassWire.handQueue(
            listOf(
                ClassWire.HandInput("b", "Bilal", "1", "200", 0),
                ClassWire.HandInput("a", "Amna", "1", "100", 0),
                ClassWire.HandInput("c", "Cyra", "", "50", 0),
                // No handAt (an old client): placed by when this phone first saw it.
                ClassWire.HandInput("d", "Dina", "1", null, 150),
            ),
        )
        assertEquals(listOf("a", "d", "b"), queue.map { it.identity })
    }

    @Test
    fun `room tools read the ticket's roomConfig`() {
        val config = Json.parseToJsonElement(
            """{"joinMuted":true,"tools":{"chat":false,"hands":true,"polls":true,"whiteboard":false,"studentShare":true}}""",
        ) as JsonObject
        val tools = RoomTools.from(config)
        assertFalse(tools.chat)
        assertTrue(tools.hands)
        assertTrue(tools.studentShare)
        assertTrue(tools.joinMuted)
        // No config: the room's defaults — chat and hands on, students do not share.
        assertEquals(RoomTools(), RoomTools.from(null))
    }

    @Test
    fun `the teacher always shares, a student only with the school's tool`() {
        assertTrue(RoomUiState(isHost = true).canShare)
        assertFalse(RoomUiState(isHost = false).canShare)
        assertTrue(RoomUiState(isHost = false, tools = RoomTools(studentShare = true)).canShare)
    }
}
