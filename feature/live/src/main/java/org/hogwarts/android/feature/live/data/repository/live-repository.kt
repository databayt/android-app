package org.hogwarts.android.feature.live.data.repository

import org.hogwarts.android.feature.live.domain.model.LiveRecording
import org.hogwarts.android.feature.live.domain.model.LiveSession

interface LiveRepository {
    /** `window` is today, upcoming or past — the three the landing reads as. */
    suspend fun getSessions(window: String, limit: Int = 20): List<LiveSession>
    suspend fun getRecordings(sessionId: String): List<LiveRecording>
    suspend fun getRecordingUrl(recordingId: String): String
}
