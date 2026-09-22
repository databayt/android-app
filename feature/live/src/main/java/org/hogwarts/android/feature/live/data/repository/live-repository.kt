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
}
