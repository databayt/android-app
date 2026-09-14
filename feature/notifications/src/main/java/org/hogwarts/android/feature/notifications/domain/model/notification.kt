package org.hogwarts.android.feature.notifications.domain.model

import java.time.Instant

data class AppNotification(
    val id: String,
    /** The web's `NotificationType`, as sent (`attendance_alert`, `fee_due`…). */
    val type: String,
    val priority: NotificationPriority,
    val title: String,
    val body: String,
    val isRead: Boolean,
    /** `metadata.url`, where the card leads. */
    val url: String?,
    val createdAt: Instant,
    val actorName: String? = null,
)

/** One page of the notification center. [unreadCount] is the user's total, not the page's. */
data class NotificationPage(
    val items: List<AppNotification>,
    val total: Int,
    val unreadCount: Int,
    val page: Int,
    val perPage: Int,
) {
    val totalPages: Int get() = if (perPage <= 0) 0 else (total + perPage - 1) / perPage
}

enum class NotificationPriority {
    Low, Normal, High, Urgent;

    companion object {
        fun fromWire(value: String?): NotificationPriority =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Normal
    }
}

/** `NotificationChannel`, in the preferences form's column order. */
enum class NotificationChannel(val wire: String) {
    InApp("in_app"), Email("email"), Push("push"), Sms("sms"), WhatsApp("whatsapp");

    companion object {
        fun fromWire(value: String): NotificationChannel? = entries.firstOrNull { it.wire == value }
    }
}

/** The web's `NOTIFICATION_TYPE_CONFIG`, keyed and ordered as `config.ts`. */
enum class NotificationKind(val wire: String, val requiresAction: Boolean) {
    Message("message", true),
    MessageMention("message_mention", true),
    AssignmentCreated("assignment_created", true),
    AssignmentDue("assignment_due", true),
    AssignmentGraded("assignment_graded", false),
    GradePosted("grade_posted", false),
    AttendanceMarked("attendance_marked", false),
    AttendanceAlert("attendance_alert", true),
    FeeDue("fee_due", true),
    FeeOverdue("fee_overdue", true),
    FeePaid("fee_paid", false),
    Announcement("announcement", false),
    EventReminder("event_reminder", false),
    ClassCancelled("class_cancelled", false),
    ClassRescheduled("class_rescheduled", false),
    SystemAlert("system_alert", true),
    AccountCreated("account_created", false),
    PasswordReset("password_reset", true),
    LoginAlert("login_alert", true),
    DocumentShared("document_shared", true),
    ReportReady("report_ready", true),
    AbsenceIntention("absence_intention", true),
    AbsenceIntentionDecision("absence_intention_decision", false),
    AbsenceUnreportedFollowup("absence_unreported_followup", true),
    SetupGuide("setup_guide", true),
    LiveClassScheduled("live_class_scheduled", false),
    LiveClassStartingSoon("live_class_starting_soon", true),
    LiveClassStarted("live_class_started", true),
    LiveClassCancelled("live_class_cancelled", false),
    LiveClassRecordingReady("live_class_recording_ready", false),
    ContentReview("content_review", true),
    ContentApproved("content_approved", false),
    ContentRejected("content_rejected", true);

    companion object {
        fun fromWire(value: String): NotificationKind? = entries.firstOrNull { it.wire == value }
    }
}

/** The preferences form: one switch per (type, channel). */
data class PreferenceMatrix(val enabled: Map<NotificationKind, Map<NotificationChannel, Boolean>>) {
    fun isOn(kind: NotificationKind, channel: NotificationChannel): Boolean = enabled[kind]?.get(channel) ?: default(channel)

    fun with(kind: NotificationKind, channel: NotificationChannel, on: Boolean): PreferenceMatrix =
        PreferenceMatrix(enabled + (kind to (NotificationChannel.entries.associateWith { isOn(kind, it) } + (channel to on))))

    companion object {
        /** `preferences-form.tsx`: in-app on, everything else off, until the user saves. */
        fun default(channel: NotificationChannel) = channel == NotificationChannel.InApp

        fun from(rows: List<Triple<String, String, Boolean>>): PreferenceMatrix {
            val stored = rows.mapNotNull { (type, channel, on) ->
                val kind = NotificationKind.fromWire(type) ?: return@mapNotNull null
                val ch = NotificationChannel.fromWire(channel) ?: return@mapNotNull null
                (kind to ch) to on
            }.toMap()
            return PreferenceMatrix(
                NotificationKind.entries.associateWith { kind ->
                    NotificationChannel.entries.associateWith { ch -> stored[kind to ch] ?: default(ch) }
                },
            )
        }
    }
}
