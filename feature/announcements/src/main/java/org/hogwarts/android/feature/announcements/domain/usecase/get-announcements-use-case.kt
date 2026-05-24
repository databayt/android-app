package org.hogwarts.android.feature.announcements.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.announcements.data.repository.AnnouncementsRepository
import org.hogwarts.android.feature.announcements.domain.model.Announcement
import javax.inject.Inject

class GetAnnouncementsUseCase @Inject constructor(
    private val repository: AnnouncementsRepository
) {
    operator fun invoke(type: String? = null): Flow<Resource<List<Announcement>>> =
        repository.getAnnouncements(type)
}

class GetAnnouncementDetailUseCase @Inject constructor(
    private val repository: AnnouncementsRepository
) {
    operator fun invoke(announcementId: String): Flow<Resource<Announcement?>> =
        repository.getAnnouncement(announcementId)
}

class GetUpcomingEventsUseCase @Inject constructor(
    private val repository: AnnouncementsRepository
) {
    operator fun invoke(): Flow<Resource<List<Announcement>>> =
        repository.getUpcomingEvents()
}
