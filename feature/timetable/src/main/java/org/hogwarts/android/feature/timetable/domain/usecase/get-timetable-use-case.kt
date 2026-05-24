package org.hogwarts.android.feature.timetable.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.timetable.data.repository.TimetableRepository
import org.hogwarts.android.feature.timetable.domain.model.TimetableEntry
import javax.inject.Inject

class GetTimetableUseCase @Inject constructor(
    private val repository: TimetableRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<TimetableEntry>>> =
        repository.getTimetable(userId)
}
