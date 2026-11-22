package org.hogwarts.android.feature.live.data.repository

import org.hogwarts.android.feature.live.domain.model.LiveLanding
import org.hogwarts.android.feature.live.domain.model.LiveRecording

interface LiveRepository {
    /** The `/live` landing for the signed-in reader, in the app's language. */
    suspend fun getLanding(): LiveLanding
    suspend fun getRecordings(sessionId: String): List<LiveRecording>
    suspend fun getRecordingUrl(recordingId: String): String
    suspend fun getSession(id: String): org.hogwarts.android.feature.live.data.remote.dto.SessionPageDto
    suspend fun join(id: String): org.hogwarts.android.feature.live.data.remote.dto.JoinResponseDto
    /** Saved room chat; empty on any failure (the live feed still works). */
    suspend fun getChat(id: String): List<org.hogwarts.android.feature.live.data.remote.dto.RoomChatMessageDto>
    suspend fun recordChat(id: String, messageId: String, text: String)
    /** Host: mute every student. True when the server did it. */
    suspend fun muteAll(id: String): Boolean
    /**
     * Resolve a timetable period (`/live/join/<timetableId>`). Defaulted so a fake that predates it still
     * compiles: it answers [JoinSlotResult.NotDeployed], the web fallback.
     */
    suspend fun joinSlot(timetableId: String): JoinSlotResult = JoinSlotResult.NotDeployed
    /** Host: the saved whiteboard's strokes (the wire's `Stroke` objects); empty on any failure. */
    suspend fun getBoard(id: String): kotlinx.serialization.json.JsonArray = kotlinx.serialization.json.JsonArray(emptyList())
    /** Host: save the whiteboard. Best-effort — a failure is silent. */
    suspend fun saveBoard(id: String, strokes: kotlinx.serialization.json.JsonArray) {}
}
