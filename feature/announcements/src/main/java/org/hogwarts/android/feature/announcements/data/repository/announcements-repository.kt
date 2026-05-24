package org.hogwarts.android.feature.announcements.data.repository

import kotlinx.coroutines.flow.Flow
import org.hogwarts.android.core.data.util.Resource
import org.hogwarts.android.feature.announcements.domain.model.Announcement

interface AnnouncementsRepository {

    fun getAnnouncements(type: String? = null): Flow<Resource<List<Announcement>>>

    fun getAnnouncement(announcementId: String): Flow<Resource<Announcement?>>

    fun getUpcomingEvents(): Flow<Resource<List<Announcement>>>
}
