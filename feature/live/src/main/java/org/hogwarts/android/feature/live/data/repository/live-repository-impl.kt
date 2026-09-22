package org.hogwarts.android.feature.live.data.repository

import org.hogwarts.android.feature.live.data.remote.LiveApi
import org.hogwarts.android.feature.live.domain.model.LiveLanding
import org.hogwarts.android.feature.live.domain.model.LiveRecording
import java.util.Locale
import javax.inject.Inject

class LiveRepositoryImpl @Inject constructor(
    private val api: LiveApi,
) : LiveRepository {

    override suspend fun getLanding(): LiveLanding =
        api.getLanding(lang = Locale.getDefault().language).toDomain()

    override suspend fun getRecordings(sessionId: String): List<LiveRecording> =
        api.getRecordings(sessionId).data.map { it.toDomain() }

    override suspend fun getRecordingUrl(recordingId: String): String =
        api.getRecordingUrl(recordingId).url
}
