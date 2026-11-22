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

    override suspend fun joinSlot(timetableId: String): JoinSlotResult = try {
        val response = api.joinSlot(timetableId, if (Locale.getDefault().language == "en") "en" else "ar")
        val body = response.body()
        when {
            response.isSuccessful && body != null -> JoinSlotResult.Resolved(body)
            response.code() == 403 -> JoinSlotResult.Forbidden
            // Not deployed yet: 404 / 405, or a 2xx with nothing the endpoint would have sent.
            response.code() == 404 || response.code() == 405 || response.isSuccessful -> JoinSlotResult.NotDeployed
            else -> JoinSlotResult.Failed("HTTP_${response.code()}")
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: kotlinx.serialization.SerializationException) {
        // A body that is not the endpoint's JSON (a web page from a server without it).
        JoinSlotResult.NotDeployed
    } catch (e: java.io.IOException) {
        JoinSlotResult.Failed(e.message)
    } catch (e: Exception) {
        JoinSlotResult.Failed(e.message)
    }

    override suspend fun getChat(id: String) =
        runCatching { api.getChat(id).body()?.data?.messages }.getOrNull().orEmpty()

    override suspend fun recordChat(id: String, messageId: String, text: String) {
        runCatching {
            api.recordChat(
                id,
                org.hogwarts.android.feature.live.data.remote.dto.RecordChatRequestDto(messageId, text),
            )
        }
    }

    override suspend fun getBoard(id: String): kotlinx.serialization.json.JsonArray =
        runCatching { api.getBoard(id).body()?.data?.strokes }.getOrNull() ?: kotlinx.serialization.json.JsonArray(emptyList())

    override suspend fun saveBoard(id: String, strokes: kotlinx.serialization.json.JsonArray) {
        runCatching { api.saveBoard(id, org.hogwarts.android.feature.live.data.remote.dto.BoardSaveDto(strokes)) }
    }

    override suspend fun muteAll(id: String): Boolean =
        runCatching {
            api.mute(id, org.hogwarts.android.feature.live.data.remote.dto.MuteRequestDto()).body()?.success == true
        }.getOrDefault(false)
}
