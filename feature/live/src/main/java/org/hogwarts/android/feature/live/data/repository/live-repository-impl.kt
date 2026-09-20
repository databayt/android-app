package org.hogwarts.android.feature.live.data.repository

import org.hogwarts.android.feature.live.data.remote.LiveApi
import org.hogwarts.android.feature.live.domain.model.LiveRecording
import org.hogwarts.android.feature.live.domain.model.LiveSession
import javax.inject.Inject

class LiveRepositoryImpl @Inject constructor(
    private val api: LiveApi,
) : LiveRepository {

    override suspend fun getSessions(window: String, limit: Int): List<LiveSession> =
        api.getSessions(window = window, limit = limit).data.map { it.toDomain() }

    override suspend fun getRecordings(sessionId: String): List<LiveRecording> =
        api.getRecordings(sessionId).data.map { it.toDomain() }

    override suspend fun getRecordingUrl(recordingId: String): String =
        api.getRecordingUrl(recordingId).url
}
