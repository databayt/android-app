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

    override suspend fun getSession(id: String) =
        api.getSession(id, if (Locale.getDefault().language == "en") "en" else "ar")

    override suspend fun join(id: String): org.hogwarts.android.feature.live.data.remote.dto.JoinResponseDto {
        val response = api.join(id)
        return response.body()
            ?: org.hogwarts.android.feature.live.data.remote.dto.JoinResponseDto(error = "HTTP_${response.code()}")
    }
}
