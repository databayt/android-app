package org.hogwarts.android.feature.timetable.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.feature.timetable.data.remote.TimetableApi
import org.hogwarts.android.feature.timetable.data.remote.dto.TimetableBundle
import org.hogwarts.android.feature.timetable.data.remote.dto.TodayTimetableDto
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface TimetableResult {
    data class Fresh(val bundle: TimetableBundle) : TimetableResult
    /** The last good bundle, because the network failed. */
    data class Cached(val bundle: TimetableBundle) : TimetableResult
    data class Failed(val message: String?) : TimetableResult
}

/** Which of the user's screens a bundle belongs to. */
enum class TimetableScope { Mine, Guardian }

interface TimetableRepository {
    suspend fun cached(scope: TimetableScope): TimetableBundle?

    /** The signed-in student's or teacher's week, with today's periods and closure. */
    suspend fun loadMine(): TimetableResult

    /** The guardian's children and the week of [childId] (the first child when null). */
    suspend fun loadGuardian(childId: String?): TimetableResult
}

/**
 * Network first, the last good bundle as the offline fallback — the same
 * per-user file cache the dashboard keeps, so a different sign-in never sees
 * another account's week.
 */
@Singleton
class TimetableRepositoryImpl @Inject constructor(
    private val api: TimetableApi,
    private val json: Json,
    private val tenantContext: TenantContext,
    @ApplicationContext private val context: Context,
) : TimetableRepository {

    private fun cacheFile(scope: TimetableScope): File? =
        tenantContext.userId?.let { File(context.filesDir, "timetable-${scope.name.lowercase()}-$it.json") }

    override suspend fun cached(scope: TimetableScope): TimetableBundle? = withContext(Dispatchers.IO) {
        runCatching { cacheFile(scope)?.takeIf { it.exists() }?.readText()?.let { json.decodeFromString<TimetableBundle>(it) } }
            .onFailure { Timber.w(it, "Timetable cache unreadable") }
            .getOrNull()
    }

    private suspend fun save(scope: TimetableScope, bundle: TimetableBundle) = withContext(Dispatchers.IO) {
        runCatching { cacheFile(scope)?.writeText(json.encodeToString(TimetableBundle.serializer(), bundle)) }
            .onFailure { Timber.w(it, "Timetable cache not written") }
    }

    override suspend fun loadMine(): TimetableResult {
        val userId = tenantContext.userId ?: return TimetableResult.Failed(null)
        return fetch(TimetableScope.Mine) {
            coroutineScope {
                // Today is best-effort, like the web's closure read: a failure still shows the week.
                val today = async { runCatching { api.getDashboardDay().bodyOrNull()?.todayTimetable }.getOrNull() }
                val week = api.getWeek(userId).requireBody().data
                TimetableBundle(slots = week, today = today.await().takeIfUsable())
            }
        }
    }

    override suspend fun loadGuardian(childId: String?): TimetableResult = fetch(TimetableScope.Guardian) {
        val children = api.getChildren().requireBody().data
        val selected = children.firstOrNull { it.id == childId } ?: children.firstOrNull()
        val week = selected?.let { api.getChildWeek(it.id).requireBody().data }.orEmpty()
        TimetableBundle(slots = week, children = children, childId = selected?.id)
    }

    private suspend fun fetch(scope: TimetableScope, block: suspend () -> TimetableBundle): TimetableResult =
        try {
            val bundle = block()
            save(scope, bundle)
            TimetableResult.Fresh(bundle)
        } catch (e: IOException) {
            cached(scope)?.let { TimetableResult.Cached(it) } ?: TimetableResult.Failed(e.message)
        } catch (e: HttpFailure) {
            cached(scope)?.let { TimetableResult.Cached(it) } ?: TimetableResult.Failed(e.message)
        }
}

private class HttpFailure(code: Int) : Exception("HTTP $code")

private fun <T> Response<T>.requireBody(): T = body()?.takeIf { isSuccessful } ?: throw HttpFailure(code())

private fun <T> Response<T>.bodyOrNull(): T? = if (isSuccessful) body() else null

/** A school with no active term answers with no periods — nothing to lay out. */
private fun TodayTimetableDto?.takeIfUsable(): TodayTimetableDto? = this?.takeIf { it.periods.isNotEmpty() }
