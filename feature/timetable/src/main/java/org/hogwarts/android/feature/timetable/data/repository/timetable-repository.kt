package org.hogwarts.android.feature.timetable.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.timetable.domain.model.TimetableEntry

interface TimetableRepository {
    fun getTimetable(userId: String): Flow<Resource<List<TimetableEntry>>>
}
