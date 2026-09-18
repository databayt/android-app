package org.hogwarts.android.feature.dashboard.data.repository

import org.hogwarts.android.feature.dashboard.data.remote.DashboardSectionsApi
import org.hogwarts.android.feature.dashboard.data.remote.DashboardSectionsDto
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SectionsResult {
    data class Ready(val data: DashboardSectionsDto) : SectionsResult

    /**
     * The route answered 404 (not deployed yet on this school's server) or the
     * read failed. Both sections hide themselves rather than showing a table
     * of nothing — the web only ever renders them against a real answer, and
     * an empty frame under the quick actions would read as a broken page.
     */
    data object Unavailable : SectionsResult
}

interface DashboardSectionsRepository {
    suspend fun load(): SectionsResult
}

/**
 * No cache: the two tables are a second read, so an offline dashboard shows
 * the home block, the day and the actions from its saved copy and simply
 * leaves the tables off.
 */
@Singleton
class DashboardSectionsRepositoryImpl @Inject constructor(
    private val api: DashboardSectionsApi,
) : DashboardSectionsRepository {

    override suspend fun load(): SectionsResult = try {
        val response = api.getSections()
        val body = response.body()
        if (response.isSuccessful && body != null) SectionsResult.Ready(body) else SectionsResult.Unavailable
    } catch (e: IOException) {
        Timber.w(e, "Dashboard sections unavailable")
        SectionsResult.Unavailable
    }
}
