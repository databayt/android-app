package org.hogwarts.android.navigation

import android.content.Intent
import android.net.Uri

object DeepLinkHandler {

    private const val SCHEME = "hogwarts"
    private const val HOST = "app"

    fun parseDeepLink(intent: Intent?): DeepLinkDestination? {
        val uri = intent?.data ?: return null
        return parseUri(uri)
    }

    fun parseUri(uri: Uri): DeepLinkDestination? {
        if (uri.scheme != SCHEME && uri.scheme != "https") return null

        val pathSegments = uri.pathSegments
        if (pathSegments.isEmpty()) return null

        return when (pathSegments[0]) {
            "attendance" -> DeepLinkDestination.Attendance(
                studentId = uri.getQueryParameter("studentId")
            )
            "grades" -> DeepLinkDestination.Grades(
                studentId = uri.getQueryParameter("studentId")
            )
            "fees" -> DeepLinkDestination.Fees(
                feeId = uri.getQueryParameter("feeId")
            )
            "chat" -> DeepLinkDestination.Chat(
                conversationId = pathSegments.getOrNull(1)
            )
            "announcements" -> DeepLinkDestination.Announcements(
                announcementId = pathSegments.getOrNull(1)
            )
            "exams" -> DeepLinkDestination.Exams(
                examId = pathSegments.getOrNull(1)
            )
            "timetable" -> DeepLinkDestination.Timetable
            "profile" -> DeepLinkDestination.Profile
            "settings" -> DeepLinkDestination.Settings
            "notifications" -> DeepLinkDestination.Notifications
            else -> null
        }
    }

    fun createDeepLinkUri(destination: DeepLinkDestination): Uri {
        val builder = Uri.Builder()
            .scheme(SCHEME)
            .authority(HOST)

        when (destination) {
            is DeepLinkDestination.Attendance -> {
                builder.appendPath("attendance")
                destination.studentId?.let { builder.appendQueryParameter("studentId", it) }
            }
            is DeepLinkDestination.Grades -> {
                builder.appendPath("grades")
                destination.studentId?.let { builder.appendQueryParameter("studentId", it) }
            }
            is DeepLinkDestination.Fees -> {
                builder.appendPath("fees")
                destination.feeId?.let { builder.appendQueryParameter("feeId", it) }
            }
            is DeepLinkDestination.Chat -> {
                builder.appendPath("chat")
                destination.conversationId?.let { builder.appendPath(it) }
            }
            is DeepLinkDestination.Announcements -> {
                builder.appendPath("announcements")
                destination.announcementId?.let { builder.appendPath(it) }
            }
            is DeepLinkDestination.Exams -> {
                builder.appendPath("exams")
                destination.examId?.let { builder.appendPath(it) }
            }
            DeepLinkDestination.Timetable -> builder.appendPath("timetable")
            DeepLinkDestination.Profile -> builder.appendPath("profile")
            DeepLinkDestination.Settings -> builder.appendPath("settings")
            DeepLinkDestination.Notifications -> builder.appendPath("notifications")
        }

        return builder.build()
    }
}

sealed class DeepLinkDestination {
    data class Attendance(val studentId: String? = null) : DeepLinkDestination()
    data class Grades(val studentId: String? = null) : DeepLinkDestination()
    data class Fees(val feeId: String? = null) : DeepLinkDestination()
    data class Chat(val conversationId: String? = null) : DeepLinkDestination()
    data class Announcements(val announcementId: String? = null) : DeepLinkDestination()
    data class Exams(val examId: String? = null) : DeepLinkDestination()
    data object Timetable : DeepLinkDestination()
    data object Profile : DeepLinkDestination()
    data object Settings : DeepLinkDestination()
    data object Notifications : DeepLinkDestination()
}
