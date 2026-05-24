package org.hogwarts.android.feature.profile.data.repository

import org.hogwarts.android.feature.profile.domain.model.ActivityFeedItem
import org.hogwarts.android.feature.profile.domain.model.ContributionGraphData
import org.hogwarts.android.feature.profile.domain.model.PinnedItem
import org.hogwarts.android.feature.profile.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getOwnProfile(): UserProfile
    suspend fun getProfileById(userId: String): UserProfile
    suspend fun updateProfile(username: String?, bio: String?): UserProfile
    suspend fun getContributions(userId: String? = null, year: Int? = null): ContributionGraphData
    suspend fun getActivity(userId: String? = null, limit: Int = 20): List<ActivityFeedItem>
    suspend fun getPinnedItems(userId: String? = null): List<PinnedItem>
}
