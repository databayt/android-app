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
}
